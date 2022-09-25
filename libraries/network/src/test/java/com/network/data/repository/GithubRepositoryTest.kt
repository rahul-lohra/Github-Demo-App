package com.network.data.repository

import com.github.data.models.GithubRequestParams
import com.github.data.models.PullRequestResult
import com.network.data.ApiResult
import com.network.data.datasource.RemoteDataSource
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.test.assertEquals

@RunWith(JUnit4::class)
class GithubRepositoryTest {
    private val dataSource: RemoteDataSource = mockk()
    private val repository: GithubRepository = spyk(GithubRepository(dataSource))

    @Test
    fun `test getPullRequest function invocation`() = runTest {
        val requestParams: GithubRequestParams = mockk()
        val listOfPullRequest: List<PullRequestResult> = mockk()

        coEvery { dataSource.getPullRequest(requestParams) } answers { listOfPullRequest }
        val lambdaSlot = slot<suspend () -> List<PullRequestResult>>()
        repository.getPullRequest(requestParams)
        coVerify { repository.invoke(capture(lambdaSlot)) }
        lambdaSlot.captured.invoke()

        coVerify { dataSource.getPullRequest(requestParams) }
    }

    @Test
    fun `test getPullRequest return`() = runTest {
        val requestParams: GithubRequestParams = mockk()
        val listOfPullRequest: List<PullRequestResult> = mockk()

        coEvery { dataSource.getPullRequest(requestParams) } answers { listOfPullRequest }
        val lambdaSlot = slot<suspend () -> List<PullRequestResult>>()
        val mockedOutput: ApiResult<List<PullRequestResult>> = mockk()
        coEvery { repository.invoke(capture(lambdaSlot)) } returns mockedOutput
        val realOutput = repository.getPullRequest(requestParams)
        assertEquals(realOutput, mockedOutput)
    }
}