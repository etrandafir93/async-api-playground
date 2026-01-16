package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    label("order-2-items")
    input {
        triggeredBy("orderCreated()")
    }
    outputMessage {
        sentTo("order-created")
        body(
            orderId: $(regex(uuid())),
            items:  ["sku-1": 1, "sku-2": 1]
        )
    }
}

