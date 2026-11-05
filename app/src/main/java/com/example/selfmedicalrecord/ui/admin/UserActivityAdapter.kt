package com.example.selfmedicalrecord.ui.admin

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.selfmedicalrecord.R
import com.example.selfmedicalrecord.data.admin.UserActivity
import com.example.selfmedicalrecord.databinding.ItemUserActivityBinding

class UserActivityAdapter : ListAdapter<UserActivity, UserActivityAdapter.Holder>(Diff) {

    class Holder(val binding: ItemUserActivityBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = Holder(
        ItemUserActivityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = getItem(position)
        val context = holder.itemView.context
        with(holder.binding) {
            tvName.text = item.name
            tvLastSeen.text = item.lastSeen
            val colorRes = if (item.isActive) R.color.badge_green_text else R.color.text_secondary
            val color = ContextCompat.getColor(context, colorRes)
            dot.backgroundTintList = ColorStateList.valueOf(color)
            tvState.setTextColor(color)
            tvState.setText(if (item.isActive) R.string.admin_active else R.string.admin_inactive)
        }
    }

    private object Diff : DiffUtil.ItemCallback<UserActivity>() {
        override fun areItemsTheSame(a: UserActivity, b: UserActivity) = a.id == b.id
        override fun areContentsTheSame(a: UserActivity, b: UserActivity) = a == b
    }
}
