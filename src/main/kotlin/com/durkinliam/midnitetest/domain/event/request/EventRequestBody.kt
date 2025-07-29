package com.durkinliam.midnitetest.domain.event.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EventRequestBody(
    @SerialName("user_id") val userId: Long,
    val type: EventType,
    val amount: String,
    @SerialName("t") val timeRequestReceivedInMillis: Long,
)
