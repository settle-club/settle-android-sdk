package com.settle.android.sdk

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.settle.sdk.payment.data.PaymentSuccessResponse
import com.settle.sdk.payment.ui.SettlePayment
import com.settle.sdk.payment.ui.SettlePaymentCallback
import com.settle.sdk.payment.ui.SettlePaymentOptions

class MainActivity : AppCompatActivity(), SettlePaymentCallback {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<Button>(R.id.start).setOnClickListener {
            SettlePayment.open(
                fragmentManager = supportFragmentManager,
                SettlePaymentOptions(paymentUrl = "", isFullScreen = false)
            )
        }
    }

    override fun onSuccess(paymentSuccessResponse: PaymentSuccessResponse?) {
        Toast.makeText(this@MainActivity, "onSuccess", Toast.LENGTH_LONG).show()
    }

    override fun onError(error: String?) {
        Toast.makeText(this@MainActivity, "onError", Toast.LENGTH_LONG).show()
    }
}