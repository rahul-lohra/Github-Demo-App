package com.github.data.models

import retrofit2.http.Path
import retrofit2.http.Query

data class GithubRequestParams(
    @Path("owner") val owner: String,
    @Path("repo") val repo: String,
    @PullRequestState
    @Query("state") val state: String,
    @Query("per_page") @androidx.annotation.IntRange(from = 0, to = 100) val perPage: Int,
    @Query("page") var page: Int
)