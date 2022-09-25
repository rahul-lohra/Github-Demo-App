package com.github.data

import com.github.data.api.GithubApi
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

class GithubApiTest {
    private val mockWebServer = MockWebServer()
    private val client = OkHttpClient.Builder()
        .connectTimeout(1, TimeUnit.SECONDS)
        .readTimeout(1, TimeUnit.SECONDS)
        .writeTimeout(1, TimeUnit.SECONDS)
        .build()

    private val api = Retrofit.Builder()
        .baseUrl(mockWebServer.url("/"))
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
        .create(GithubApi::class.java)

    @Test
    fun `test github base url`() {
        assertEquals(GithubApi.Config.BASE_URL, "https://api.github.com/repos/")
    }

    private fun readApiResponses(fileName: String): String {
        val file =
            File(ClassLoader.getSystemClassLoader().getResource("api_responses/$fileName")!!.path)
        return file.readText()
    }

    @Test
    fun `test get pull request`() = runTest {
        val mockResponse = MockResponse()
        mockResponse.setResponseCode(200)
        mockResponse.setBody(readApiResponses("github_pr_success.json"))
        mockWebServer.enqueue(mockResponse)
        mockWebServer.url("/repos/owner/repo/pulls")
        val response = api.getPullRequest("owner", "repo", "closed", 1, 10)
        assertEquals(response.size, 2)

        val request1 = mockWebServer.takeRequest()
        assertEquals(request1.method, "GET")
        val url = request1.requestUrl!!

        assertEquals(url.queryParameter("state"), "closed")
        assertEquals(url.queryParameter("per_page"), "1")
        assertEquals(url.queryParameter("page"), "10")
        assertEquals(request1.path, "/repos/owner/repo/pulls?state=closed&per_page=1&page=10")

        val responseItem = response.first()
        assertEquals(responseItem.id, "1056372593")
        assertEquals(responseItem.state, "closed")
        assertEquals(responseItem.title, "Migrate more tests to XProcessing testing APIs.")
        assertEquals(
            responseItem.user.avatarUrl,
            "https://avatars.githubusercontent.com/in/44061?v=4"
        )
        assertEquals(responseItem.user.login, "copybara-service[bot]")
        assertEquals(responseItem.createdAt, "2022-09-14T17:40:56Z")
        assertEquals(responseItem.closedAt, "2022-09-14T18:15:54Z")
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }
}