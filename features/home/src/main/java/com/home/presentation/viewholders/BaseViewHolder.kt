package com.home.presentation.viewholders

import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.viewbinding.ViewBinding
import com.home.databinding.HomeListItemRepoBinding
import com.home.presentation.data.UiListItem

abstract class BaseViewHolder<T : UiListItem>(binding: ViewBinding) : ViewHolder(binding.root) {
    abstract fun bind(item: T)
}

class RepoViewHolder(private val binding: HomeListItemRepoBinding) : BaseViewHolder<UiListItem.RepoItem>(binding) {
    override fun bind(item: UiListItem.RepoItem) {
        binding.userImage.setImage(item.userImage)
        with(binding) {
            userImage.setImage(item.userImage)
            tvTitle.text = item.title
            tvClosedDate.text = item.closedAt
            tvCreatedDate.text = item.createdAt
            tvUserName.text = item.userName
        }
    }
}
