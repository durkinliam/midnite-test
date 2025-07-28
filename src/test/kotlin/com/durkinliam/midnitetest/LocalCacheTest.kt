package com.durkinliam.midnitetest

import com.durkinliam.midnitetest.domain.customer.CustomerEvent
import com.durkinliam.midnitetest.domain.customer.CustomerRecord
import com.durkinliam.midnitetest.domain.event.request.EventRequestBody
import com.durkinliam.midnitetest.domain.event.request.EventType.DEPOSIT
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull

class LocalCacheTest {

    private lateinit var cache: LocalCache

    @BeforeEach
    fun setUp() {
        cache = LocalCache()
    }

    val eventRequestBody = EventRequestBody(
        userId = 123,
        type = DEPOSIT,
        amount = "100.0",
        timeRequestReceivedInMillis = 1633072800000
    )

    @Test
    fun `upsertRecord creates a new entry into the cache if the userId isn't already present`(){
        cache.upsertRecord(eventRequestBody)

        val customerRecord = cache.localCache[eventRequestBody.userId]
        assertNotNull(customerRecord)
        assertEquals(1, customerRecord.customerEvents.size)
    }

    @Test
    fun `upsertRecord adds a new CustomerEvent into an existing userId's CustomerRecord entry`(){
        cache.localCache[eventRequestBody.userId] = CustomerRecord(
            customerEvents = setOf(
                CustomerEvent(
                    type = DEPOSIT,
                    amount = "10.0",
                    timestamp = 1
                )
            )
        )

        cache.upsertRecord(eventRequestBody)

        val customerRecord = cache.localCache[eventRequestBody.userId]
        assertNotNull(customerRecord)
        assertEquals(2, customerRecord.customerEvents.size)
        assertEquals(
            CustomerEvent(
                type = DEPOSIT,
                amount = "100.0",
                timestamp = eventRequestBody.timeRequestReceivedInMillis
            ),
            customerRecord.customerEvents.maxByOrNull { it.timestamp }!!
        )
    }
}
