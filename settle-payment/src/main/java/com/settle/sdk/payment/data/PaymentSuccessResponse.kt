package com.settle.sdk.payment.data

data class PaymentSuccessResponse(
    val customer: Customer?,
    val order: Order?,
    val status: String?,
    val transactionId: String?
)

data class Customer(
    val countryCode: String?,
    val mobile: String?,
    val uid: String?
)

data class Order(
    val uid: String?,
    val valueInPaise: Int?
)