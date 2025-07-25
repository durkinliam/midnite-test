package com.durkinliam.midnitetest.event

import com.durkinliam.midnitetest.LocalCache
import com.durkinliam.midnitetest.alert.AlertUtilities
import com.durkinliam.midnitetest.alert.DepositAlertService
import com.durkinliam.midnitetest.alert.WithdrawalAlertService
import com.durkinliam.midnitetest.domain.EventRequestBody
import com.durkinliam.midnitetest.domain.EventType
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.random.Random

class EventServiceTest {
    private val cache = mockk<LocalCache>(relaxed = true)
    private val depositAlertService = mockk<DepositAlertService>()
    private val withdrawalAlertService = mockk<WithdrawalAlertService>()
    private val eventService = EventService(
        cache = cache,
        depositAlertService = depositAlertService,
        withdrawalAlertService = withdrawalAlertService
    )

    @Nested
    inner class WhenARequestOfEventTypeDepositIsReceived{
        val userId = Random.nextLong()
        val testEventRequestBody = EventRequestBody(
            userId = userId,
            type = EventType.DEPOSIT,
            amount = Random.nextDouble().toString(),
            timeRequestReceivedInMillis = Random.nextLong()
        )

        @BeforeEach
        fun setUp() {
            every { cache.upsertRecord(testEventRequestBody) } returns Unit
            every { depositAlertService.handleDeposit(testEventRequestBody) } returns AlertUtilities.noAlertResponse(userId)
        }

        @Test
        fun `should call upsertRecord on cache when an event request is handled`() {
            eventService.handleEventRequest(testEventRequestBody)

            verify(exactly = 1) { cache.upsertRecord(testEventRequestBody) }
        }

        @Test
        fun `should call depositAlertService`() {
            eventService.handleEventRequest(testEventRequestBody)

            verify(exactly = 1) { depositAlertService.handleDeposit(testEventRequestBody) }
        }

        @Test
        fun `should not call withdrawalAlertService`() {
            eventService.handleEventRequest(testEventRequestBody)

            verify(exactly = 0) { withdrawalAlertService.handleWithdrawal(testEventRequestBody) }
        }
    }

    @Nested
    inner class WhenARequestOfEventTypeWithdrawalIsReceived{

        val userId = Random.nextLong()
        val testEventRequestBody = EventRequestBody(
            userId = userId,
            type = EventType.WITHDRAWAL,
            amount = Random.nextDouble().toString(),
            timeRequestReceivedInMillis = Random.nextLong()
        )

        @BeforeEach
        fun setUp() {
            every { cache.upsertRecord(testEventRequestBody) } returns Unit
            every { withdrawalAlertService.handleWithdrawal(testEventRequestBody) } returns AlertUtilities.noAlertResponse(userId)
        }

        @Test
        fun `should call upsertRecord on cache when an event request is handled`() {
            eventService.handleEventRequest(testEventRequestBody)

            verify(exactly = 1) { cache.upsertRecord(testEventRequestBody) }
        }

        @Test
        fun `should call withdrawalAlertService`() {
            eventService.handleEventRequest(testEventRequestBody)

            verify(exactly = 1) { withdrawalAlertService.handleWithdrawal(testEventRequestBody) }
        }

        @Test
        fun `should not call depositAlertService`() {
            eventService.handleEventRequest(testEventRequestBody)

            verify(exactly = 0) { depositAlertService.handleDeposit(testEventRequestBody) }
        }
    }
}
