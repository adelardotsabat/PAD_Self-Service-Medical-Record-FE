package com.example.selfmedicalrecord.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.selfmedicalrecord.R
import com.example.selfmedicalrecord.data.admin.IntegrationEndpoint
import com.example.selfmedicalrecord.data.admin.IntegrationState
import com.example.selfmedicalrecord.databinding.ItemIntegrationBinding

class IntegrationAdapter :
    ListAdapter<IntegrationEndpoint, IntegrationAdapter.Holder>(Diff) {

    class Holder(val binding: ItemIntegrationBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = Holder(
        ItemIntegrationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = getItem(position)
        val context = holder.itemView.context
        with(holder.binding) {
            tvName.text = item.name
            tvUrl.text = item.url
            tvSync.text = context.getString(R.string.admin_integration_last_sync, item.lastSync)
            when (item.state) {
                IntegrationState.CONNECTED -> tvState.bindBadge(
                    context.getString(R.string.admin_state_connected),
                    R.color.badge_green_bg, R.color.badge_green_text
                )
                IntegrationState.FAILED -> tvState.bindBadge(
                    context.getString(R.string.admin_state_failed),
                    R.color.badge_red_bg, R.color.badge_red_text
                )
                IntegrationState.WAITING -> tvState.bindBadge(
                    context.getString(R.string.admin_state_waiting),
                    R.color.badge_amber_bg, R.color.badge_amber_text
                )
            }
        }
    }

    private object Diff : DiffUtil.ItemCallback<IntegrationEndpoint>() {
        override fun areItemsTheSame(a: IntegrationEndpoint, b: IntegrationEndpoint) = a.id == b.id
        override fun areContentsTheSame(a: IntegrationEndpoint, b: IntegrationEndpoint) = a == b
    }
}
