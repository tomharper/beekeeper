package com.beekeeper.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Apiary(
    val id: String,
    val name: String,
    val location: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val hiveCount: Int,
    val status: ApiaryStatus
)

enum class ApiaryStatus {
    HEALTHY,
    WARNING,
    ALERT
}
