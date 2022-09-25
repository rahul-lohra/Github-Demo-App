package com.network.data.repository

import com.github.data.models.GithubRequestParams
import com.github.data.models.PullRequestResult
import com.network.data.ApiResult
import com.network.data.datasource.RemoteDataSource
import javax.inject.Inject

class GithubRepository @Inject constructor(private val dataSource: RemoteDataSource) :
    BaseRepository {

    override suspend fun getPullRequest(requestParams: GithubRequestParams): ApiResult<List<PullRequestResult>> {
        return invoke { dataSource.getPullRequest(requestParams) }
    }
}