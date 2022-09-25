package com.network.data.datasource

import com.github.data.models.GithubRequestParams
import com.github.data.models.PullRequestResult

interface BaseRemoteSource {
    suspend fun getPullRequest(requestParams: GithubRequestParams): List<PullRequestResult>
}