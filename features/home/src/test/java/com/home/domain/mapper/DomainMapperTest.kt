package com.home.domain.mapper

import com.home.domain.helper.DateTimeHelper
import com.github.data.models.PullRequestResult
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class DomainMapperTest {

    private lateinit var domainMapper: DomainMapper
    private val dateTimeHelper: DateTimeHelper = mockk()

    @Before
    fun setup() {
        domainMapper = spyk(DomainMapper(dateTimeHelper))
    }

    @Test
    fun testToHomeUseCaseDataSuccess() {
        val pullRequestResult: PullRequestResult = mockk(relaxed = true)

        every {
            dateTimeHelper.toHumanReadableTime(pullRequestResult.createdAt)
        } returns "createdAt"

        every {
            dateTimeHelper.toHumanReadableTime(pullRequestResult.closedAt)
        } returns "closedAt"

        val homeUseCaseDataSuccess = domainMapper.toHomeUseCaseDataSuccess(pullRequestResult)

        verify {
            dateTimeHelper.toHumanReadableTime(pullRequestResult.createdAt)
            dateTimeHelper.toHumanReadableTime(pullRequestResult.closedAt)
        }
        assert(homeUseCaseDataSuccess.id == pullRequestResult.id)
        assert(
            homeUseCaseDataSuccess.createdAt == dateTimeHelper.toHumanReadableTime(
                pullRequestResult.createdAt
            )
        )
        assert(
            homeUseCaseDataSuccess.closedAt == dateTimeHelper.toHumanReadableTime(
                pullRequestResult.closedAt
            )
        )
        assert(homeUseCaseDataSuccess.title == pullRequestResult.title)
        assert(homeUseCaseDataSuccess.avatarUrl == pullRequestResult.user.avatarUrl)
        assert(homeUseCaseDataSuccess.userId == pullRequestResult.user.login)
    }
}