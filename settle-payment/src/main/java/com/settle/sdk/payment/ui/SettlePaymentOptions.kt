package com.settle.sdk.payment.ui

data class SettlePaymentOptions @JvmOverloads constructor(
    val paymentUrl: String,
    val isFullScreen: Boolean = false
)