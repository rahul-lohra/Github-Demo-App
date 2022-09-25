package com.home.presentation.paging

import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.home.presentation.data.UiListItem
import javax.inject.Inject

class PagingSourceUtil @Inject constructor() {
    fun getPager( pageSize:Int, api: suspend (Int) -> List<UiListItem>) = Pager(
        config = PagingConfig(pageSize, enablePlaceholders = false, initialLoadSize = 10,),
        pagingSourceFactory = {
            GithubPagingSource(api)
        }).flow
}