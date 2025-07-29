package com.durkinliam.midnitetest.event

import com.durkinliam.midnitetest.domain.event.request.EventRequestBody
import com.durkinliam.midnitetest.domain.event.request.exception.EventRequestTimestampNotLaterThanLatestRecordException
import com.durkinliam.midnitetest.domain.event.request.exception.EventRequestUnknownErrorException
import com.durkinliam.midnitetest.domain.event.response.UnsuccessfulEventAlertResponse
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class EventController(
    private val eventService: EventService
) {
    @PostMapping("/event", consumes = ["application/json"])
    fun handleEvent(@RequestBody eventRequest: String) = try {
        val eventRequestBody = json.decodeFromString<EventRequestBody>(eventRequest)

        ResponseEntity.ok(eventService.handleEventRequest(eventRequestBody))
    } catch (e: EventRequestTimestampNotLaterThanLatestRecordException) {
        ResponseEntity.badRequest()
            .body(
                UnsuccessfulEventAlertResponse(
                    user_id = e.userId,
                    reason = "Event request timestamp is earlier than the last event for userId: ${e.userId}. All events must be increasing in timestamp order and unique per user."
                )
            )
    } catch (_: SerializationException) {
        // Because the request body is not valid JSON, the user_id cannot be passed into the response and will appear as null.
        ResponseEntity
            .badRequest()
            .body(
                UnsuccessfulEventAlertResponse(
                    reason = "Invalid event request made to /event endpoint. Please ensure the request body is valid JSON and matches the expected structure."
                )
            )
    } catch (e: EventRequestUnknownErrorException) {
        ResponseEntity
            .internalServerError()
            .body(
                UnsuccessfulEventAlertResponse(
                    user_id = e.userId,
                    reason = "An unexpected error occurred while processing the event request. Please try again later."
                )
            )
    }

    companion object {
        private val json = Json
    }
}
