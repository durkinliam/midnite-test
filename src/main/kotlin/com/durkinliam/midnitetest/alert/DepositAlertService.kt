package com.durkinliam.midnitetest.alert

import com.durkinliam.midnitetest.alert.AlertUtilities.accumulationOverATimePeriod
import com.durkinliam.midnitetest.alert.AlertUtilities.isMoreThanThreshold
import com.durkinliam.midnitetest.LocalCache
import com.durkinliam.midnitetest.alert.AlertUtilities.THIRTY_SECONDS_IN_MILLIS
import com.durkinliam.midnitetest.alert.AlertUtilities.TWO_HUNDRED
import com.durkinliam.midnitetest.alert.AlertUtilities.noAlertResponse
import com.durkinliam.midnitetest.domain.AlertCode.ACCUMULATIVE_DEPOSIT_AMOUNT_OVER_TWO_HUNDRED_WITHIN_THIRTY_SECONDS
import com.durkinliam.midnitetest.domain.AlertCode.THREE_CONSECUTIVE_INCREASING_DEPOSITS
import com.durkinliam.midnitetest.domain.CustomerEvent
import com.durkinliam.midnitetest.domain.EventRequestBody
import com.durkinliam.midnitetest.domain.EventResponseBody
import com.durkinliam.midnitetest.domain.EventType.DEPOSIT
import org.springframework.stereotype.Service

@Service
class DepositAlertService(
    private val cache: LocalCache,
) {
    fun handleDeposit(eventRequestBody: EventRequestBody): EventResponseBody {
        val alertCodes = mutableSetOf<Int>()
        val userId = eventRequestBody.userId
        val customerEvents = cache.localCache[userId]!!.customerEvents.filter { it.type == DEPOSIT }
            .sortedBy { it.timestamp }

        // Early return to avoid unnecessary processing if there's not enough deposits to evaluate
        if (customerEvents.filter { it.type == DEPOSIT }.size < 3) return noAlertResponse(
            userId
        )

        val lastThreeDeposits = customerEvents.takeLast(3)
        val accumulatedDeposits = customerEvents.accumulationOverATimePeriod(THIRTY_SECONDS_IN_MILLIS)

        // Check if the last three deposits are increasing and if the accumulated deposits exceed the threshold
        if (!lastThreeDeposits.areLastThreeDepositsIncreasing() && !accumulatedDeposits.isMoreThanThreshold(TWO_HUNDRED)) return noAlertResponse(
            userId
        )

        if (lastThreeDeposits.areLastThreeDepositsIncreasing()) alertCodes.add(THREE_CONSECUTIVE_INCREASING_DEPOSITS.code)
        if (accumulatedDeposits.isMoreThanThreshold(TWO_HUNDRED)) alertCodes.add(
            ACCUMULATIVE_DEPOSIT_AMOUNT_OVER_TWO_HUNDRED_WITHIN_THIRTY_SECONDS.code
        )

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
