package com.durkinliam.midnitetest.domain.event.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SuccessfulEventAlertResponse(
    val alert: Boolean,
    @SerialName("alert_codes") val alertCodes: Set<Int>,
    @SerialName("user_id") val userId: Long,
) : EventAlertResponse
