package com.aquadaily.app.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aquadaily.app.core.database.entity.HistoryEntity
import com.aquadaily.app.databinding.ItemHistoryBinding

class HistoryAdapter(
    private val onEditClick: (HistoryEntity) -> Unit,
    private val onDeleteClick: (HistoryEntity) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    private val historyList = mutableListOf<HistoryEntity>()

    inner class HistoryViewHolder(
        private val binding: ItemHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(history: HistoryEntity) {

            binding.tvHistoryDate.text = history.date

            binding.tvHistoryTime.text = history.time

            binding.tvHistoryAmount.text =
                "💧 ${history.amount} ml"

            binding.tvHistoryNote.text =
                if (history.note.isBlank()) {
                    "Tidak ada catatan"
                } else {
                    history.note
                }

            binding.btnEditHistory.setOnClickListener {
                onEditClick(history)
            }

            binding.btnDeleteHistory.setOnClickListener {
                onDeleteClick(history)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HistoryViewHolder {

        val binding = ItemHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: HistoryViewHolder,
        position: Int
    ) {

        holder.bind(historyList[position])
    }

    override fun getItemCount(): Int {
        return historyList.size
    }

    fun submitList(newList: List<HistoryEntity>) {
        historyList.clear()
        historyList.addAll(newList)
        notifyDataSetChanged()
    }

    fun getItemAt(position: Int): HistoryEntity {
        return historyList[position]
    }
}