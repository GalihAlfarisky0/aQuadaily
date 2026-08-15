package com.aquadaily.app.ui.reminder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.aquadaily.app.animation.DashboardAnimation
import com.aquadaily.app.core.database.entity.ReminderEntity
import com.aquadaily.app.databinding.ItemReminderBinding
import java.util.Locale

class ReminderAdapter(
    private val onToggle: (ReminderEntity, Boolean) -> Unit,
    private val onEdit: (ReminderEntity) -> Unit,
    private val onDelete: (ReminderEntity) -> Unit
) : ListAdapter<ReminderEntity, ReminderAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemReminderBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
        DashboardAnimation.animateRecyclerViewItem(holder.itemView, position)
    }

    inner class ViewHolder(private val binding: ItemReminderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(reminder: ReminderEntity) {
            binding.apply {
                val hour12 = if (reminder.hour % 12 == 0) 12 else reminder.hour % 12
                val amPm = if (reminder.hour < 12) "AM" else "PM"
                
                tvTime.text = String.format(Locale.getDefault(), "%d:%02d", hour12, reminder.minute)
                tvAmPm.text = amPm
                tvDetail.text = "${reminder.amount} ml • ${reminder.day}"
                
                switchEnable.setOnCheckedChangeListener(null)
                switchEnable.isChecked = reminder.isEnabled
                
                // Set Opacity based on status
                root.alpha = if (reminder.isEnabled) 1.0f else 0.5f
                cvIcon.alpha = if (reminder.isEnabled) 1.0f else 0.5f

                switchEnable.setOnCheckedChangeListener { _, isChecked ->
                    onToggle(reminder, isChecked)
                }

                btnEdit.setOnClickListener {
                    onEdit(reminder)
                }

                btnDelete.setOnClickListener {
                    onDelete(reminder)
                }
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<ReminderEntity>() {
        override fun areItemsTheSame(oldItem: ReminderEntity, newItem: ReminderEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ReminderEntity, newItem: ReminderEntity): Boolean {
            return oldItem == newItem
        }
    }
}