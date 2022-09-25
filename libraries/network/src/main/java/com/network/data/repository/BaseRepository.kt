package com.network.data.repository

import com.network.data.ApiResult
import com.github.data.models.GithubRequestParams
import com.github.data.models.PullRequestResult

interface BaseRepository {
    suspend fun getPullRequest(requestParams: GithubRequestParams): ApiResult<List<PullRequestResult>>

    suspend fun <T> invoke(method: suspend () -> T): ApiResult<T> {
        try {
            return ApiResult.Success(method.invoke())
        } catch (th: Throwable) {
            return ApiResult.Error(th)
        }
    }
}