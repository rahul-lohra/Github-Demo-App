package com.home.presentation.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.home.presentation.data.UiListItem

class GithubPagingSource(private val api: suspend (Int) -> List<UiListItem>) :
    PagingSource<Int, UiListItem>() {
    private var lastItem: UiListItem? = null
    override fun getRefreshKey(state: PagingState<Int, UiListItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPageIndex = state.pages.indexOf(state.closestPageToPosition(anchorPosition))
            state.pages.getOrNull(anchorPageIndex + 1)?.prevKey ?: state.pages.getOrNull(
                anchorPageIndex - 1
            )?.nextKey
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, UiListItem> {
        val currentPageNumber = params.key ?: 1
        try {
            val result = api.invoke(currentPageNumber)
            val nextPage =
                if (lastItem?.equals(result.lastOrNull()) == true || result.size < params.loadSize) null else currentPageNumber + 1

            if (lastItem?.equals(result.last()) == true) {
                return LoadResult.Page(
                    data = emptyList(),
                    if (currentPageNumber == 1) null else currentPageNumber - 1,
                    nextKey = nextPage
                )
            } else {
                lastItem = result.lastOrNull()
                return LoadResult.Page(
                    data = result,
                    if (currentPageNumber == 1) null else currentPageNumber - 1,
                    nextKey = nextPage
                )
            }
        } catch (th: Throwable) {
            return LoadResult.Error(th)
        }
    }
}