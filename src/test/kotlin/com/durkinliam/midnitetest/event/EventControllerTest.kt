package com.durkinliam.midnitetest.event

import com.durkinliam.midnitetest.alert.AlertUtilities.noAlertResponse
import com.durkinliam.midnitetest.domain.event.request.EventRequestTimestampNotLaterThanLatestRecordException
import com.durkinliam.midnitetest.domain.event.response.SuccessfulEventAlertResponse
import com.durkinliam.midnitetest.domain.event.response.UnsuccessfulEventAlertResponse
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.ResponseEntity
import kotlin.random.Random
import kotlin.test.assertEquals

class EventControllerTest {
    private val eventService: EventService = mockk()
    private val eventController = EventController(eventService)

    @Nested
    inner class AValidRequest {

        @BeforeEach
        fun setUp() {
            every { eventService.handleEventRequest(any()) } returns noAlertResponse(userId)
        }

        @Test
        fun `of deposit event type returns an OK-200 response with a SuccessfulEventAlertResponse body`() {
            val response =
                eventController.handleEvent(aValidRequestBody("deposit"))

            assertEquals(ResponseEntity.ok().body(noAlertResponse(userId)).statusCode, response.statusCode)
            assertTrue(response.body is SuccessfulEventAlertResponse)
        }

        @Test
        fun `of withdraw event type returns an OK-200 response with a SuccessfulEventAlertResponse body`() {
            val response =
                eventController.handleEvent(aValidRequestBody("withdraw"))

            assertEquals(ResponseEntity.ok().body(noAlertResponse(userId)).statusCode, response.statusCode)
            assertTrue(response.body is SuccessfulEventAlertResponse)
        }
    }

    @Nested
    inner class AnInvalidRequest {

        @Test
        fun `for a request which throws an EventRequestTimestampNotLaterThanLatestRecord exception returns a BadRequest-400 response with an UnsuccessfulEventAlertResponse body`() {
            every { eventService.handleEventRequest(any()) } throws EventRequestTimestampNotLaterThanLatestRecordException(
                userId
            )

            val response =
                eventController.handleEvent("""{"user_id": $userId, "type": "deposit", "amount": "42.00", "t": 10}""")

            assertEquals(ResponseEntity.badRequest().body(UnsuccessfulEventAlertResponse("test")).statusCode, response.statusCode)
            assertTrue(response.body is UnsuccessfulEventAlertResponse)
        }

        @Nested
        inner class ForARequestWhichThrowsASerializationException {
            @Test
            fun `returns a BadRequest-400 response with an UnsuccessfulEventAlertResponse`() {
                val response = eventController.handleEvent("invalid json")

                assertEquals(ResponseEntity.badRequest().body(UnsuccessfulEventAlertResponse("test")).statusCode, response.statusCode)
                assertTrue(response.body is UnsuccessfulEventAlertResponse)
            }

            @Test
            fun `does not call the EventService class`() {
                eventController.handleEvent("invalid json")

                verify(exactly = 0) { eventService.handleEventRequest(any()) }
            }
        }
    }

    @Test
    fun `when any other Exception is thrown, an InternalServerError-500 response is returned with an UnsuccessfulEventAlertResponse body`() {
        every { eventService.handleEventRequest(any()) } throws Exception("Unexpected error")

        val response = eventController.handleEvent(aValidRequestBody("deposit"))

        assertEquals(ResponseEntity.internalServerError().body(UnsuccessfulEventAlertResponse("test")).statusCode, response.statusCode)
        assertTrue(response.body is UnsuccessfulEventAlertResponse)
    }

    private companion object {
        val userId = Random.nextLong()
        private fun aValidRequestBody(eventType: String) =
            """{"user_id": $userId, "type": "deposit", "amount": "42.00", "t": 10}"""
    }
}
