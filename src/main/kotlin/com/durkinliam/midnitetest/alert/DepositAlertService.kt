package com.durkinliam.midnitetest.alert

import com.durkinliam.midnitetest.InMemoryStorage
import com.durkinliam.midnitetest.alert.AlertUtilities.THIRTY_SECONDS_IN_MILLIS
import com.durkinliam.midnitetest.alert.AlertUtilities.TWO_HUNDRED
import com.durkinliam.midnitetest.alert.AlertUtilities.accumulationOverATimePeriod
import com.durkinliam.midnitetest.alert.AlertUtilities.isMoreThanThreshold
import com.durkinliam.midnitetest.alert.AlertUtilities.noAlertResponse
import com.durkinliam.midnitetest.domain.alert.AlertCode.ACCUMULATIVE_DEPOSIT_AMOUNT_OVER_TWO_HUNDRED_WITHIN_THIRTY_SECONDS
import com.durkinliam.midnitetest.domain.alert.AlertCode.THREE_CONSECUTIVE_INCREASING_DEPOSITS
import com.durkinliam.midnitetest.domain.customer.CustomerEvent
import com.durkinliam.midnitetest.domain.event.request.EventRequestBody
import com.durkinliam.midnitetest.domain.event.request.EventType.DEPOSIT
import com.durkinliam.midnitetest.domain.event.response.EventAlertResponse
import com.durkinliam.midnitetest.domain.event.response.SuccessfulEventAlertResponse
import org.springframework.stereotype.Service

@Service
class DepositAlertService(
    private val cache: InMemoryStorage,
) {
    fun handle(eventRequestBody: EventRequestBody): EventAlertResponse {
        val alertCodes = mutableSetOf<Int>()
        val userId = eventRequestBody.userId
        // This will always be non-null as the cache is populated before this service is called.
        val customerDepositEvents =
            cache.cache[userId]!!
                .customerEvents
                .filter { it.type == DEPOSIT }
                .sortedBy { it.timestamp }

        if (customerDepositEvents.size < 3) return noAlertResponse(userId)

        val lastThreeDeposits = customerDepositEvents.takeLast(3)
        val accumulatedDeposits = customerDepositEvents.accumulationOverATimePeriod(THIRTY_SECONDS_IN_MILLIS)

        if (!lastThreeDeposits.areLastThreeDepositsIncreasing() &&
            !accumulatedDeposits.isMoreThanThreshold(TWO_HUNDRED)
        ) {
            return noAlertResponse(
                userId,
            )
        }

        if (lastThreeDeposits.areLastThreeDepositsIncreasing()) alertCodes.add(THREE_CONSECUTIVE_INCREASING_DEPOSITS.code)
        if (accumulatedDeposits.isMoreThanThreshold(TWO_HUNDRED)) {
            alertCodes.add(
                ACCUMULATIVE_DEPOSIT_AMOUNT_OVER_TWO_HUNDRED_WITHIN_THIRTY_SECONDS.code,
            )
        }

        return SuccessfulEventAlertResponse(
            alert = alertCodes.isNotEmpty(),
            alert_codes = alertCodes.toSet(),
            user_id = userId,
        )
    }

    private fun List<CustomerEvent>.areLastThreeDepositsIncreasing() =
        this[0].amount < this[1].amount &&
            this[1].amount < this[2].amount
}
