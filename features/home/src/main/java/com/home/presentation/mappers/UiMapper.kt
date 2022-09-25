package com.home.presentation.mappers

import com.home.domain.data.HomeUseCaseDataSuccess
import com.home.presentation.data.UiListItem
import javax.inject.Inject

class UiMapper @Inject constructor() {
    fun toUiListItem(data: HomeUseCaseDataSuccess): UiListItem {
        return UiListItem.RepoItem(
            data.title,
            data.id,
            data.createdAt,
            data.closedAt,
            data.userId,
            data.avatarUrl,
        )
    }
}