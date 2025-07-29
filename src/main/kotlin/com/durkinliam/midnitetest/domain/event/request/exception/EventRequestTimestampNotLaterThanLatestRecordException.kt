package com.durkinliam.midnitetest.domain.event.request.exception

class EventRequestTimestampNotLaterThanLatestRecordException(
    val userId: Long,
) : IllegalArgumentException()
