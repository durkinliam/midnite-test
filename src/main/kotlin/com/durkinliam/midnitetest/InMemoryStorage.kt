package com.durkinliam.midnitetest

import com.durkinliam.midnitetest.domain.customer.CustomerEvent
import com.durkinliam.midnitetest.domain.customer.CustomerRecord
import com.durkinliam.midnitetest.domain.event.request.EventRequestBody
import org.springframework.stereotype.Component

@Component
class InMemoryStorage {
    /**
     * This is a simple in-memory cache to store CustomerRecord data per User.
     * A HashMap is not thread-safe, so @Synchronized is used to ensure that only one thread can access the cache at a time.
     * A HashMap also does ensure order of insertion, which is important for the order of customer events. The responsibility of ordering CustomerRecords is handled by the service layer.
     * In a production application, I would switch this class out for a database solution using Spring Boot and the @Repository annotation.
     */
    val cache = HashMap<Long, CustomerRecord>()

    fun upsertRecord(eventRequestBody: EventRequestBody) {
        val userId = eventRequestBody.userId
        val existingCustomerRecords = getExistingRecordIfPresent(userId)

        if (existingCustomerRecords != null) {
            updateRecord(
                userId = userId,
                existingRecord = existingCustomerRecords,
                newEvent = eventRequestBody.toNewCustomerEvent(),
            )
        } else {
            createNewRecord(
                userId = userId,
                customerRecord = eventRequestBody.toNewCustomerRecord(),
            )
        }
    }

    @Synchronized
    private fun getExistingRecordIfPresent(userId: Long): CustomerRecord? = cache[userId]

    @Synchronized
    private fun createNewRecord(
        userId: Long,
        customerRecord: CustomerRecord,
    ) = cache.put(userId, customerRecord)

    @Synchronized
    private fun updateRecord(
        userId: Long,
        existingRecord: CustomerRecord,
        newEvent: CustomerEvent,
    ) {
        val existingEvents = existingRecord.customerEvents

        cache[userId] =
            CustomerRecord(
                customerEvents = existingEvents + newEvent,
            )
    }

    private fun EventRequestBody.toNewCustomerRecord() =
        CustomerRecord(
            customerEvents = setOf(this.toNewCustomerEvent()),
        )

    private fun EventRequestBody.toNewCustomerEvent() =
        CustomerEvent(
            type = this.type,
            amount = this.amount.toDouble(),
            timestamp = this.timeRequestReceivedInMillis,
        )
}
