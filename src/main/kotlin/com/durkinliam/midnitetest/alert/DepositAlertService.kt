package com.durkinliam.midnitetest.alert

import com.durkinliam.midnitetest.alert.AlertUtilities.accumulationOverATimePeriod
import com.durkinliam.midnitetest.alert.AlertUtilities.isMoreThanThreshold
import com.durkinliam.midnitetest.LocalCache
import com.durkinliam.midnitetest.domain.AlertCode
import com.durkinliam.midnitetest.domain.CustomerEvent
import com.durkinliam.midnitetest.domain.EventRequestBody
import com.durkinliam.midnitetest.domain.EventResponseBody
import com.durkinliam.midnitetest.domain.EventType
import org.springframework.stereotype.Service

@Service
class DepositAlertService(
    private val cache: LocalCache,
) {
    fun handleDeposit(eventRequestBody: EventRequestBody): EventResponseBody {
        val alertCodes = mutableSetOf<Int>()
        val userId = eventRequestBody.userId
        val customerEvents = cache.localCache[userId]!!.customerEvents.filter { it.type == EventType.DEPOSIT }
            .sortedBy { it.timestamp }

        // Early return to avoid unnecessary processing if there's not enough deposits to evaluate
        if (customerEvents.filter { it.type == EventType.DEPOSIT }.size < 3) return AlertUtilities.noAlertResponse(
            userId
        )

        val lastThreeDeposits = customerEvents.takeLast(3)
        val accumulatedDeposits = customerEvents.accumulationOverATimePeriod(AlertUtilities.THIRTY_SECONDS_IN_MILLIS)

        // Check if the last three deposits are increasing and if the accumulated deposits exceed the threshold
        if (!lastThreeDeposits.areLastThreeDepositsIncreasing() && !accumulatedDeposits.isMoreThanThreshold(
                AlertUtilities.TWO_HUNDRED
            )) return AlertUtilities.noAlertResponse(
            userId
        )

        if (lastThreeDeposits.areLastThreeDepositsIncreasing()) alertCodes.add(AlertCode.THREE_CONSECUTIVE_INCREASING_DEPOSITS.code)
        if (accumulatedDeposits.isMoreThanThreshold(AlertUtilities.TWO_HUNDRED)) alertCodes.add(AlertCode.ACCUMULATIVE_DEPOSIT_AMOUNT_OVER_TWO_HUNDRED_WITHIN_THIRTY_SECONDS.code)

        return EventResponseBody(
            alert = true,
            alertCodes = alertCodes.toSet(),
            userId = userId
        )
    }

    private fun List<CustomerEvent>.areLastThreeDepositsIncreasing() =
        this[0].amount.toDouble() < this[1].amount.toDouble() &&
                this[1].amount.toDouble() < this[2].amount.toDouble()
}
