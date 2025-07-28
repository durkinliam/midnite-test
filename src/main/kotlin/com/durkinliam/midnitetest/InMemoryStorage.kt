package com.durkinliam.midnitetest

import com.durkinliam.midnitetest.domain.customer.CustomerEvent
import com.durkinliam.midnitetest.domain.customer.CustomerRecord
import com.durkinliam.midnitetest.domain.event.request.EventRequestBody
import org.springframework.stereotype.Component

@Component
class InMemoryStorage {
    val cache = HashMap<Long, CustomerRecord>()

    fun upsertRecord(eventRequestBody: EventRequestBody) {
        val userId = eventRequestBody.userId
        val existingCustomerRecords = getExistingRecordIfPresent(userId)

        if (existingCustomerRecords != null) {
            updateRecord(
                userId = userId,
                existingRecord = existingCustomerRecords,
                newEvent = eventRequestBody.toNewCustomerEvent()
            )
        } else {
            createNewRecord(
                userId = userId,
                customerRecord = eventRequestBody.toNewCustomerRecord()
            )
        }
    }

    @Synchronized
    private fun getExistingRecordIfPresent(userId: Long): CustomerRecord? {
        return cache[userId]
    }

    @Synchronized
    private fun createNewRecord(userId: Long, customerRecord: CustomerRecord) =
        cache.put(userId, customerRecord)

    @Synchronized
    private fun updateRecord(userId: Long, existingRecord: CustomerRecord, newEvent: CustomerEvent) {
        val existingEvents = existingRecord.customerEvents

        cache[userId] = CustomerRecord(
            customerEvents = existingEvents + newEvent
        )
    }

    private fun EventRequestBody.toNewCustomerRecord() = CustomerRecord(
        customerEvents = setOf(this.toNewCustomerEvent())
    )

    private fun EventRequestBody.toNewCustomerEvent() = CustomerEvent(
        type = this.type,
        amount = this.amount.toDouble(),
        timestamp = this.timeRequestReceivedInMillis,
    )
}