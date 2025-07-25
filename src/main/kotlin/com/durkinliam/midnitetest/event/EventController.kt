package com.durkinliam.midnitetest.event

import com.durkinliam.midnitetest.domain.EventRequestBody
import com.durkinliam.midnitetest.domain.EventResponseBody
import kotlinx.serialization.json.Json
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class EventController(
    private val eventService: EventService
) {
    @PostMapping("/event")
    fun handleEvent(@RequestBody eventRequest: String): ResponseEntity<EventResponseBody> {
        val eventRequestBody = json.decodeFromString<EventRequestBody>(eventRequest)
        val eventResponse = eventService.handleEventRequest(eventRequestBody)

        return ResponseEntity.ok().body(eventResponse)
    }

    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
            prettyPrint = true
        }
    }
}
