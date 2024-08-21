package com.settle.sdk.payment.ui

interface PaymentCallback {
    fun onSuccess()
    fun onError()
}