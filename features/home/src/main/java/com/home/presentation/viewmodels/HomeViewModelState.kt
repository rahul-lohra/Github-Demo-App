package com.home.presentation.viewmodels

import androidx.annotation.StringDef

@StringDef(HomeViewModelState.OWNER, HomeViewModelState.REPO)
@Retention
annotation class HomeViewModelState {
    companion object {
        const val OWNER = "owner"
        const val REPO = "repo"
    }
}