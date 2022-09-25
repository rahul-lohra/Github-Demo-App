package com.home.presentation.activities

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.core.KeyboardUtils.hideKeyboard
import com.core.activities.ScrollingActivity
import com.home.databinding.HomeActivityHomeBinding
import com.home.presentation.adapters.HomeAdapter
import com.home.presentation.data.UiAction
import com.home.presentation.data.UiState
import com.home.presentation.viewmodels.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : ScrollingActivity() {

    private lateinit var binding: HomeActivityHomeBinding
    private val viewModel: HomeViewModel by viewModels()

    @Inject
    lateinit var adapter: HomeAdapter
    private lateinit var layoutManager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = HomeActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRv()
        setListeners()
        observeFlows()
    }

    private fun observeFlows() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiStateFlow.collect {
                    when (it) {
                        is UiState.Success -> {
                            handleUiStateSuccess()
                        }
                        is UiState.Loading -> {
                            handleUiStateLoading(it)
                        }
                        is UiState.Error -> {
                            handleUiStateError(it)
                        }
                        is UiState.Refresh -> {
                            adapter.refresh()
                        }
                        is UiState.Retry -> {
                            adapter.retry()
                        }
                        is UiState.Initial -> {}
                    }
                }
            }
        }
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.pullRequestsFlow.distinctUntilChanged().collectLatest {
                    handleUiStateSuccess()
                    adapter.submitData(it)
                }
            }
        }
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                adapter.loadStateFlow.distinctUntilChanged().collectLatest {
                    handlePagingErrorErrors(it.append)
                    handlePagingErrorErrors(it.refresh)
                }
            }
        }
    }

    private fun handlePagingErrorErrors(state: LoadState) {
        when (state) {
            is LoadState.Error -> {
                addListenerToCheckIsLastItemVisibleOnRecyclerView()
                handleUiStateError(UiState.Error(state.error))
            }
            is LoadState.NotLoading -> {
                if (state.endOfPaginationReached) {
                    removeListenerToCheckIsLastItemVisibleOnRecyclerView()
                }
            }
            is LoadState.Loading -> {
                removeListenerToCheckIsLastItemVisibleOnRecyclerView()
            }
        }
    }

    private fun sendSearchAction() {
        binding.btn.hideKeyboard(this.window)

        viewModel.sendUiAction(
            UiAction.Search(
                binding.etOwner.text.toString(), binding.etRepo.text.toString()
            )
        )
    }

    private fun setListeners() {
        binding.btn.setOnClickListener {
            sendSearchAction()
        }

        binding.etOwner.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                sendSearchAction()
                return@setOnEditorActionListener true
            }
            false
        }

        binding.etRepo.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                sendSearchAction()
                return@setOnEditorActionListener true
            }
            false
        }
    }

    private fun setupRv() {
        layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rv.layoutManager = layoutManager
        binding.rv.adapter = adapter
    }

    private fun handleUiStateLoading(state: UiState.Loading) {
        binding.tvLoading.text = state.text
        handleLoadingTextVisibility()
    }

    private fun handleUiStateError(state: UiState.Error) {
        binding.tvLoading.text = state.th.message
        handleLoadingTextVisibility()
        Toast.makeText(this, state.th.message, Toast.LENGTH_SHORT).show()
    }

    private fun handleUiStateSuccess() {
        binding.tvLoading.visibility = View.GONE
    }

    private fun handleLoadingTextVisibility() {
        binding.rv.post {
            binding.tvLoading.visibility =
                if (layoutManager.itemCount == 0) View.VISIBLE else View.GONE
        }
    }

    override fun getRecyclerView() = binding.rv

    override fun performRetryActionFromRecyclerView() = viewModel.sendUiAction(UiAction.Retry)
}