package com.durkinliam.midnitetest.event

import com.durkinliam.midnitetest.InMemoryStorage
import com.durkinliam.midnitetest.alert.AlertUtilities.noAlertResponse
import com.durkinliam.midnitetest.alert.DepositAlertService
import com.durkinliam.midnitetest.alert.WithdrawalAlertService
import com.durkinliam.midnitetest.domain.customer.CustomerEvent
import com.durkinliam.midnitetest.domain.customer.CustomerRecord
import com.durkinliam.midnitetest.domain.event.request.EventRequestBody
import com.durkinliam.midnitetest.domain.event.request.exception.EventRequestTimestampNotLaterThanLatestRecordException
import com.durkinliam.midnitetest.domain.event.request.EventType
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.random.Random

class EventServiceTest {
    private val cache = mockk<InMemoryStorage>(relaxed = true)
    private val depositAlertService = mockk<DepositAlertService>()
    private val withdrawalAlertService = mockk<WithdrawalAlertService>()
    private val eventService = EventService(
        cache = cache,
        depositAlertService = depositAlertService,
        withdrawalAlertService = withdrawalAlertService
    )

    @Nested
    inner class WhenARequestOfEventTypeDepositIsReceived {

        val testEventRequestBody = EventRequestBody(
            userId = userId,
            type = EventType.DEPOSIT,
            amount = Random.nextDouble().toString(),
            timeRequestReceivedInMillis = Random.nextLong()
        )

        @Nested
        inner class AndTheUserDoesNotExistInTheCache {

            @BeforeEach
            fun setUp() {
                every { cache.cache[userId] } returns null
                every { cache.upsertRecord(any()) } returns Unit
            }

            @Test
            fun `should upsert the new CustomerEvent `() {

                every { depositAlertService.handle(any()) } returns noAlertResponse(userId)

                eventService.handleEventRequest(testEventRequestBody)

                verify(exactly = 1) { cache.upsertRecord(testEventRequestBody) }
            }

            @Test
            fun `should call the DepositAlertService handle function `() {

                every { depositAlertService.handle(any()) } returns noAlertResponse(userId)

                eventService.handleEventRequest(testEventRequestBody)

                verify(exactly = 1) { depositAlertService.handle(any()) }
            }

            @Test
            fun `should not call the WithdrawalAlertService handle function `() {

                every { depositAlertService.handle(any()) } returns noAlertResponse(userId)

                eventService.handleEventRequest(testEventRequestBody)

                verify(exactly = 0) { withdrawalAlertService.handle(any()) }
            }
        }

        @Nested
        inner class AndTheUserExistsInTheCache {

            @Nested
            inner class WithATimestampBeforeTheLatestCacheTimestampForTheGivenUser {
                @BeforeEach
                fun setUp() {
                    every { cache.cache[userId] } returns CustomerRecord(
                        setOf(
                            CustomerEvent(
                                type = testEventRequestBody.type,
                                amount = testEventRequestBody.amount.toDouble(),
                                timestamp = testEventRequestBody.timeRequestReceivedInMillis + 1
                            )
                        )
                    )
                }

                @Test
                fun `should throw an EventRequestTimestampNotLaterThanLatestRecordException`() {
                    assertThrows<EventRequestTimestampNotLaterThanLatestRecordException> {
                        eventService.handleEventRequest(testEventRequestBody)
                    }
                }
            }

            @Nested
            inner class WithATimestampEqualToTheLatestCacheTimestampForTheGivenUser {
                @BeforeEach
                fun setUp() {
                    every { cache.cache[userId] } returns CustomerRecord(
                        setOf(
                            CustomerEvent(
                                type = testEventRequestBody.type,
                                amount = testEventRequestBody.amount.toDouble() + 1.00,
                                timestamp = testEventRequestBody.timeRequestReceivedInMillis
                            )
                        )
                    )
                }

                @Test
                fun `should throw an EventRequestTimestampNotLaterThanLatestRecordException`() {
                    assertThrows<EventRequestTimestampNotLaterThanLatestRecordException> {
                        eventService.handleEventRequest(testEventRequestBody)
                    }
                }
            }

            @Nested
            inner class WithATimestampLaterThanTheLatestCacheTimestampForTheGivenUser {

                @BeforeEach
                fun setUp() {
                    every { cache.cache[userId] } returns CustomerRecord(
                        setOf(
                            CustomerEvent(
                                type = testEventRequestBody.type,
                                amount = testEventRequestBody.amount.toDouble(),
                                timestamp = testEventRequestBody.timeRequestReceivedInMillis - 1
                            )
                        )
                    )

                    every { cache.upsertRecord(any()) } returns Unit
                }

                @Test
                fun `should upsert the new CustomerEvent `() {
                    every { depositAlertService.handle(any()) } returns noAlertResponse(userId)

                    eventService.handleEventRequest(testEventRequestBody)

                    verify(exactly = 1) { cache.upsertRecord(testEventRequestBody) }
                    verify(exactly = 1) { depositAlertService.handle(any()) }
                }

                @Test
                fun `should call the DepositAlertService handle function `() {

                    every { depositAlertService.handle(any()) } returns noAlertResponse(userId)

                    eventService.handleEventRequest(testEventRequestBody)

                    verify(exactly = 1) { depositAlertService.handle(any()) }
                }

                @Test
                fun `should not call the WithdrawalAlertService handle function `() {

                    every { depositAlertService.handle(any()) } returns noAlertResponse(userId)

                    eventService.handleEventRequest(testEventRequestBody)

                    verify(exactly = 0) { withdrawalAlertService.handle(any()) }
                }
            }
        }
    }

    @Nested
    inner class WhenARequestOfEventTypeWithdrawalIsReceived {

        val testEventRequestBody = EventRequestBody(
            userId = userId,
            type = EventType.WITHDRAWAL,
            amount = Random.nextDouble().toString(),
            timeRequestReceivedInMillis = Random.nextLong()
        )

        @Nested
        inner class AndTheUserDoesNotExistInTheCache {

            @BeforeEach
            fun setUp() {
                every { cache.cache[userId] } returns null
                every { cache.upsertRecord(any()) } returns Unit
            }

            @Test
            fun `should upsert the new CustomerEvent `() {

                every { withdrawalAlertService.handle(any()) } returns noAlertResponse(userId)

                eventService.handleEventRequest(testEventRequestBody)

                verify(exactly = 1) { cache.upsertRecord(testEventRequestBody) }
            }

            @Test
            fun `should call the WithdrawalAlertService handle function `() {

                every { withdrawalAlertService.handle(any()) } returns noAlertResponse(userId)

                eventService.handleEventRequest(testEventRequestBody)

                verify(exactly = 1) { withdrawalAlertService.handle(any()) }
            }

            @Test
            fun `should not call the DepositAlertService handle function `() {

                every { withdrawalAlertService.handle(any()) } returns noAlertResponse(userId)

                eventService.handleEventRequest(testEventRequestBody)

                verify(exactly = 0) { depositAlertService.handle(any()) }
            }
        }

        @Nested
        inner class AndTheUserExistsInTheCache {

            @Nested
            inner class WithATimestampBeforeTheLatestCacheTimestampForTheGivenUser {
                @BeforeEach
                fun setUp() {
                    every { cache.cache[userId] } returns CustomerRecord(
                        setOf(
                            CustomerEvent(
                                type = testEventRequestBody.type,
                                amount = testEventRequestBody.amount.toDouble(),
                                timestamp = testEventRequestBody.timeRequestReceivedInMillis + 1
                            )
                        )
                    )
                }

                @Test
                fun `should throw an EventRequestTimestampNotLaterThanLatestRecordException`() {
                    assertThrows<EventRequestTimestampNotLaterThanLatestRecordException> {
                        eventService.handleEventRequest(testEventRequestBody)
                    }
                }
            }

            @Nested
            inner class WithATimestampEqualToTheLatestCacheTimestampForTheGivenUser {
                @BeforeEach
                fun setUp() {
                    every { cache.cache[userId] } returns CustomerRecord(
                        setOf(
                            CustomerEvent(
                                type = testEventRequestBody.type,
                                amount = testEventRequestBody.amount.toDouble() + 1.00,
                                timestamp = testEventRequestBody.timeRequestReceivedInMillis
                            )
                        )
                    )
                }

                @Test
                fun `should throw an EventRequestTimestampNotLaterThanLatestRecordException`() {
                    assertThrows<EventRequestTimestampNotLaterThanLatestRecordException> {
                        eventService.handleEventRequest(testEventRequestBody)
                    }
                }
            }

            @Nested
            inner class WithATimestampLaterThanTheLatestCacheTimestampForTheGivenUser {

                @BeforeEach
                fun setUp() {
                    every { cache.cache[userId] } returns CustomerRecord(
                        setOf(
                            CustomerEvent(
                                type = testEventRequestBody.type,
                                amount = testEventRequestBody.amount.toDouble(),
                                timestamp = testEventRequestBody.timeRequestReceivedInMillis - 1
                            )
                        )
                    )

                    every { cache.upsertRecord(any()) } returns Unit
                }

                @Test
                fun `should upsert the new CustomerEvent `() {
                    every { withdrawalAlertService.handle(any()) } returns noAlertResponse(userId)

                    eventService.handleEventRequest(testEventRequestBody)

                    verify(exactly = 1) { cache.upsertRecord(testEventRequestBody) }
                }

                @Test
                fun `should call the WithdrawalAlertService handle function `() {

                    every { withdrawalAlertService.handle(any()) } returns noAlertResponse(userId)

                    eventService.handleEventRequest(testEventRequestBody)

                    verify(exactly = 1) { withdrawalAlertService.handle(any()) }
                }

                @Test
                fun `should not call the DepositAlertService handle function `() {

                    every { withdrawalAlertService.handle(any()) } returns noAlertResponse(userId)

                    eventService.handleEventRequest(testEventRequestBody)

                    verify(exactly = 0) { depositAlertService.handle(any()) }
                }
            }
        }
    }

    private companion object {
        private val userId = Random.nextLong()
    }
}
