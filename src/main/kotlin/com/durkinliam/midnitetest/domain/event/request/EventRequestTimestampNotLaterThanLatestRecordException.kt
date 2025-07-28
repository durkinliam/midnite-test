package com.durkinliam.midnitetest.domain.event.request

class EventRequestTimestampNotLaterThanLatestRecordException(val userId: Long): IllegalArgumentException()
