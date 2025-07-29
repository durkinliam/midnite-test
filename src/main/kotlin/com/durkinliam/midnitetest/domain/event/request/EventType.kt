package com.durkinliam.midnitetest.domain.event.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class EventType {
    @SerialName("deposit")
    DEPOSIT,

    @SerialName("withdraw")
    WITHDRAWAL,
}
