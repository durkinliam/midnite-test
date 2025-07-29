package com.durkinliam.midnitetest.event

import com.durkinliam.midnitetest.alert.AlertUtilities.noAlertResponse
import com.durkinliam.midnitetest.domain.event.request.exception.EventRequestTimestampNotLaterThanLatestRecordException
import com.durkinliam.midnitetest.domain.event.request.exception.EventRequestUnknownErrorException
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
            every { eventService.handleEventRequest(any()) } throws
                EventRequestTimestampNotLaterThanLatestRecordException(
                    userId,
                )

            val response =
                eventController.handleEvent("""{"user_id": $userId, "type": "deposit", "amount": "42.00", "t": 10}""")

            val expectedResponseBody =
                UnsuccessfulEventAlertResponse(
                    user_id = userId,
                    reason =
                        "Event request timestamp is earlier than the last event for userId: $userId. " +
                            "All events must be increasing in timestamp order and unique per user.",
                )

            assertEquals(ResponseEntity.badRequest().body(expectedResponseBody).statusCode, response.statusCode)

            assertTrue(response.body is UnsuccessfulEventAlertResponse)

            val unsuccessfulEventAlertResponseBody = response.body as UnsuccessfulEventAlertResponse

            assertEquals(expectedResponseBody.user_id, unsuccessfulEventAlertResponseBody.user_id)
            assertEquals(expectedResponseBody.reason, unsuccessfulEventAlertResponseBody.reason)
        }

        @Nested
        inner class ForARequestWhichThrowsASerializationException {
            @Test
            fun `returns a BadRequest-400 response with an UnsuccessfulEventAlertResponse`() {
                val response = eventController.handleEvent("invalid json")
                val expectedResponseBody =
                    UnsuccessfulEventAlertResponse(
                        user_id = null,
                        reason =
                            "Invalid event request made to /event endpoint. " +
                                "Please ensure the request body is valid JSON and matches the expected structure.",
                    )

                assertEquals(ResponseEntity.badRequest().body(expectedResponseBody).statusCode, response.statusCode)

                assertTrue(response.body is UnsuccessfulEventAlertResponse)

                val unsuccessfulEventAlertResponseBody = response.body as UnsuccessfulEventAlertResponse

                assertEquals(expectedResponseBody.user_id, unsuccessfulEventAlertResponseBody.user_id)
                assertEquals(expectedResponseBody.reason, unsuccessfulEventAlertResponseBody.reason)
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
        every { eventService.handleEventRequest(any()) } throws EventRequestUnknownErrorException(userId)

        val response = eventController.handleEvent(aValidRequestBody("deposit"))

        val expectedResponseBody =
            UnsuccessfulEventAlertResponse(
                user_id = userId,
                reason = "An unexpected error occurred while processing the event request. Please try again later.",
            )

        assertEquals(ResponseEntity.internalServerError().body(expectedResponseBody).statusCode, response.statusCode)

        assertTrue(response.body is UnsuccessfulEventAlertResponse)

        val unsuccessfulEventAlertResponseBody = response.body as UnsuccessfulEventAlertResponse

        assertEquals(expectedResponseBody.user_id, unsuccessfulEventAlertResponseBody.user_id)
        assertEquals(expectedResponseBody.reason, unsuccessfulEventAlertResponseBody.reason)
    }

    private companion object {
        val userId = Random.nextLong()

        private fun aValidRequestBody(eventType: String) = """{"user_id": $userId, "type": "$eventType", "amount": "42.00", "t": 10}"""
    }
}
