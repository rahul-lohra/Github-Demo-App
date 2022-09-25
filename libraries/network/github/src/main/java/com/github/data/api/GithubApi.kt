package com.github.data.api

import com.github.data.models.PullRequestResult
import com.github.data.models.PullRequestState
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GithubApi {

    object Config {
        const val BASE_URL = "https://api.github.com/repos/"
    }

    @GET("/repos/{owner}/{repo}/pulls")
    suspend fun getPullRequest(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @PullRequestState
        @Query("state") state: String,
        @Query("per_page") @androidx.annotation.IntRange(from = 0, to = 100) perPage: Int,
        @Query("page") page: Int
    ): List<PullRequestResult>
}