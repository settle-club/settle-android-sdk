package com.settle.sdk.payment.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.GeolocationPermissions
import android.webkit.JavascriptInterface
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.fragment.app.setFragmentResultListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.settle.sdk.payment.R
import com.settle.sdk.payment.data.PaymentSuccessResponse
import com.settle.sdk.payment.permissions.PermissionHandlerFragment
import com.settle.sdk.payment.permissions.PermissionType
import com.settle.sdk.payment.utils.URLUtils

class SettlePayment : DialogFragment() {

    private var isFullScreen: Boolean = false
    private var paymentUrl: String = ""
    private var webView: WebView? = null
    private var settlePaymentCallback: SettlePaymentCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            isFullScreen = it.getBoolean(IS_FULL_SCREEN, false)
            paymentUrl = it.getString(PAYMENT_URL, "")
        }
        setupFragmentResultListener()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_payment_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        webView = view.findViewById(R.id.web_view)
        setupWebView()

        // This will make sure the status bar is visible
        activity?.window?.let {
            WindowCompat.getInsetsController(it, view).apply {
                isAppearanceLightStatusBars = true
                show(WindowInsetsCompat.Type.statusBars())
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object : Dialog(requireContext(), R.style.FullScreenDialogStyle) {
            // Using deprecated onBackPressed since dialog opens in a new window
            // More info: https://issuetracker.google.com/issues/149173280
            @Deprecated("Deprecated in Java")
            override fun onBackPressed() {
                showPaymentConfirmationDialog()
            }
        }.apply {
            val window = window ?: return@apply
            if (!isFullScreen) {
                window.attributes = window.attributes?.apply {
                    width = (resources.displayMetrics.widthPixels * 0.9).toInt()
                    height = (resources.displayMetrics.heightPixels * 0.9).toInt()
                }

                window.setBackgroundDrawable(
                    ContextCompat.getDrawable(requireContext(), R.drawable.rounded_corner_24)
                )
            }
            setCanceledOnTouchOutside(false)
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        dismissDialog(isSuccess = false)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setWindowAnimations(R.style.dialog_animation_fade)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        settlePaymentCallback = try {
            when {
                parentFragment is SettlePaymentCallback -> parentFragment as SettlePaymentCallback
                context is SettlePaymentCallback -> context
                else -> null
            }
        } catch (e: ClassCastException) {
            null
        }
    }

    private fun showPaymentConfirmationDialog() {
        MaterialAlertDialogBuilder(
            requireContext(),
            R.style.AlertDialogTheme
        ).setTitle(getString(R.string.exit_confirmation_title))
            .setMessage(getString(R.string.exit_continue_subtitle))
            .setNegativeButton(getString(R.string.exit_action)) { _, _ ->
                // Dismiss the payment dialog and communicate back to the merchant app
                dismissDialog(isSuccess = false)
            }.setPositiveButton(getString(R.string.continue_action)) { _, _ ->
                // Do Nothing
            }.show()
    }

    private fun setupFragmentResultListener() {
        setFragmentResultListener(
            PermissionHandlerFragment.RESULT_PERMISSION_KEY
        ) { _, bundle ->
            val permissionType: PermissionType? =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    bundle.getSerializable(
                        PermissionHandlerFragment.RESULT_PERMISSION_TYPE_KEY,
                        PermissionType::class.java
                    )
                } else {
                    bundle.getSerializable(
                        PermissionHandlerFragment.RESULT_PERMISSION_TYPE_KEY
                    ) as? PermissionType
                }

            val permissionGranted =
                bundle.getBoolean(PermissionHandlerFragment.RESULT_PERMISSION_GRANTED_KEY)

            when (permissionType) {
                PermissionType.Camera,
                PermissionType.Location -> {
                    processJSPermission(
                        permissionType = permissionType,
                        permissionGranted = permissionGranted
                    )
                }
                null -> {
                    // Do nothing
                }
            }
        }
    }

    private fun setupWebView() {
        applyWebViewSettings()
        addChromeClient()
        addWebViewClient()
        addJSInterface()
        //Only load settle club urls
        if (URLUtils.validateURL(paymentUrl)) {
            webView?.loadUrl(paymentUrl)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun applyWebViewSettings() {
        webView?.settings?.apply {
            // Enable DOM storage for saving user data.
            domStorageEnabled = true
            // Enable javascript
            javaScriptEnabled = true
            // Enable geolocation
            setGeolocationEnabled(true)
            // Allow media playback without user gesture
            mediaPlaybackRequiresUserGesture = false
        }
    }

    private fun addChromeClient() {
        webView?.webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest?) {
                // Check if camera permission is granted
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    // Grant camera permission
                    request?.grant(request.resources)
                } else {
                    // Ask for camera permission or redirect the user to application settings
                    // to grant the required permissions
                    askCameraPermission()
                }
            }

            override fun onGeolocationPermissionsShowPrompt(
                origin: String?,
                callback: GeolocationPermissions.Callback?,
            ) {
                // Check if location permissions are granted
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    // Grant location permission
                    callback?.invoke(origin, true, false)
                } else {
                    // Ask for location permission or redirect the user to application settings
                    // to grant the required permissions
                    askLocationPermission()
                }
            }
        }
    }

    private fun addWebViewClient() {
        webView?.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?,
            ): Boolean {
                // Handle URL redirection here
                return super.shouldOverrideUrlLoading(view, request)
            }
        }
    }

    private fun addJSInterface() {
        webView?.addJavascriptInterface(
            object {
                // Used to grant location permission to the webview
                @JavascriptInterface
                fun getLocationPermission(params: String) {
                    if (ContextCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        // Grant location permission
                        processJSPermission(
                            permissionType = PermissionType.Location,
                            permissionGranted = true
                        )
                    } else {
                        // Ask for location permission or redirect the user to application settings
                        // to grant the required permissions
                        askLocationPermission()
                    }
                }

                // Used to grant camera permission to the webview
                @JavascriptInterface
                fun getCameraPermission(params: String) {
                    if (ContextCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        // Grant camera permission
                        processJSPermission(
                            permissionType = PermissionType.Camera,
                            permissionGranted = true
                        )
                    } else {
                        // Ask for camera permission or redirect the user to application settings
                        // to grant the required permissions
                        askCameraPermission()
                    }
                }

                @JavascriptInterface
                fun onTransactionSuccess(params: String?) {
                    activity?.runOnUiThread {
                        val paymentSuccessResponse = try {
                            if (!params.isNullOrBlank()) {
                                Gson().fromJson(
                                    params,
                                    PaymentSuccessResponse::class.java
                                )
                            } else {
                                null
                            }
                        } catch (throwable: Throwable) {
                            null
                        }
                        dismissDialog(
                            isSuccess = true,
                            paymentSuccessResponse = paymentSuccessResponse
                        )
                    }
                }

                @JavascriptInterface
                fun onTransactionFailure(params: String?) {
                    activity?.runOnUiThread {
                        dismissDialog(isSuccess = false, error = params)
                    }
                }
            },
            SETTLE_ANDROID_KIT
        )
    }

    private fun processJSPermission(
        permissionType: PermissionType,
        permissionGranted: Boolean,
    ) {
        when (permissionType) {
            PermissionType.Camera,
            PermissionType.Location -> {
                invokeJavascriptPermission(
                    permissionType = permissionType,
                    permissionGranted = permissionGranted
                )
            }
        }
    }

    private fun invokeJavascriptPermission(
        permissionType: PermissionType,
        permissionGranted: Boolean,
    ) {
        activity?.runOnUiThread {
            val permissionName = permissionType.name
            val javascriptCode =
                "window.processPermission(${permissionGranted}, '${permissionName}');"
            webView?.evaluateJavascript(javascriptCode, null)
        }
    }

    private fun dismissDialog(
        isSuccess: Boolean,
        paymentSuccessResponse: PaymentSuccessResponse? = null,
        error: String? = null
    ) {
        if (isSuccess) {
            settlePaymentCallback?.onSuccess(paymentSuccessResponse = paymentSuccessResponse)
        } else {
            settlePaymentCallback?.onError(error = error)
        }
        dismiss()
    }

    private fun askCameraPermission() {
        addFragment(fragment = PermissionHandlerFragment.cameraPermissionInstance())
    }

    private fun askLocationPermission() {
        addFragment(fragment = PermissionHandlerFragment.locationPermissionInstance())
    }

    private fun addFragment(fragment: PermissionHandlerFragment) {
        childFragmentManager.commit {
            add(
                fragment,
                PermissionHandlerFragment::class.simpleName
            )
        }
    }

    companion object {

        private const val IS_FULL_SCREEN = "isFullScreen"
        private const val PAYMENT_URL = "paymentUrl"
        private const val SETTLE_ANDROID_KIT = "__settleAndroidKit"

        @JvmStatic
        fun open(
            fragmentManager: FragmentManager,
            settlePaymentOptions: SettlePaymentOptions
        ) {
            if (settlePaymentOptions.paymentUrl.isBlank()) return

            return SettlePayment().apply {
                arguments = Bundle().apply {
                    putBoolean(IS_FULL_SCREEN, settlePaymentOptions.isFullScreen)
                    putString(PAYMENT_URL, settlePaymentOptions.paymentUrl)
                }
            }.show(fragmentManager, SettlePayment::class.simpleName)
        }
    }
}