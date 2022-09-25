package com.home.domain.usecase

import androidx.annotation.VisibleForTesting
import com.domain.BaseUseCase
import com.home.domain.data.HomeUseCaseDomainData
import com.home.domain.mapper.DomainMapper
import com.network.data.ApiResult
import com.network.data.repository.GithubRepository
import com.github.data.models.GithubRequestParams
import com.github.data.models.PullRequestState
import javax.inject.Inject

class HomeUseCase @Inject constructor(
    private val repository: GithubRepository,
    private val domainMapper: DomainMapper
) :
    BaseUseCase(repository) {

    suspend fun getPullRequest(
        owner: String,
        repo: String,
        pageSize: Int,
        page: Int
    ): HomeUseCaseDomainData {
        val requestParams = createRequestParams(owner, repo, PullRequestState.CLOSE, pageSize, page)
        val result = repository.getPullRequest(requestParams)
        return when (result) {
            is ApiResult.Success -> HomeUseCaseDomainData.Success(result.data.map {
                domainMapper.toHomeUseCaseDataSuccess(
                    it
                )
            })
            is ApiResult.Error -> HomeUseCaseDomainData.Error(result.th)
        }
    }

    @VisibleForTesting
    fun createRequestParams(
        owner: String,
        repo: String,
        @PullRequestState pullRequestState: String,
        pageSize: Int,
        page: Int
    ): GithubRequestParams {
        return GithubRequestParams(owner, repo, pullRequestState, pageSize, page)
    }
}