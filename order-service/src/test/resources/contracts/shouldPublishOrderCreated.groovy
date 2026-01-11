package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    label("orderCreated")
    input {
        triggeredBy("orderCreated()")
    }
    outputMessage {
        sentTo("order-created")
        body(
            orderId: $(regex(uuid())),
            items:  ["sku-100": 10, "sku-200": 20]
        )
    }
}

