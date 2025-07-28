package com.durkinliam.midnitetest.alert

import com.durkinliam.midnitetest.InMemoryStorage
import com.durkinliam.midnitetest.domain.event.request.EventRequestBody
import com.durkinliam.midnitetest.domain.event.request.EventType.WITHDRAWAL
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach

class WithdrawalAlertServiceTest {
    private val cache = mockk<InMemoryStorage>(relaxed = true)
    private val withdrawalAlertService = WithdrawalAlertService(cache)

    val eventRequestBody = EventRequestBody(
        userId = 123,
        type = WITHDRAWAL,
        amount = "10.00",
        timeRequestReceivedInMillis = 100
    )

    @BeforeEach
    fun setUp() {
        every { cache.upsertRecord(any()) } returns Unit
    }



}