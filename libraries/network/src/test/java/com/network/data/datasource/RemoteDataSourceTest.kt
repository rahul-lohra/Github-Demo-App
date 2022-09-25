package com.network.data.datasource

import com.github.data.api.GithubApi
import com.github.data.models.GithubRequestParams
import com.github.data.models.PullRequestResult
import com.github.data.models.PullRequestState
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals


class RemoteDataSourceTest {

    private val api: GithubApi = mockk()
    private lateinit var remoteDataSource: RemoteDataSource

    @Before
    fun setup() {
        remoteDataSource = RemoteDataSource(api)
    }

    @Test
    fun `test exception for empty owner getPullRequest`() = runTest {
        val requestParams: GithubRequestParams = mockk()
        every { requestParams.owner } returns ""
        kotlin.test.assertFailsWith<Exception>("Owner is empty") {
            remoteDataSource.getPullRequest(
                requestParams
            )
        }
    }

    @Test
    fun `test exception for empty repo getPullRequest`() = runTest {
        val requestParams: GithubRequestParams = mockk()
        every { requestParams.owner } returns "a"
        every { requestParams.repo } returns ""
        kotlin.test.assertFailsWith<Exception>("Repo is empty") {
            remoteDataSource.getPullRequest(
                requestParams
            )
        }
    }

    @Test
    fun `test getPullRequest happy case`() = runTest {
        val requestParams: GithubRequestParams =
            GithubRequestParams("o", "r", PullRequestState.CLOSE, 1, 10)

        val mockkedFunctionOutput: List<PullRequestResult> = mockk()
        coEvery {
            api.getPullRequest(
                requestParams.owner,
                requestParams.repo,
                requestParams.state,
                requestParams.perPage,
                requestParams.page
            )
        } returns mockkedFunctionOutput

        val realOutput = remoteDataSource.getPullRequest(requestParams)
        coVerifyOrder {
            requestParams.owner.isEmpty()
            requestParams.repo.isEmpty()
            api.getPullRequest(
                requestParams.owner,
                requestParams.repo,
                requestParams.state,
                requestParams.perPage,
                requestParams.page,
            )
        }
        assertEquals(realOutput, mockkedFunctionOutput)
    }
}