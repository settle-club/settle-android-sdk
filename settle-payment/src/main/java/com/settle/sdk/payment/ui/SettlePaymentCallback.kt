package com.settle.sdk.payment.ui

import com.settle.sdk.payment.data.PaymentSuccessResponse

interface SettlePaymentCallback {
    fun onSuccess(paymentSuccessResponse: PaymentSuccessResponse?)
    fun onError(error: String?)
}