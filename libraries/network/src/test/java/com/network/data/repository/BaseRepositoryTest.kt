package com.network.data.repository

import com.github.data.models.GithubRequestParams
import com.github.data.models.PullRequestResult
import com.network.data.ApiResult
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class BaseRepositoryTest {
    private val repository: BaseRepository = object : BaseRepository {
        override suspend fun getPullRequest(requestParams: GithubRequestParams): ApiResult<List<PullRequestResult>> {
            return ApiResult.Success(emptyList())
        }
    }

    @Test
    fun `test invoke`() = runTest {
        val method:suspend () -> Int =  {10}
        val item: ApiResult<Int> = repository.invoke(method)
        assertIs<ApiResult.Success<Int>>(item)
        assertEquals(item.data, method.invoke())
    }
}