package com.home.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import com.home.databinding.HomeListItemRepoBinding
import com.home.presentation.data.UiListItem
import com.home.presentation.comparators.HomePageComparator
import com.home.presentation.viewholders.BaseViewHolder
import com.home.presentation.viewholders.RepoViewHolder
import javax.inject.Inject

class HomeAdapter @Inject constructor(homePageComparator: HomePageComparator) :
    PagingDataAdapter<UiListItem, BaseViewHolder<UiListItem>>(homePageComparator) {

    override fun onBindViewHolder(holder: BaseViewHolder<UiListItem>, position: Int) {
        getItem(position)?.let {
            holder.bind(it)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<UiListItem> {
        return RepoViewHolder(
            HomeListItemRepoBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        ) as BaseViewHolder<UiListItem>
    }
}

