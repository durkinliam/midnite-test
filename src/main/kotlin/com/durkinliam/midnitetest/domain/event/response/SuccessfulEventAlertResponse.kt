package com.durkinliam.midnitetest.domain.event.response

import kotlinx.serialization.Serializable

@Serializable
data class SuccessfulEventAlertResponse(
    val alert: Boolean,
    val alert_codes: Set<Int>,
    val user_id: Long,
) : EventAlertResponse
