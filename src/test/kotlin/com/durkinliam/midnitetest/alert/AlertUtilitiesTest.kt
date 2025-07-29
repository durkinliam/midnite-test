package com.durkinliam.midnitetest.alert

import com.durkinliam.midnitetest.alert.AlertUtilities.accumulationOverATimePeriod
import com.durkinliam.midnitetest.alert.AlertUtilities.isMoreThanThreshold
import com.durkinliam.midnitetest.alert.AlertUtilities.noAlertResponse
import com.durkinliam.midnitetest.domain.customer.CustomerEvent
import com.durkinliam.midnitetest.domain.event.request.EventType.DEPOSIT
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.random.Random
import kotlin.test.assertFalse

class AlertUtilitiesTest {
    @Test
    fun noAlertResponseShouldReturnTheCorrectValues() {
        val userId = Random.nextLong()
        val response = noAlertResponse(userId)

        assertFalse(response.alert)
        assertTrue(response.alert_codes.isEmpty())
        assertEquals(userId, response.user_id)
    }

    @Test
    fun accumulationOverATimePeriodShouldReturnCorrectSumForEventsWithinTheTimePeriodAndIgnoresEventsOutsideOfTheTimePeriod() {
        val events =
            listOf(
                CustomerEvent(timestamp = 10_000, amount = 100.00, type = DEPOSIT),
                CustomerEvent(timestamp = 13_000, amount = 30.00, type = DEPOSIT),
                CustomerEvent(timestamp = 18_000, amount = 20.00, type = DEPOSIT),
                CustomerEvent(timestamp = 21_000, amount = 50.00, type = DEPOSIT),
            )
        val result = events.accumulationOverATimePeriod(10_000)

        assertEquals(100.0, result)
    }

    @Test
    fun isMoreThanThresholdShouldReturnTrueWhenTheAmountExceedsThreshold() {
        val amount = 200.0
        val threshold = 100.0

        assertTrue(amount.isMoreThanThreshold(threshold))
    }

    @Test
    fun isMoreThanThresholdShouldReturnFalseWhenTheAmountDoeNotExceedThreshold() {
        val amount = 100.0
        val threshold = 500.0

        assertFalse(amount.isMoreThanThreshold(threshold))
    }
}
