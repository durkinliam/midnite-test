package com.durkinliam.midnitetest.alert

import com.durkinliam.midnitetest.alert.AlertUtilities.isMoreThanThreshold
import com.durkinliam.midnitetest.LocalCache
import com.durkinliam.midnitetest.domain.AlertCode
import com.durkinliam.midnitetest.domain.CustomerEvent
import com.durkinliam.midnitetest.domain.EventRequestBody
import com.durkinliam.midnitetest.domain.EventResponseBody
import com.durkinliam.midnitetest.domain.EventType
import org.springframework.stereotype.Service

@Service
class WithdrawalAlertService(
    private val cache: LocalCache,
) {
    fun handleWithdrawal(eventRequestBody: EventRequestBody): EventResponseBody {
        val alertCodes = mutableSetOf<Int>()
        val userId = eventRequestBody.userId
        val customerEvents = cache.localCache[userId]!!.customerEvents.sortedBy { it.timestamp }

        val doesWithdrawalExceedThreshold = eventRequestBody.amount.toDouble().isMoreThanThreshold(AlertUtilities.ONE_HUNDRED)

        if (customerEvents.size < 3 && !doesWithdrawalExceedThreshold) return AlertUtilities.noAlertResponse(userId)

        val areLastThreeEventsAllWithdrawals = customerEvents.areLastThreeEventsAllWithdrawals()

        if (doesWithdrawalExceedThreshold) alertCodes.add(AlertCode.WITHDRAWAL_OVER_ONE_HUNDRED.code)
        if (areLastThreeEventsAllWithdrawals) alertCodes.add(AlertCode.THREE_CONSECUTIVE_WITHDRAWALS.code)

        return EventResponseBody(
            alert = true,
            alertCodes = alertCodes.toSet(),
            userId = userId
        )
    }

    private fun List<CustomerEvent>.areLastThreeEventsAllWithdrawals() =
        this.takeLast(3).all { it.type == EventType.WITHDRAWAL }
}
