package com.durkinliam.midnitetest

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class EventIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Nested
    inner class AValidPostRequest {
        @ParameterizedTest
        @ValueSource(
            strings = [
                "validRequest/deposit/UserOne_Deposit_TenPounds_ZeroSeconds.json",
                "validRequest/withdrawal/UserOne_Withdrawal_TenPounds_ZeroSeconds.json",
            ],
        )
        fun `should return 200 status code and a SuccessfulEventAlertResponse body with an empty alert_code array for either eventType`(
            validJsonFilename: String,
        ) {
            val requestBody =
                javaClass
                    .getResourceAsStream("/$validJsonFilename")!!
                    .bufferedReader()
                    .use { it.readText() }

            mockMvc
                .perform(
                    MockMvcRequestBuilders
                        .post("/event")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody),
                ).andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(
                    MockMvcResultMatchers
                        .content()
                        .json(
                            """
                            {
                            "alert": false,
                            "alert_codes": [],
                            "user_id": 1
                            }
                            """.trimIndent(),
                        ),
                )
        }

        @Nested
        inner class HandlesDepositsCorrectly {
            @Test
            fun `should return 200 status code and a SuccessfulEventAlertResponse body with an alert_code array containing 300`() {
                val requestFiles =
                    listOf(
                        "validRequest/deposit/UserOne_Deposit_TenPounds_ZeroSeconds.json",
                        "validRequest/deposit/UserOne_Deposit_TwentyPounds_TenSeconds.json",
                        "validRequest/deposit/UserOne_Deposit_ThirtyPounds_TwentySeconds.json",
                    )

                val responses =
                    requestFiles.map { filename ->
                        val requestBody =
                            javaClass
                                .getResourceAsStream("/$filename")!!
                                .bufferedReader()
                                .use { it.readText() }
                        mockMvc
                            .perform(
                                MockMvcRequestBuilders
                                    .post("/event")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(requestBody),
                            ).andReturn()
                    }

                assertEquals(3, responses.size)

                val firstResponse = responses[0].response

                assertEquals(200, firstResponse.status)
                assertEquals("application/json", firstResponse.contentType)
                assertEquals(
                    """{"alert":false,"alert_codes":[],"user_id":1}""".trimIndent(),
                    firstResponse.contentAsString,
                )

                val secondResponse = responses[1].response

                assertEquals(200, secondResponse.status)
                assertEquals("application/json", secondResponse.contentType)
                assertEquals(
                    """{"alert":false,"alert_codes":[],"user_id":1}""".trimIndent(),
                    secondResponse.contentAsString,
                )

                val thirdResponse = responses[2].response

                assertEquals(200, thirdResponse.status)
                assertEquals("application/json", thirdResponse.contentType)
                assertEquals(
                    """{"alert":true,"alert_codes":[300],"user_id":1}""".trimIndent(),
                    thirdResponse.contentAsString,
                )
            }

            @Test
            fun `should return 200 status code and a SuccessfulEventAlertResponse body with an alert_code array containing 123`() {
                val requestFiles =
                    listOf(
                        "validRequest/deposit/UserOne_Deposit_TenPounds_ZeroSeconds.json",
                        "validRequest/deposit/UserOne_Deposit_TenPounds_TenSeconds.json",
                        "validRequest/deposit/UserOne_Deposit_FiveHundredPounds_TwentyFiveSeconds.json",
                    )

                val responses =
                    requestFiles.map { filename ->
                        val requestBody =
                            javaClass
                                .getResourceAsStream("/$filename")!!
                                .bufferedReader()
                                .use { it.readText() }
                        mockMvc
                            .perform(
                                MockMvcRequestBuilders
                                    .post("/event")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(requestBody),
                            ).andReturn()
                    }

                assertEquals(3, responses.size)

                val firstResponse = responses[0].response

                assertEquals(200, firstResponse.status)
                assertEquals("application/json", firstResponse.contentType)
                assertEquals(
                    """{"alert":false,"alert_codes":[],"user_id":1}""".trimIndent(),
                    firstResponse.contentAsString,
                )

                val secondResponse = responses[1].response

                assertEquals(200, secondResponse.status)
                assertEquals("application/json", secondResponse.contentType)
                assertEquals(
                    """{"alert":false,"alert_codes":[],"user_id":1}""".trimIndent(),
                    secondResponse.contentAsString,
                )

                val thirdResponse = responses[2].response

                assertEquals(200, thirdResponse.status)
                assertEquals("application/json", thirdResponse.contentType)
                assertEquals(
                    """{"alert":true,"alert_codes":[123],"user_id":1}""".trimIndent(),
                    thirdResponse.contentAsString,
                )
            }

            @Test
            fun `should return 200 status code and a SuccessfulEventAlertResponse body with an alert_code array containing 300 and 123`() {
                val requestFiles =
                    listOf(
                        "validRequest/deposit/UserOne_Deposit_TenPounds_ZeroSeconds.json",
                        "validRequest/deposit/UserOne_Deposit_TwentyPounds_TenSeconds.json",
                        "validRequest/deposit/UserOne_Deposit_FiveHundredPounds_TwentyFiveSeconds.json",
                    )

                val responses =
                    requestFiles.map { filename ->
                        val requestBody =
                            javaClass
                                .getResourceAsStream("/$filename")!!
                                .bufferedReader()
                                .use { it.readText() }
                        mockMvc
                            .perform(
                                MockMvcRequestBuilders
                                    .post("/event")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(requestBody),
                            ).andReturn()
                    }

                assertEquals(3, responses.size)

                val firstResponse = responses[0].response

                assertEquals(200, firstResponse.status)
                assertEquals("application/json", firstResponse.contentType)
                assertEquals(
                    """{"alert":false,"alert_codes":[],"user_id":1}""".trimIndent(),
                    firstResponse.contentAsString,
                )

                val secondResponse = responses[1].response

                assertEquals(200, secondResponse.status)
                assertEquals("application/json", secondResponse.contentType)
                assertEquals(
                    """{"alert":false,"alert_codes":[],"user_id":1}""".trimIndent(),
                    secondResponse.contentAsString,
                )

                val thirdResponse = responses[2].response

                assertEquals(200, thirdResponse.status)
                assertEquals("application/json", thirdResponse.contentType)
                assertEquals(
                    """{"alert":true,"alert_codes":[300,123],"user_id":1}""".trimIndent(),
                    thirdResponse.contentAsString,
                )
            }
        }

        @Nested
        inner class HandlesWithdrawalsCorrectly {
            @Test
            fun `should return 200 status code and a SuccessfulEventAlertResponse body with an alert_code array containing 1100`() {
                val requestBody =
                    javaClass
                        .getResourceAsStream("/validRequest/withdrawal/UserOne_Withdrawal_TwoHundredPounds_FiveSeconds.json")!!
                        .bufferedReader()
                        .use { it.readText() }

                mockMvc
                    .perform(
                        MockMvcRequestBuilders
                            .post("/event")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody),
                    ).andExpect(MockMvcResultMatchers.status().isOk)
                    .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(
                        MockMvcResultMatchers.content().json(
                            """
                            {
                            "alert": true,
                            "alert_codes": [1100],
                            "user_id": 1
                            }
                            """.trimIndent(),
                        ),
                    )
            }

            @Test
            fun `should return 200 status code and a SuccessfulEventAlertResponse body with an alert_code array containing 30`() {
                val requestFiles =
                    listOf(
                        "validRequest/withdrawal/UserOne_Withdrawal_TenPounds_ZeroSeconds.json",
                        "validRequest/withdrawal/UserOne_Withdrawal_TenPounds_TenSeconds.json",
                        "validRequest/withdrawal/UserOne_Withdrawal_TenPounds_FiftySeconds.json",
                    )

                val responses =
                    requestFiles.map { filename ->
                        val requestBody =
                            javaClass
                                .getResourceAsStream("/$filename")!!
                                .bufferedReader()
                                .use { it.readText() }
                        mockMvc
                            .perform(
                                MockMvcRequestBuilders
                                    .post("/event")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(requestBody),
                            ).andReturn()
                    }

                assertEquals(3, responses.size)

                val firstResponse = responses[0].response

                assertEquals(200, firstResponse.status)
                assertEquals("application/json", firstResponse.contentType)
                assertEquals(
                    """{"alert":false,"alert_codes":[],"user_id":1}""".trimIndent(),
                    firstResponse.contentAsString,
                )

                val secondResponse = responses[1].response

                assertEquals(200, secondResponse.status)
                assertEquals("application/json", secondResponse.contentType)
                assertEquals(
                    """{"alert":false,"alert_codes":[],"user_id":1}""".trimIndent(),
                    secondResponse.contentAsString,
                )

                val thirdResponse = responses[2].response

                assertEquals(200, thirdResponse.status)
                assertEquals("application/json", thirdResponse.contentType)
                assertEquals(
                    """{"alert":true,"alert_codes":[30],"user_id":1}""".trimIndent(),
                    thirdResponse.contentAsString,
                )
            }

            @Test
            fun `should return 200 status code and a SuccessfulEventAlertResponse body with an alert_code array containing both 1100 and 30`() {
                val requestFiles =
                    listOf(
                        "validRequest/withdrawal/UserOne_Withdrawal_TenPounds_ZeroSeconds.json",
                        "validRequest/withdrawal/UserOne_Withdrawal_TenPounds_TenSeconds.json",
                        "validRequest/withdrawal/UserOne_Withdrawal_TwoHundredPounds_ElevenSeconds.json",
                    )

                val responses =
                    requestFiles.map { filename ->
                        val requestBody =
                            javaClass
                                .getResourceAsStream("/$filename")!!
                                .bufferedReader()
                                .use { it.readText() }
                        mockMvc
                            .perform(
                                MockMvcRequestBuilders
                                    .post("/event")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(requestBody),
                            ).andReturn()
                    }

                assertEquals(3, responses.size)

                val firstResponse = responses[0].response

                assertEquals(200, firstResponse.status)
                assertEquals("application/json", firstResponse.contentType)
                assertEquals(
                    """{"alert":false,"alert_codes":[],"user_id":1}""".trimIndent(),
                    firstResponse.contentAsString,
                )

                val secondResponse = responses[1].response

                assertEquals(200, secondResponse.status)
                assertEquals("application/json", secondResponse.contentType)
                assertEquals(
                    """{"alert":false,"alert_codes":[],"user_id":1}""".trimIndent(),
                    secondResponse.contentAsString,
                )

                val thirdResponse = responses[2].response

                assertEquals(200, thirdResponse.status)
                assertEquals("application/json", thirdResponse.contentType)
                assertEquals(
                    """{"alert":true,"alert_codes":[1100,30],"user_id":1}""".trimIndent(),
                    thirdResponse.contentAsString,
                )
            }
        }
    }

    @Nested
    inner class AnInvalidPostRequest {
        @Test
        fun `should return 400 status code and an UnsuccessfulEventAlertResponse body with a reason for invalid JSON`() {
            val incomingRequest = """{"invalid":"json"}""".trimIndent()

            mockMvc
                .perform(
                    MockMvcRequestBuilders
                        .post("/event")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(incomingRequest),
                ).andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(
                    MockMvcResultMatchers.content().json(
                        """
                        {
                        "user_id": null,
                        "reason": "Invalid event request made to /event endpoint. Please ensure the request body is valid JSON and matches the expected structure."}
                        """.trimIndent(),
                    ),
                )
        }

        @Test
        fun `should return 400 status code and an UnsuccessfulEventAlertResponse body with a reason for timestamp earlier than latest record`() {
            val requestFiles =
                listOf(
                    "validRequest/withdrawal/UserOne_Withdrawal_TwoHundredPounds_ElevenSeconds.json",
                    "validRequest/deposit/UserOne_Deposit_TwentyPounds_TenSeconds.json",
                )

            val responses =
                requestFiles.map { filename ->
                    val requestBody =
                        javaClass
                            .getResourceAsStream("/$filename")!!
                            .bufferedReader()
                            .use { it.readText() }
                    mockMvc
                        .perform(
                            MockMvcRequestBuilders
                                .post("/event")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody),
                        ).andReturn()
                }

            assertEquals(2, responses.size)

            val firstResponse = responses[0].response

            assertEquals(200, firstResponse.status)
            assertEquals("application/json", firstResponse.contentType)
            assertEquals(
                """{"alert":true,"alert_codes":[1100],"user_id":1}""".trimIndent(),
                firstResponse.contentAsString,
            )

            val secondResponse = responses[1].response

            assertEquals(400, secondResponse.status)
            assertEquals("application/json", secondResponse.contentType)
            assertEquals(
                """{"user_id":1,"reason":"Event request timestamp is earlier than the last event for userId: 1. All events must be increasing in timestamp order and unique per user."}"""
                    .trimIndent(),
                secondResponse.contentAsString,
            )
        }
    }
}
