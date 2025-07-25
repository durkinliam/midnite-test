package com.durkinliam.midnitetest.event

import com.durkinliam.midnitetest.LocalCache
import com.durkinliam.midnitetest.alert.DepositAlertService
import com.durkinliam.midnitetest.alert.WithdrawalAlertService
import com.durkinliam.midnitetest.domain.EventRequestBody
import com.durkinliam.midnitetest.domain.EventResponseBody
import com.durkinliam.midnitetest.domain.EventType
import org.springframework.stereotype.Service

@Service
class EventService(
    private val cache: LocalCache,
    private val depositAlertService: DepositAlertService,
    private val withdrawalAlertService: WithdrawalAlertService,
) {
    fun handleEventRequest(eventRequestBody: EventRequestBody): EventResponseBody {
        cache.upsertRecord(eventRequestBody)

        return when (eventRequestBody.type) {
            EventType.DEPOSIT -> depositAlertService.handleDeposit(eventRequestBody)
            EventType.WITHDRAWAL -> withdrawalAlertService.handleWithdrawal(eventRequestBody)
        }
    }
}
