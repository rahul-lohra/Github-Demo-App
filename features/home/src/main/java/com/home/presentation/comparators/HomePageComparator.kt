package com.home.presentation.comparators

import androidx.recyclerview.widget.DiffUtil
import com.home.presentation.data.UiListItem
import javax.inject.Inject

class HomePageComparator @Inject constructor() : DiffUtil.ItemCallback<UiListItem>() {
    override fun areItemsTheSame(oldItem: UiListItem, newItem: UiListItem): Boolean {
        return (oldItem is UiListItem.RepoItem && newItem is UiListItem.RepoItem &&
                oldItem.id == newItem.id)
    }

    override fun areContentsTheSame(oldItem: UiListItem, newItem: UiListItem): Boolean =
        oldItem == newItem
}