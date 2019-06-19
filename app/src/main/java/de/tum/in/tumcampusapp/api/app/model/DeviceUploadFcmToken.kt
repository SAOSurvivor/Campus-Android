package de.tum.`in`.tumcampusapp.api.app.model

data class DeviceUploadFcmToken(
    val verification: TumCabeVerification,
    val token: String,
    val signature: String
)
