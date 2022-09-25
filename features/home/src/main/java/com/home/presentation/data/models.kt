package com.home.presentation.data

sealed class UiAction {
    data class Search(val owner: String, val repo: String) : UiAction()
    object Retry : UiAction()
}

sealed class UiState {
    object Initial : UiState()
    class Loading(val text:String) : UiState()
    object Success : UiState()
    object Refresh : UiState()
    object Retry : UiState()
    class Error(val th: Throwable) : UiState()
}

sealed class UiListItem {
    data class RepoItem(
        val title: String,
        val id: String,
        val createdAt: String,
        val closedAt: String,
        val userName: String,
        val userImage: String,
    ) : UiListItem()
}

