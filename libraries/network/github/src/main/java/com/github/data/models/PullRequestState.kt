package com.github.data.models

import androidx.annotation.StringDef

@StringDef(PullRequestState.OPEN, PullRequestState.ALL, PullRequestState.CLOSE)
@Retention
annotation class PullRequestState {
    companion object {
        const val ALL = "all"
        const val OPEN = "open"
        const val CLOSE = "closed"
    }
}