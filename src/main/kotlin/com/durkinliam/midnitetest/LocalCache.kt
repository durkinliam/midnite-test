package com.durkinliam.midnitetest

import com.durkinliam.midnitetest.domain.CustomerEvent
import com.durkinliam.midnitetest.domain.CustomerRecord
import com.durkinliam.midnitetest.domain.EventRequestBody
import org.springframework.stereotype.Component

@Component
class LocalCache {
    val localCache = HashMap<Long, CustomerRecord>()

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
        return localCache[userId]
    }

    @Synchronized
    private fun createNewRecord(userId: Long, customerRecord: CustomerRecord) =
        localCache.put(userId, customerRecord)

    @Synchronized
    private fun updateRecord(userId: Long, existingRecord: CustomerRecord, newEvent: CustomerEvent) {
        val existingEvents = existingRecord.customerEvents

        localCache[userId] = CustomerRecord(
            customerEvents = existingEvents + newEvent
        )
    }

    private fun EventRequestBody.toNewCustomerRecord() = CustomerRecord(
        customerEvents = setOf(this.toNewCustomerEvent())
    )

    private fun EventRequestBody.toNewCustomerEvent() = CustomerEvent(
        type = this.type,
        amount = this.amount,
        timestamp = this.timeRequestReceivedInMillis,
    )
}