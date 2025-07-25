package com.durkinliam.midnitetest.domain

import kotlinx.serialization.Serializable

@Serializable
data class CustomerRecord(
    val customerEvents: Set<CustomerEvent>,
)
