package com.durkinliam.midnitetest.alert

import com.durkinliam.midnitetest.alert.AlertUtilities.isMoreThanThreshold
import com.durkinliam.midnitetest.InMemoryStorage
import com.durkinliam.midnitetest.alert.AlertUtilities.ONE_HUNDRED
import com.durkinliam.midnitetest.alert.AlertUtilities.noAlertResponse
import com.durkinliam.midnitetest.domain.alert.AlertCode.THREE_CONSECUTIVE_WITHDRAWALS
import com.durkinliam.midnitetest.domain.alert.AlertCode.WITHDRAWAL_OVER_ONE_HUNDRED
import com.durkinliam.midnitetest.domain.customer.CustomerEvent
import com.durkinliam.midnitetest.domain.event.response.EventAlertResponse
import com.durkinliam.midnitetest.domain.event.request.EventRequestBody
import com.durkinliam.midnitetest.domain.event.response.SuccessfulEventAlertResponse
import com.durkinliam.midnitetest.domain.event.request.EventType
import org.springframework.stereotype.Service

@Service
class WithdrawalAlertService(
    private val cache: InMemoryStorage,
) {
    fun handle(eventRequestBody: EventRequestBody): EventAlertResponse {
        val alertCodes = mutableSetOf<Int>()
        val userId = eventRequestBody.userId
        // This will always be non-null as the cache is populated before this service is called.
        val customerEvents = cache.cache[userId]!!.customerEvents.sortedBy { it.timestamp }
        val doesWithdrawalExceedThreshold = eventRequestBody.amount.toDouble().isMoreThanThreshold(ONE_HUNDRED)

        if (customerEvents.size < 3 && !doesWithdrawalExceedThreshold) return noAlertResponse(userId)

        if (doesWithdrawalExceedThreshold) alertCodes.add(WITHDRAWAL_OVER_ONE_HUNDRED.code)
        if (customerEvents.areLastThreeEventsAllWithdrawals()) alertCodes.add(THREE_CONSECUTIVE_WITHDRAWALS.code)

        return SuccessfulEventAlertResponse(
            alert = alertCodes.isNotEmpty(),
            alert_codes = alertCodes.toSet(),
            user_id = userId
        )
    }

    private fun List<CustomerEvent>.areLastThreeEventsAllWithdrawals() =
        this.takeLast(3).all { it.type == EventType.WITHDRAWAL }
}
