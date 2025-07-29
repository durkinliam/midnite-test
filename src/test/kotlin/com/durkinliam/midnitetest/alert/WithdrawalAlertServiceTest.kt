package com.durkinliam.midnitetest.alert

import com.durkinliam.midnitetest.InMemoryStorage
import com.durkinliam.midnitetest.domain.customer.CustomerEvent
import com.durkinliam.midnitetest.domain.customer.CustomerRecord
import com.durkinliam.midnitetest.domain.event.request.EventRequestBody
import com.durkinliam.midnitetest.domain.event.request.EventType.DEPOSIT
import com.durkinliam.midnitetest.domain.event.request.EventType.WITHDRAWAL
import com.durkinliam.midnitetest.domain.event.response.SuccessfulEventAlertResponse
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class WithdrawalAlertServiceTest {
    private val cache = mockk<InMemoryStorage>(relaxed = true)
    private val withdrawalAlertService = WithdrawalAlertService(cache)

    val eventRequestBody =
        EventRequestBody(
            userId = 123,
            type = WITHDRAWAL,
            amount = "10.00",
            timeRequestReceivedInMillis = 100,
        )

    @BeforeEach
    fun setUp() {
        every { cache.upsertRecord(any()) } returns Unit
    }

    @Test
    fun `when a user has less than three withdrawals and the new event is not a withdrawal over 100, should return a SuccessfulEventAlertResponse with no alerts`() {
        every { cache.cache[eventRequestBody.userId] } returns
            CustomerRecord(
                customerEvents =
                    setOf(
                        CustomerEvent(
                            type = WITHDRAWAL,
                            amount = eventRequestBody.amount.toDouble(),
                            timestamp = eventRequestBody.timeRequestReceivedInMillis,
                        ),
                    ),
            )

        val response = withdrawalAlertService.handle(eventRequestBody)

        assertTrue(response is SuccessfulEventAlertResponse)

        val successfulResponse = response as SuccessfulEventAlertResponse

        assertTrue(response.alert_codes.isEmpty())
        assertFalse(response.alert)
        assertEquals(eventRequestBody.userId, successfulResponse.user_id)
    }

    @Test
    fun `when a user has less than three withdrawals and the new event is a withdrawal over 100, should return a SuccessfulEventAlertResponse with an AlertCode of 1100`() {
        every { cache.cache[eventRequestBody.userId] } returns
            CustomerRecord(
                customerEvents =
                    setOf(
                        CustomerEvent(
                            type = WITHDRAWAL,
                            amount = eventRequestBody.amount.toDouble(),
                            timestamp = eventRequestBody.timeRequestReceivedInMillis - 1,
                        ),
                    ),
            )

        val response = withdrawalAlertService.handle(eventRequestBody.copy(amount = "110.00"))

        assertTrue(response is SuccessfulEventAlertResponse)

        val successfulResponse = response as SuccessfulEventAlertResponse

        assertEquals(1, response.alert_codes.size)
        assertTrue(response.alert_codes.contains(1100))
        assertTrue(response.alert)
        assertEquals(eventRequestBody.userId, successfulResponse.user_id)
    }

    @Nested
    inner class WhenAUserHasAtLeastThreePreviousEvents {
        @Test
        fun `where all three events being withdrawals and the new event is not a withdrawal over 100, should return a SuccessfulEventAlertResponse with an AlertCode of 30`() {
            every { cache.cache[eventRequestBody.userId] } returns
                CustomerRecord(
                    customerEvents =
                        setOf(
                            CustomerEvent(
                                type = WITHDRAWAL,
                                amount = eventRequestBody.amount.toDouble(),
                                timestamp = eventRequestBody.timeRequestReceivedInMillis - 3,
                            ),
                            CustomerEvent(
                                type = WITHDRAWAL,
                                amount = eventRequestBody.amount.toDouble(),
                                timestamp = eventRequestBody.timeRequestReceivedInMillis - 2,
                            ),
                            CustomerEvent(
                                type = WITHDRAWAL,
                                amount = eventRequestBody.amount.toDouble(),
                                timestamp = eventRequestBody.timeRequestReceivedInMillis - 1,
                            ),
                        ),
                )

            val response = withdrawalAlertService.handle(eventRequestBody)

            assertTrue(response is SuccessfulEventAlertResponse)

            val successfulResponse = response as SuccessfulEventAlertResponse

            assertEquals(1, response.alert_codes.size)
            assertTrue(response.alert_codes.contains(30))
            assertTrue(response.alert)
            assertEquals(eventRequestBody.userId, successfulResponse.user_id)
        }

        @Test
        fun `but not all previous events are withdrawals and the new event is a withdrawal over 100, should return a SuccessfulEventAlertResponse with an AlertCode of 1100`() {
            every { cache.cache[eventRequestBody.userId] } returns
                CustomerRecord(
                    customerEvents =
                        setOf(
                            CustomerEvent(
                                type = WITHDRAWAL,
                                amount = eventRequestBody.amount.toDouble(),
                                timestamp = eventRequestBody.timeRequestReceivedInMillis - 3,
                            ),
                            CustomerEvent(
                                type = DEPOSIT,
                                amount = eventRequestBody.amount.toDouble(),
                                timestamp = eventRequestBody.timeRequestReceivedInMillis - 2,
                            ),
                            CustomerEvent(
                                type = WITHDRAWAL,
                                amount = eventRequestBody.amount.toDouble() + 100.00,
                                timestamp = eventRequestBody.timeRequestReceivedInMillis - 1,
                            ),
                        ),
                )

            val response = withdrawalAlertService.handle(eventRequestBody.copy(amount = "110.00"))

            assertTrue(response is SuccessfulEventAlertResponse)

            val successfulResponse = response as SuccessfulEventAlertResponse

            assertEquals(1, response.alert_codes.size)
            assertTrue(response.alert_codes.contains(1100))
            assertTrue(response.alert)
            assertEquals(eventRequestBody.userId, successfulResponse.user_id)
        }

        @Test
        fun `where all three events being withdrawals and the new event is a withdrawal over 100, should return a SuccessfulEventAlertResponse with an AlertCodes of 1100 and 30`() {
            every { cache.cache[eventRequestBody.userId] } returns
                CustomerRecord(
                    customerEvents =
                        setOf(
                            CustomerEvent(
                                type = WITHDRAWAL,
                                amount = eventRequestBody.amount.toDouble(),
                                timestamp = eventRequestBody.timeRequestReceivedInMillis - 3,
                            ),
                            CustomerEvent(
                                type = WITHDRAWAL,
                                amount = eventRequestBody.amount.toDouble(),
                                timestamp = eventRequestBody.timeRequestReceivedInMillis - 2,
                            ),
                            CustomerEvent(
                                type = WITHDRAWAL,
                                amount = eventRequestBody.amount.toDouble(),
                                timestamp = eventRequestBody.timeRequestReceivedInMillis - 1,
                            ),
                        ),
                )

            val response = withdrawalAlertService.handle(eventRequestBody.copy(amount = "110.00"))

            assertTrue(response is SuccessfulEventAlertResponse)

            val successfulResponse = response as SuccessfulEventAlertResponse

            assertEquals(2, response.alert_codes.size)
            assertTrue(response.alert_codes.contains(1100))
            assertTrue(response.alert_codes.contains(30))
            assertTrue(response.alert)
            assertEquals(eventRequestBody.userId, successfulResponse.user_id)
        }
    }
}
