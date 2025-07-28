package com.durkinliam.midnitetest.event

import com.durkinliam.midnitetest.InMemoryStorage
import com.durkinliam.midnitetest.alert.DepositAlertService
import com.durkinliam.midnitetest.alert.WithdrawalAlertService
import com.durkinliam.midnitetest.domain.event.response.EventAlertResponse
import com.durkinliam.midnitetest.domain.event.request.EventRequestBody
import com.durkinliam.midnitetest.domain.event.request.EventRequestTimestampNotLaterThanLatestRecordException
import com.durkinliam.midnitetest.domain.event.request.EventType.DEPOSIT
import com.durkinliam.midnitetest.domain.event.request.EventType.WITHDRAWAL
import org.springframework.stereotype.Service

@Service
class EventService(
    private val cache: InMemoryStorage,
    private val depositAlertService: DepositAlertService,
    private val withdrawalAlertService: WithdrawalAlertService,
) {
    fun handleEventRequest(eventRequestBody: EventRequestBody): EventAlertResponse {
        val customerEvents = cache.cache[eventRequestBody.userId]?.customerEvents?.sortedBy { it.timestamp }

        if (customerEvents.isNullOrEmpty()) {
            cache.upsertRecord(eventRequestBody)
        } else if (eventRequestBody.timeRequestReceivedInMillis <= customerEvents.last().timestamp) {
            throw EventRequestTimestampNotLaterThanLatestRecordException(eventRequestBody.userId)
        } else {
            cache.upsertRecord(eventRequestBody)
        }

        return when (eventRequestBody.type) {
            DEPOSIT -> depositAlertService.handle(eventRequestBody)
            WITHDRAWAL -> withdrawalAlertService.handle(eventRequestBody)
        }
    }
}
