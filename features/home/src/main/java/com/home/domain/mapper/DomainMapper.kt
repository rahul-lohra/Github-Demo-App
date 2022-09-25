package com.home.domain.mapper

import com.home.domain.data.HomeUseCaseDataSuccess
import com.home.domain.helper.DateTimeHelper
import com.github.data.models.PullRequestResult
import javax.inject.Inject

class DomainMapper @Inject constructor(private val dateTimeHelper: DateTimeHelper) {
    fun toHomeUseCaseDataSuccess(data: PullRequestResult): HomeUseCaseDataSuccess {
        return HomeUseCaseDataSuccess(
            data.id,
            data.title,
            dateTimeHelper.toHumanReadableTime(data.createdAt),
            dateTimeHelper.toHumanReadableTime(data.closedAt),
            data.user.avatarUrl,
            data.user.login,
        )
    }
}