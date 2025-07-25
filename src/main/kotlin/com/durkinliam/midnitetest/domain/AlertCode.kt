package com.durkinliam.midnitetest.domain

import kotlinx.serialization.Serializable

@Serializable
enum class AlertCode(val code: Int) {
    WITHDRAWAL_OVER_ONE_HUNDRED(1100),
    THREE_CONSECUTIVE_WITHDRAWALS(30),
    THREE_CONSECUTIVE_INCREASING_DEPOSITS(300),
    ACCUMULATIVE_DEPOSIT_AMOUNT_OVER_TWO_HUNDRED_WITHIN_THIRTY_SECONDS(123),
}
