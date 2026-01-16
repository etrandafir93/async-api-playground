package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    label("order-2k-items")
    input {
        triggeredBy("orderCreatedWith2kItems()")
    }
    outputMessage {
        sentTo("order-created")
        body(
            orderId: $(regex(uuid())),
            items:  ["sku-1": 1000, "sku-2": 1000]
        )
    }
}

