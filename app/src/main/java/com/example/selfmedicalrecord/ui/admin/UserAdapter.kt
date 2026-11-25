package com.example.selfmedicalrecord.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.selfmedicalrecord.R
import com.example.selfmedicalrecord.data.admin.AccountStatus
import com.example.selfmedicalrecord.data.admin.AdminUser
import com.example.selfmedicalrecord.data.admin.UserRole
import com.example.selfmedicalrecord.databinding.ItemAdminUserBinding

class UserAdapter(
    private val onItemClick: (user: AdminUser) -> Unit,
    private val onMoreClick: (anchor: android.view.View, user: AdminUser) -> Unit
) : ListAdapter<AdminUser, UserAdapter.Holder>(Diff) {

    class Holder(val binding: ItemAdminUserBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = Holder(
        ItemAdminUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = getItem(position)
        val context = holder.itemView.context
        with(holder.binding) {
            tvName.text = item.name
            tvEmail.text = item.email
            val roleLabel = context.getString(
                if (item.role == UserRole.ADMIN) R.string.admin_role_admin else R.string.admin_role_user
            )
            tvRole.text = item.lastSeen
                ?.let { context.getString(R.string.admin_user_role_seen, roleLabel, it) }
                ?: roleLabel
            // Ikon perisai hanya untuk akun yang belum punya waktu aktif terakhir (sesuai desain).
            ivRoleIcon.isVisible = item.lastSeen == null
            when (item.status) {
                AccountStatus.ACTIVE -> tvStatus.bindBadge(
                    context.getString(R.string.admin_active), R.color.badge_green_bg, R.color.badge_green_text
                )
                AccountStatus.INACTIVE -> tvStatus.bindBadge(
                    context.getString(R.string.admin_inactive), R.color.badge_red_bg, R.color.badge_red_text
                )
                AccountStatus.PENDING -> tvStatus.bindBadge(
                    context.getString(R.string.admin_state_waiting), R.color.badge_amber_bg, R.color.badge_amber_text
                )
            }
            root.setOnClickListener { onItemClick(item) }
            btnMore.setOnClickListener { onMoreClick(btnMore, item) }
        }
    }

    private object Diff : DiffUtil.ItemCallback<AdminUser>() {
        override fun areItemsTheSame(a: AdminUser, b: AdminUser) = a.id == b.id
        override fun areContentsTheSame(a: AdminUser, b: AdminUser) = a == b
    }
}
