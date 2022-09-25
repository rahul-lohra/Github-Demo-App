package com.home.domain.data

sealed class HomeUseCaseDomainData {
    data class Success(val data:List<HomeUseCaseDataSuccess>) : HomeUseCaseDomainData()
    data class Error(val th: Throwable) : HomeUseCaseDomainData()
}

data class HomeUseCaseDataSuccess(
    val id: String,
    val title: String,
    val createdAt: String,
    val closedAt: String,
    val avatarUrl: String,
    val userId: String,
)