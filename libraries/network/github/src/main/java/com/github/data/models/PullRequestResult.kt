package com.github.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PullRequestResult(

    @Json(name = "id") val id: String,
    @Json(name = "state") val state: String,
    @Json(name = "title") val title: String,
    @Json(name = "user") val user: User,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "closed_at") val closedAt: String,

    ) {
    @JsonClass(generateAdapter = true)
    data class User(
        @Json(name = "avatar_url") val avatarUrl: String,
        @Json(name = "login") val login: String,
    )
}