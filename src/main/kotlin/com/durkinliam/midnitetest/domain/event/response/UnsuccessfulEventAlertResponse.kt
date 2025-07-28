package com.durkinliam.midnitetest.domain.event.response

import kotlinx.serialization.Serializable

@Serializable
data class UnsuccessfulEventAlertResponse(
    val reason: String,
) : EventAlertResponse
