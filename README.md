# Settle Android SDK

- [Introduction](#-introduction)
- [Installation](#-installation)
- [Create an order](#-create-an-order)
- [Initiate Payment](#-initiate-payment)
- [Handle Success and Error Events](#-handle-success-and-error-events)
- [Verify Payment](#-verify-payment)

## 👋 Introduction

The Settle Android SDK allows merchants to seamlessly integrate a transaction checkout popup into
their apps. It provides functions to open the checkout popup and supports callbacks for both success
and failure scenarios, including comprehensive error handling.

## 🎉 Installation

Add the following `Gradle dependency` to your app's `build.gradle` file:

   ```gradle
   implementation("com.github.settle-club:settle-android-sdk:0.0.1")
   ```

## 📦 Create an order

To create an order and generate a payment URL, please refer
to [this documentation](https://merchant.settle.club/help/docs/developer-guide/api-reference/customer/createTransaction),
where you can find detailed information about the request and response. 

## 💰 Initiate Payment

Obtain the `redirectUrl` from the Create Order API and open it using `SettlePayment.open()`.

#### Parameters:

| Parameter                                    | Description                                                                                                | Default |
|----------------------------------------------|------------------------------------------------------------------------------------------------------------|---------|
| fragmentManager*   `FragmentManager`         | Current Activity or Fragment fragmentManager.                                                              | -       |
| settlePaymentOptions* `SettlePaymentOptions` | Payment configurations that can be customized by the user.                                                 | -       |
| &nbsp;&nbsp;&nbsp; paymentUrl* `string`      | `redirectUrl` obtained from the [create-order](#create-an-order)API.                                       | -       |
| &nbsp;&nbsp;&nbsp; isFullScreen `boolean`    | Controls the display mode of the payment page:<br />`true`: Full screen<br />`false` (default): Popup mode | false   |

*Required parameter

## ✅ Handle Success and Error Events

Extend your activity or fragment with `SettlePaymentCallback` to intercept payment success or error
events.

**Note:** To intercept callbacks in a fragment, extend it with SettlePaymentCallback and pass the
fragment's childFragmentManager to the SettlePayment.open() method.

Override the `onSuccess` and `onError` callbacks in your activity or fragment:

##### Sample code:

```Kotlin
class MainActivity : AppCompatActivity(), SettlePaymentCallback {
    override fun onSuccess(paymentSuccessResponse: PaymentSuccessResponse) {
        // Handle successful payment response
    }

    override fun onError(error: String) {
        // Handle payment failure
    }
}
```

##### PaymentSuccessResponse Schema

| Parameter                     | Description                                                                              | Possible Values                    |
|-------------------------------|------------------------------------------------------------------------------------------|------------------------------------|
| status `string`               | Transaction status.                                                                      | `SUCCESS`, `FAILED` or `CANCELLED` |
| order `object`                | Order details. May be empty for `FAILED` or `CANCELLED` statuses.                        |                                    |
| order.valueInPaise `int`      | Total order value in paise.                                                              |                                    |
| order.uid `string`            | Unique identifier for the order.                                                         |                                    |
| customer `object`             | Customer details. May be empty for `FAILED` or `CANCELLED` statuses.                     |                                    |
| customer.mobile `string`      | Customer's mobile number.                                                                |                                    |
| customer.countryCode `string` | Country code of the customer's mobile number.                                            |                                    |
| customer.uid `string`         | Unique identifier for the customer.                                                      |                                    |
| transactionId `string`        | Unique identifier for the transaction. May be null for failed or cancelled transactions. |                                    |

## 💸 Verify Payment

To verify payments from your backend, securely use
our [Transaction Webhook](https://merchant.settle.club/help/docs/developer-guide/webhooks/events/transaction) with
proper authentication and validation mechanisms.

