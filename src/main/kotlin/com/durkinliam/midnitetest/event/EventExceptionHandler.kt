package com.durkinliam.midnitetest.event

import com.durkinliam.midnitetest.domain.event.request.EventRequestTimestampNotLaterThanLatestRecordException
import com.durkinliam.midnitetest.domain.event.response.UnsuccessfulEventAlertResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class EventExceptionHandler {

    @ExceptionHandler(EventRequestTimestampNotLaterThanLatestRecordException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun onEventRequestTimestampEarlierThanLastEvent() = UnsuccessfulEventAlertResponse
}
