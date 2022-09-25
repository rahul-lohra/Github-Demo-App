package com.home.presentation.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.home.databinding.HomeActivityHomeBinding
import com.home.presentation.data.UiAction
import com.home.presentation.data.UiState
import com.home.presentation.adapters.HomeAdapter
import com.home.presentation.viewmodels.HomeViewModel
import com.network.presentation.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : BaseActivity() {

    private lateinit var binding: HomeActivityHomeBinding
    private val viewModel: HomeViewModel by viewModels()
    @Inject
    lateinit var adapter: HomeAdapter
    private lateinit var layoutManager: LinearLayoutManager

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                val isLastItemVisible = layoutManager
                    .findLastVisibleItemPosition() == adapter.itemCount - 1
                if (isLastItemVisible) {
                    viewModel.sendUiAction(UiAction.Retry)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = HomeActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRv()
        setClickListeners()
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
                        is UiState.Initial->{}
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
                    handleErrors(it.append)
                    handleErrors(it.refresh)
                }
            }
        }
    }

    private fun addListenerToCheckIsLastItemVisibleOnRecyclerView() {
        removeListenerToCheckIsLastItemVisibleOnRecyclerView()
        binding.rv.addOnScrollListener(scrollListener)
    }

    private fun removeListenerToCheckIsLastItemVisibleOnRecyclerView() {
        binding.rv.removeOnScrollListener(scrollListener)
    }

    private fun handleErrors(state: LoadState) {
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

    private fun setClickListeners() {
        binding.btn.setOnClickListener {
//            viewModel.sendTestEvents()
            viewModel.sendUiAction(
                UiAction.Search(
                    binding.etOwner.text.toString(), binding.etRepo.text.toString()
                )
            )
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

    override fun onDestroy() {
        removeListenerToCheckIsLastItemVisibleOnRecyclerView()
        super.onDestroy()
    }
}