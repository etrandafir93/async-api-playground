package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    label("orderCreatedWithTooManyItems")
    input {
        triggeredBy("orderCreatedWithTooManyItems()")
    }
    outputMessage {
        sentTo("order-created")
        body(
            orderId: $(regex(uuid())),
            items:  ["sku-100": 1000, "sku-200": 2000]
        )
    }
}

