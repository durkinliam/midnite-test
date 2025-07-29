package com.durkinliam.midnitetest.domain.customer

import kotlinx.serialization.Serializable

@Serializable
data class CustomerRecord(
    val customerEvents: Set<CustomerEvent>,
)
