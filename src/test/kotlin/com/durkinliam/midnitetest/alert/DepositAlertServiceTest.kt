package com.durkinliam.midnitetest.alert

import com.durkinliam.midnitetest.InMemoryStorage
import com.durkinliam.midnitetest.domain.customer.CustomerEvent
import com.durkinliam.midnitetest.domain.customer.CustomerRecord
import com.durkinliam.midnitetest.domain.event.request.EventRequestBody
import com.durkinliam.midnitetest.domain.event.request.EventType.DEPOSIT
import com.durkinliam.midnitetest.domain.event.response.SuccessfulEventAlertResponse
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DepositAlertServiceTest {

    private val cache = mockk<InMemoryStorage>(relaxed = true)
    private val depositAlertService = DepositAlertService(cache)

    val eventRequestBody = EventRequestBody(
        userId = 123,
        type = DEPOSIT,
        amount = "10.00",
        timeRequestReceivedInMillis = 100
    )

    @BeforeEach
    fun setUp() {
        every { cache.upsertRecord(any()) } returns Unit
    }

    @Nested
    inner class WhenACustomerHasLessThanThreeDeposits {

        @BeforeEach
        fun setUp() {
            every { cache.cache[eventRequestBody.userId] } returns CustomerRecord(
                customerEvents = setOf(
                    CustomerEvent(
                        type = DEPOSIT,
                        amount = eventRequestBody.amount.toDouble(),
                        timestamp = eventRequestBody.timeRequestReceivedInMillis
                    )
                )
            )
        }

        @Test
        fun `should return a SuccessfulEventAlertResponse with no alerts`() {
            val response = depositAlertService.handle(eventRequestBody)

            assertTrue(response is SuccessfulEventAlertResponse)

            val successfulResponse = response as SuccessfulEventAlertResponse

            assertFalse(successfulResponse.alert)
            assertTrue(response.alert_codes.isEmpty())
            assertEquals(eventRequestBody.userId, successfulResponse.user_id)
        }
    }

    @Nested
    inner class WhenACustomerHasAtLeastThreeDeposits {

        @Nested
        inner class AndTheAccumulatedSumOfDepositsWithinTheLast30SecondsAreNotMoreThan200 {

            @Test
            fun `and the last 3 deposits are not increasing, should return SuccessfulEventAlertResponse with no alerts`() {
                every { cache.cache[eventRequestBody.userId] } returns CustomerRecord(
                    customerEvents = setOf(
                        CustomerEvent(
                            type = DEPOSIT,
                            amount = eventRequestBody.amount.toDouble() - 10.00,
                            timestamp = eventRequestBody.timeRequestReceivedInMillis - 3
                        ),
                        CustomerEvent(
                            type = DEPOSIT,
                            amount = eventRequestBody.amount.toDouble() ,
                            timestamp = eventRequestBody.timeRequestReceivedInMillis - 2
                        ),
                        CustomerEvent(
                            type = DEPOSIT,
                            amount = eventRequestBody.amount.toDouble() - 10.00,
                            timestamp = eventRequestBody.timeRequestReceivedInMillis - 1
                        ),
                        CustomerEvent(
                            type = DEPOSIT,
                            amount = eventRequestBody.amount.toDouble() + 10.00,
                            timestamp = eventRequestBody.timeRequestReceivedInMillis
                        ),
                    )
                )

                val response = depositAlertService.handle(eventRequestBody)

                assertTrue(response is SuccessfulEventAlertResponse)

                val successfulResponse = response as SuccessfulEventAlertResponse

                assertFalse(successfulResponse.alert)
                assertTrue(response.alert_codes.isEmpty())
                assertEquals(eventRequestBody.userId, successfulResponse.user_id)
            }

            @Test
            fun `and the last 3 deposits are increasing, should return SuccessfulEventAlertResponse with an AlertCode of 300`() {
                every { cache.cache[eventRequestBody.userId] } returns CustomerRecord(
                    customerEvents = setOf(
                        CustomerEvent(
                            type = DEPOSIT,
                            amount = eventRequestBody.amount.toDouble() - 7.50,
                            timestamp = eventRequestBody.timeRequestReceivedInMillis - 2
                        ),
                        CustomerEvent(
                            type = DEPOSIT,
                            amount = eventRequestBody.amount.toDouble() - 5.00,
                            timestamp = eventRequestBody.timeRequestReceivedInMillis - 1
                        ),
                        CustomerEvent(
                            type = DEPOSIT,
                            amount = eventRequestBody.amount.toDouble() - 1.00,
                            timestamp = eventRequestBody.timeRequestReceivedInMillis
                        ),
                    )
                )

                val response = depositAlertService.handle(eventRequestBody)

                assertTrue(response is SuccessfulEventAlertResponse)

                val successfulResponse = response as SuccessfulEventAlertResponse

                assertTrue(successfulResponse.alert)
                assertEquals(1, successfulResponse.alert_codes.size)
                assertTrue(successfulResponse.alert_codes.contains(300))
                assertEquals(eventRequestBody.userId, successfulResponse.user_id)
            }
        }

        @Nested
        inner class AndTheLast3DepositsAreNotIncreasing{
            @Test
            fun `and the accumulated sum of deposits within the last 30 seconds is more than 200, should return SuccessfulEventAlertResponse with an AlertCode of 123`() {
                every { cache.cache[eventRequestBody.userId] } returns CustomerRecord(
                    customerEvents = setOf(
                        CustomerEvent(
                            type = DEPOSIT,
                            amount = eventRequestBody.amount.toDouble() + 150.00,
                            timestamp = eventRequestBody.timeRequestReceivedInMillis - 3
                        ),
                        CustomerEvent(
                            type = DEPOSIT,
                            amount = eventRequestBody.amount.toDouble() + 75.00,
                            timestamp = eventRequestBody.timeRequestReceivedInMillis - 2
                        ),
                        CustomerEvent(
                            type = DEPOSIT,
                            amount = eventRequestBody.amount.toDouble() + 50.00,
                            timestamp = eventRequestBody.timeRequestReceivedInMillis - 1
                        ),
                    )
                )

                val response = depositAlertService.handle(eventRequestBody)

                assertTrue(response is SuccessfulEventAlertResponse)

                val successfulResponse = response as SuccessfulEventAlertResponse

                assertTrue(successfulResponse.alert)
                assertEquals(1, successfulResponse.alert_codes.size)
                assertTrue(successfulResponse.alert_codes.contains(123))
                assertEquals(eventRequestBody.userId, successfulResponse.user_id)
            }
        }

        @Test
        fun `should return a SuccessfulEventAlertResponse with AlertCodes of 123 and 300 when both conditions are met`() {
            every { cache.cache[eventRequestBody.userId] } returns CustomerRecord(
                customerEvents = setOf(
                    CustomerEvent(
                        type = DEPOSIT,
                        amount = eventRequestBody.amount.toDouble(),
                        timestamp = eventRequestBody.timeRequestReceivedInMillis - 2
                    ),
                    CustomerEvent(
                        type = DEPOSIT,
                        amount = eventRequestBody.amount.toDouble() + 10.00,
                        timestamp = eventRequestBody.timeRequestReceivedInMillis - 1
                    ),
                    CustomerEvent(
                        type = DEPOSIT,
                        amount = eventRequestBody.amount.toDouble() + 190.00,
                        timestamp = eventRequestBody.timeRequestReceivedInMillis
                    ),
                )
            )

            val response = depositAlertService.handle(eventRequestBody)

            assertTrue(response is SuccessfulEventAlertResponse)

            val successfulResponse = response as SuccessfulEventAlertResponse

            assertTrue(successfulResponse.alert)
            assertEquals(2, successfulResponse.alert_codes.size)
            assertTrue(successfulResponse.alert_codes.contains(123))
            assertTrue(successfulResponse.alert_codes.contains(300))
            assertEquals(eventRequestBody.userId, successfulResponse.user_id)
        }
    }
}
