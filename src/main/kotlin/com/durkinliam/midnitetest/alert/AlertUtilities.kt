package com.durkinliam.midnitetest.alert

import com.durkinliam.midnitetest.domain.customer.CustomerEvent
import com.durkinliam.midnitetest.domain.event.response.SuccessfulEventAlertResponse

object AlertUtilities {
    const val THIRTY_SECONDS_IN_MILLIS = 30_000L
    const val ONE_HUNDRED = 100.00
    const val TWO_HUNDRED = 200.00

    fun noAlertResponse(userId: Long) = SuccessfulEventAlertResponse(
        alert = false,
        alertCodes = emptySet(),
        userId = userId
    )

    fun List<CustomerEvent>.accumulationOverATimePeriod(
        timePeriod: Long
    ) = this
        .filter { it.timestamp <= timePeriod }
        .sumOf { it.amount.toDouble() }

    fun Double.isMoreThanThreshold(threshold: Double) = this > threshold
}
