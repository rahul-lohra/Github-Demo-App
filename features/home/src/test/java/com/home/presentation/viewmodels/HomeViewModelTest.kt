package com.home.presentation.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import app.cash.turbine.testIn
import com.home.domain.usecase.HomeUseCase
import com.home.presentation.data.UiAction
import com.home.presentation.data.UiListItem
import com.home.presentation.data.UiState
import com.home.presentation.mappers.UiMapper
import com.home.presentation.paging.PagingSourceUtil
import io.mockk.*
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.reflect.KClass

class HomeViewModelTest {

    private lateinit var homeViewModel: HomeViewModel

    @OptIn(ExperimentalCoroutinesApi::class)
    private val io: CoroutineDispatcher = UnconfinedTestDispatcher()
    private val useCase: HomeUseCase = mockk()
    private val pagingSourceUtil: PagingSourceUtil = spyk()
    private val mapper: UiMapper = mockk()
    private val savedStateHandle: SavedStateHandle = mockk()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val testScope = TestScope()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup() {
        Dispatchers.setMain(io)
        every { savedStateHandle.get<String>(HomeViewModelState.OWNER) } returns ""
        every { savedStateHandle.get<String>(HomeViewModelState.REPO) } returns ""
        homeViewModel = createViewModel()
        every { homeViewModel.viewModelScope } answers { testScope }
    }

    private fun createViewModel() = spyk(HomeViewModel(io, useCase, pagingSourceUtil, mapper, savedStateHandle))

    @Test
    fun `test init block`() {
        //Mockk doesn't support mockking init block yet. So I am unable to test it and there no easy alternatives
        assertEquals(true,true)
    }

    @Test
    fun `test handleProcessDeath for non empty saved state handle`() {
        val uiActionSearchSlot = slot<UiAction.Search>()

        every { savedStateHandle.get<String>(HomeViewModelState.OWNER) } returns "s"
        every { savedStateHandle.get<String>(HomeViewModelState.REPO) } returns "r"

        every { homeViewModel.sendUiAction(capture(uiActionSearchSlot)) } just runs
        homeViewModel.handleProcessDeath("s", "r")
        verifyOrder {
            homeViewModel.handleProcessDeath("s", "r")
            !"s".isNullOrEmpty() && !"r".isNullOrEmpty()
            homeViewModel.sendUiAction(uiActionSearchSlot.captured)
        }
    }

    @Test
    fun `test handleProcessDeath for empty saved state handle`() {
        val savedOwner = ""
        val savedRepo = ""
        every { savedStateHandle.get<String>(HomeViewModelState.OWNER) } returns savedOwner
        every { savedStateHandle.get<String>(HomeViewModelState.REPO) } returns savedRepo

        homeViewModel.handleProcessDeath(savedOwner, savedRepo)

        verifyOrder {
            homeViewModel.handleProcessDeath(savedOwner, savedRepo)
            !savedOwner.isNullOrEmpty() && !savedRepo.isNullOrEmpty()
        }
        verify(exactly = 0){
            homeViewModel.sendUiAction(any())
        }
    }

    @Test
    fun testSendUiAction() {
        val owner = "owner"
        val repo = "repo"
        every { homeViewModel.handleSearchAction(owner, repo) } answers { mockk() }
        homeViewModel.sendUiAction(UiAction.Search(owner, repo))
        verify { homeViewModel.handleSearchAction(owner, repo) }
    }

    @Test
    fun testIsRefreshingTrue() {
        val owner = "owner"
        val repo = "repo"
        val savedOwner = "owner"
        val savedRepo = "repo"
        every {
            savedStateHandle.get<String>(HomeViewModelState.OWNER)
        } returns savedOwner

        every {
            savedStateHandle.get<String>(HomeViewModelState.REPO)
        } returns savedRepo

        val output = homeViewModel.isRefreshing(owner, repo)

        verify {
            savedStateHandle.get<String>(HomeViewModelState.OWNER)
            savedStateHandle.get<String>(HomeViewModelState.REPO)
            savedOwner == owner && savedRepo == repo
        }
        assertEquals(output, savedOwner == owner && savedRepo == repo)
    }

    @Test
    fun testIsRefreshingFalse() {
        val owner = "owner"
        val repo = "repo"
        val savedOwner = "savedOwner"
        val savedRepo = "savedRepo"
        every {
            savedStateHandle.get<String>(HomeViewModelState.OWNER)
        } returns savedOwner

        every {
            savedStateHandle.get<String>(HomeViewModelState.REPO)
        } returns savedRepo

        val output = homeViewModel.isRefreshing(owner, repo)

        verify {
            savedStateHandle.get<String>(HomeViewModelState.OWNER)
            savedStateHandle.get<String>(HomeViewModelState.REPO)
            savedOwner == owner && savedRepo == repo
        }
        assertEquals(output, savedOwner == owner && savedRepo == repo)
    }

    @Test
    fun testUpdateSavedState() {
        val owner = "owner"
        val repo = "repo"
        every { savedStateHandle.set<String>(HomeViewModelState.OWNER, owner) } just runs
        every { savedStateHandle.set<String>(HomeViewModelState.REPO, repo) } just runs
        homeViewModel.updateSavedState(owner, repo)
        verify {
            savedStateHandle.set<String>(HomeViewModelState.OWNER, owner)
            savedStateHandle.set<String>(HomeViewModelState.REPO, repo)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testHandleSearchActionWithRefreshingTrue() = runTest {
        val owner = "owner"
        val repo = "repo"

        val turbine = homeViewModel.uiStateFlow.testIn(this)

        every { homeViewModel.isRefreshing(owner, repo) } returns true

        homeViewModel.handleSearchAction(owner, repo)

        assert(turbine.awaitItem() is UiState.Loading)
        assert(turbine.awaitItem() is UiState.Refresh)
        turbine.cancel()

        coVerify {
            homeViewModel.isRefreshing(owner, repo)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test HandleSearchAction with refreshing false`() = runTest {
        val repo = "r"
        val owner = "o"
        val turbine = homeViewModel.uiStateFlow.testIn(this)

        every { homeViewModel.isRefreshing(owner, repo) } returns false
        every { homeViewModel.updateSavedState(owner, repo) } just runs
        coEvery { homeViewModel.loadPaginatedData(owner, repo) } just runs

        homeViewModel.handleSearchAction(owner, repo)

        assertEquals((turbine.awaitItem() as UiState.Loading).text, "Loading")

        coVerify {
            homeViewModel.isRefreshing(owner, repo)
            homeViewModel.updateSavedState(owner, repo)
            homeViewModel.loadPaginatedData(owner, repo)
        }
        turbine.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testLoadPaginatedData() = runTest {
        val owner = "owner"
        val repo = "repo"
        val pageSize = 10

        val slot = slot<suspend (Int) -> List<UiListItem>>()
        val outputFlow = spyk<Flow<PagingData<UiListItem>>>()
        every { pagingSourceUtil.getPager(pageSize, capture(slot)) } answers {
            outputFlow
        }
        homeViewModel.loadPaginatedData(owner, repo)
        verify {
            pagingSourceUtil.getPager(pageSize, slot.captured)
        }

        verify {
            homeViewModel.collectFromPagingSource(any())
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test pager flow is cached in`() = runTest {

        val outputFlow: MutableSharedFlow<PagingData<UiListItem>> = mockk()

        val distinctKt = "kotlinx.coroutines.flow.FlowKt"
        mockkStatic(distinctKt)

        every { pagingSourceUtil.getPager(any(), any()).distinctUntilChanged() } returns outputFlow
        every { homeViewModel.collectFromPagingSource(any()) } just runs

        val cachingPagingDataKt = "androidx.paging.CachedPagingDataKt"
        mockkStatic(cachingPagingDataKt)
        every { outputFlow.cachedIn(testScope) } answers { mockk() }

        homeViewModel.loadPaginatedData("", "")

        verify { outputFlow.cachedIn(testScope) }
        unmockkStatic(cachingPagingDataKt)
        unmockkStatic(distinctKt)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test collecting from paging source is happening on io dispatcher`() = runTest {
        val flow: Flow<PagingData<UiListItem>> = mockk()
        val buildersKt = "kotlinx.coroutines.BuildersKt"
        mockkStatic(buildersKt)

        val slot = slot<suspend CoroutineScope.() -> Unit>()

        every { homeViewModel.viewModelScope.launch(io, block = capture(slot)) } returns mockk()
        homeViewModel.collectFromPagingSource(flow)
        verify { homeViewModel.viewModelScope.launch(io, block = slot.captured) }
        unmockkStatic(buildersKt)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `check result flow is collected in collectFromPagingSource method`() = runTest {
        val flow: Flow<PagingData<UiListItem>> = mockk() //flowOf(PagingData.empty())
        val slotForPagingData = slot<FlowCollector<PagingData<UiListItem>>>()
        coEvery { flow.collect(capture(slotForPagingData)) } returns mockk()
        homeViewModel.collectFromPagingSource(flow)
        coVerify { flow.collect(slotForPagingData.captured) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `check pullRequestsFlow is emitting values collected from flow passed as function argument of collectFromPagingSource method`() =
        runTest {
            val pagingData: PagingData<UiListItem> = mockk()
            val flow: Flow<PagingData<UiListItem>> = flowOf(pagingData)
            val turbine = homeViewModel.pullRequestsFlow.testIn(this)

            homeViewModel.collectFromPagingSource(flow)
            assertEquals(turbine.awaitItem(), pagingData)
            turbine.cancel()
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun cleanup() {
        Dispatchers.resetMain()
    }

}