package com.example.selfmedicalrecord.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.selfmedicalrecord.R
import com.example.selfmedicalrecord.data.admin.LogCategory
import com.example.selfmedicalrecord.data.admin.LogEntry
import com.example.selfmedicalrecord.databinding.ItemLogBinding

class LogAdapter : ListAdapter<LogEntry, LogAdapter.Holder>(Diff) {

    class Holder(val binding: ItemLogBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = Holder(
        ItemLogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = getItem(position)
        with(holder.binding) {
            tvMeta.text = root.context.getString(R.string.admin_log_meta, item.time, item.actor)
            tvMessage.text = item.message
            tvTag.setText(
                when (item.category) {
                    LogCategory.LOGIN -> R.string.admin_filter_login
                    LogCategory.DATA -> R.string.admin_filter_data
                    LogCategory.SYSTEM -> R.string.admin_filter_system
                    LogCategory.ERROR -> R.string.admin_tag_error
                }
            )
        }
    }

    private object Diff : DiffUtil.ItemCallback<LogEntry>() {
        override fun areItemsTheSame(a: LogEntry, b: LogEntry) = a.id == b.id
        override fun areContentsTheSame(a: LogEntry, b: LogEntry) = a == b
    }
}
