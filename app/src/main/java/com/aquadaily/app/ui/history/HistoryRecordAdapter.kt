package com.aquadaily.app.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.aquadaily.app.animation.DashboardAnimation
import com.aquadaily.app.core.database.model.DailyWater
import com.aquadaily.app.databinding.ItemHistoryRecordBinding
import java.text.SimpleDateFormat
import java.util.*

class HistoryRecordAdapter(
    private val dailyTarget: Int
) : ListAdapter<DailyWater, HistoryRecordAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHistoryRecordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
        DashboardAnimation.animateRecyclerViewItem(holder.itemView, position)
    }

    inner class ViewHolder(private val binding: ItemHistoryRecordBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DailyWater) {
            binding.tvDate.text = formatDate(item.date)
            binding.tvTotalAmount.text = "${item.totalAmount} ml"
            
            if (item.totalAmount >= dailyTarget) {
                binding.tvGoalStatus.text = binding.root.context.getString(com.aquadaily.app.R.string.goal_met)
                binding.tvGoalStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
                binding.viewStatusDot.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#4CAF50"))
                binding.tvTotalAmount.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
                binding.cvIcon.setCardBackgroundColor(android.graphics.Color.parseColor("#F1F8E9"))
                binding.ivStatus.setImageResource(com.aquadaily.app.R.drawable.ic_completion)
                binding.ivStatus.setColorFilter(android.graphics.Color.parseColor("#4CAF50"))
            } else {
                binding.tvGoalStatus.text = binding.root.context.getString(com.aquadaily.app.R.string.goal_not_met)
                binding.tvGoalStatus.setTextColor(android.graphics.Color.parseColor("#FF9800"))
                binding.viewStatusDot.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FF9800"))
                binding.tvTotalAmount.setTextColor(android.graphics.Color.parseColor("#FF9800"))
                binding.cvIcon.setCardBackgroundColor(android.graphics.Color.parseColor("#FFF3E0"))
                binding.ivStatus.setImageResource(com.aquadaily.app.R.drawable.ic_water_drop)
                binding.ivStatus.setColorFilter(android.graphics.Color.parseColor("#FF9800"))
            }
        }

        private fun formatDate(dateStr: String): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd MMMM", Locale.getDefault())
                val date = inputFormat.parse(dateStr)
                outputFormat.format(date ?: Date())
            } catch (e: Exception) {
                dateStr
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<DailyWater>() {
        override fun areItemsTheSame(oldItem: DailyWater, newItem: DailyWater) = oldItem.date == newItem.date
        override fun areContentsTheSame(oldItem: DailyWater, newItem: DailyWater) = oldItem == newItem
    }
}