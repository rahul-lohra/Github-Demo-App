package com.home.domain.usecase

import com.home.domain.data.HomeUseCaseDomainData
import com.home.domain.mapper.DomainMapper
import com.network.data.ApiResult
import com.network.data.repository.GithubRepository
import com.github.data.models.GithubRequestParams
import com.github.data.models.PullRequestResult
import com.github.data.models.PullRequestState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class HomeUseCaseTest {

    private lateinit var homeUseCase: HomeUseCase
    private val githubRepository: GithubRepository = mockk()
    private val domainMapper: DomainMapper = mockk()

    @Before
    fun setup() {
        homeUseCase = spyk(HomeUseCase(githubRepository, domainMapper))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun getPullRequestSuccess() {
        runTest {
            val ownerName = "owner"
            val repo = "repo"
            val pageSize = 1
            val page = 10

            val requestParams: GithubRequestParams = mockk(relaxed = true)

            coEvery {
                homeUseCase.createRequestParams(
                    ownerName,
                    repo,
                    PullRequestState.CLOSE,
                    pageSize,
                    page
                )
            } returns requestParams
            val inputList: List<PullRequestResult> = emptyList()
            val apiResult: ApiResult<List<PullRequestResult>> = ApiResult.Success(inputList)

            coEvery { githubRepository.getPullRequest(requestParams) } returns apiResult

            val output = homeUseCase.getPullRequest(ownerName, repo, pageSize, page)

            coVerify {
                homeUseCase.createRequestParams(
                    ownerName,
                    repo,
                    PullRequestState.CLOSE,
                    pageSize,
                    page
                )
                githubRepository.getPullRequest(requestParams)

                (apiResult as ApiResult.Success).data.map {
                    domainMapper.toHomeUseCaseDataSuccess(it)
                }
            }
            assert((output as HomeUseCaseDomainData.Success).data == inputList)
        }

    }

    @Test
    fun getPullRequestError() {
        runTest {
            val ownerName = "owner"
            val repo = "repo"
            val pageSize = 1
            val page = 10

            val requestParams: GithubRequestParams = mockk(relaxed = true)

            coEvery {
                homeUseCase.createRequestParams(
                    ownerName,
                    repo,
                    PullRequestState.CLOSE,
                    pageSize,
                    page
                )
            } returns requestParams
            val ex = Exception()
            val apiResult: ApiResult<List<PullRequestResult>> = ApiResult.Error(ex)
            coEvery { githubRepository.getPullRequest(requestParams) } returns apiResult

            val output = homeUseCase.getPullRequest(ownerName, repo, pageSize, page)

            coVerify {
                homeUseCase.createRequestParams(
                    ownerName,
                    repo,
                    PullRequestState.CLOSE,
                    pageSize,
                    page
                )
                githubRepository.getPullRequest(requestParams)

            }
            assert((output as HomeUseCaseDomainData.Error).th == ex)
        }
    }

    @Test
    fun testCreateRequestParams() {
        val ownerName = "owner"
        val repo = "repo"
        val pageSize = 1
        val page = 10
        val state = PullRequestState.CLOSE
        val output = homeUseCase.createRequestParams(ownerName, repo, state, pageSize, page)
        assert(output.owner == ownerName)
        assert(output.repo == repo)
        assert(output.page == page)
        assert(output.perPage == pageSize)
        assert(output.state == state)
    }
}