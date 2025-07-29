package com.durkinliam.midnitetest.event

import com.durkinliam.midnitetest.domain.event.request.exception.EventRequestTimestampNotLaterThanLatestRecordException
import com.durkinliam.midnitetest.domain.event.request.exception.EventRequestUnknownErrorException
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

    @ExceptionHandler(EventRequestUnknownErrorException::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun onUnknownError() = UnsuccessfulEventAlertResponse
}
