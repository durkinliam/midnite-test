package com.durkinliam.midnitetest.domain.customer

import com.durkinliam.midnitetest.domain.event.request.EventType
import kotlinx.serialization.Serializable

@Serializable
data class CustomerEvent(
    val type: EventType,
    val amount: String,
    val timestamp: Long,
)
