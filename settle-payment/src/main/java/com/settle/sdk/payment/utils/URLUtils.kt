package com.settle.sdk.payment.utils

import android.net.Uri

object URLUtils {

    private val allowedUrls = listOf(
        "account.settle.club",
        "account.uat.potleex0.de",
        "account.sit.potleex0.de",
        "l.uat.potleex0.de",
        "l.sit.potleex0.de"
    )

    private const val HTTPS = "https"
    private const val HTTP = "http"

    /**
     * Validate if the url is has http or https scheme & host is a valid settle host
     */
    fun validateURL(url: String): Boolean {
        val uri = Uri.parse(url)
        val scheme = uri.scheme
        val host = uri.host
        return ((scheme == HTTP || scheme == HTTPS) && allowedUrls.contains(host))
    }
}