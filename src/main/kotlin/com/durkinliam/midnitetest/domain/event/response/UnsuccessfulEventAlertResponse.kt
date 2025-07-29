package com.durkinliam.midnitetest.domain.event.response

import kotlinx.serialization.Serializable

@Serializable
data class UnsuccessfulEventAlertResponse(
    val user_id: Long? = null,
    val reason: String,
) : EventAlertResponse
