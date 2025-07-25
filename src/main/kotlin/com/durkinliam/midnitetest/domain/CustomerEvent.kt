package com.durkinliam.midnitetest.domain

import kotlinx.serialization.Serializable

@Serializable
data class CustomerEvent(
    val type: EventType,
    val amount: String,
    val timestamp: Long,
)
