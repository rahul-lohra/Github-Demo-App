package com.home.presentation.viewmodels

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.home.domain.data.HomeUseCaseDomainData
import com.home.domain.usecase.HomeUseCase
import com.home.presentation.data.UiAction
import com.home.presentation.data.UiListItem
import com.home.presentation.data.UiState
import com.home.presentation.mappers.UiMapper
import com.home.presentation.paging.PagingSourceUtil
import com.network.presentation.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val io: CoroutineDispatcher,
    private val useCase: HomeUseCase,
    private val pagingSourceUtil: PagingSourceUtil,
    private val mapper: UiMapper,
    private val savedStateHandle: SavedStateHandle
) :
    BaseViewModel() {

    val pullRequestsFlow = MutableSharedFlow<PagingData<UiListItem>>(replay = 1)
    val uiStateFlow = MutableSharedFlow<UiState>(replay = 1)

    init {
        handleProcessDeath(
            savedStateHandle[HomeViewModelState.OWNER],
            savedStateHandle[HomeViewModelState.REPO]
        )
    }

    @VisibleForTesting
    fun handleProcessDeath(savedOwner: String?, savedRepo: String?) {
        if (!savedOwner.isNullOrEmpty() && !savedRepo.isNullOrEmpty()) {
            sendUiAction(UiAction.Search(savedOwner, savedRepo))
        }
    }

    fun sendUiAction(action: UiAction) {
        when (action) {
            is UiAction.Search -> handleSearchAction(action.owner, action.repo)
            is UiAction.Retry -> handleRetryAction()
        }
    }

    private fun handleRetryAction() {
        viewModelScope.launch(io) {
            uiStateFlow.emit(UiState.Loading("Loading"))
            uiStateFlow.emit(UiState.Retry)
        }
    }

    @VisibleForTesting
    fun isRefreshing(owner: String, repo: String): Boolean {
        val oldOwner: String? = savedStateHandle[HomeViewModelState.OWNER]
        val oldRepo: String? = savedStateHandle[HomeViewModelState.REPO]
        return oldOwner == owner && oldRepo == repo
    }

    @VisibleForTesting
    fun updateSavedState(owner: String, repo: String) {
        savedStateHandle[HomeViewModelState.OWNER] = owner
        savedStateHandle[HomeViewModelState.REPO] = repo
    }

    @VisibleForTesting
    fun handleSearchAction(owner: String, repo: String) {
        viewModelScope.launch(io) {
            uiStateFlow.emit(UiState.Loading("Loading"))

            if (isRefreshing(owner, repo)) {
                uiStateFlow.emit(UiState.Refresh)
                return@launch
            } else {
                updateSavedState(owner, repo)
            }
            loadPaginatedData(owner, repo)
        }
    }

    @VisibleForTesting
    suspend fun loadPaginatedData(owner: String, repo: String) {
        val pageSize = 10

        val pagerFlow = pagingSourceUtil.getPager(pageSize) { pageNumber ->
            val result = useCase.getPullRequest(owner, repo, pageSize, pageNumber)
            when (result) {
                is HomeUseCaseDomainData.Success -> result.data.map { mapper.toUiListItem(it) }
                is HomeUseCaseDomainData.Error -> throw result.th
            }
        }.distinctUntilChanged()

        collectFromPagingSource(pagerFlow.cachedIn(viewModelScope))
    }

    @VisibleForTesting
    fun collectFromPagingSource(result: Flow<PagingData<UiListItem>>) {
        viewModelScope.launch(io) {
            result.collect {
                pullRequestsFlow.emit(it)
            }
        }
    }

}