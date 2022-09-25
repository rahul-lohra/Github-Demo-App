package com.network.data.datasource

import com.github.data.api.GithubApi
import com.github.data.models.GithubRequestParams
import com.github.data.models.PullRequestResult
import javax.inject.Inject

class RemoteDataSource @Inject constructor(private val api: GithubApi) : BaseRemoteSource {

    @kotlin.jvm.Throws(Exception::class)
    override suspend fun getPullRequest(requestParams: GithubRequestParams): List<PullRequestResult> {
        if (requestParams.owner.isEmpty()) throw Exception("Owner is empty")
        if (requestParams.repo.isEmpty()) throw Exception("Repo is empty")

        return api.getPullRequest(
            requestParams.owner,
            requestParams.repo,
            requestParams.state,
            requestParams.perPage,
            requestParams.page
        )
    }

}