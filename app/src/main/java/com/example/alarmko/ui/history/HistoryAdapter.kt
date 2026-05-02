package com.example.alarmko.ui.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.alarmko.R
import com.example.alarmko.data.model.AlarmLog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter : ListAdapter<AlarmLog, HistoryAdapter.HistoryViewHolder>(HistoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val ivIcon: ImageView = itemView.findViewById(R.id.ivHistoryIcon)
        private val tvDate: TextView = itemView.findViewById(R.id.tvHistoryDate)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvHistoryStatus)

        fun bind(log: AlarmLog) {
            val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            tvDate.text = dateFormat.format(Date(log.triggeredAt))

            if (log.missionSuccess) {
                ivIcon.setImageResource(R.drawable.ic_check)
                tvStatus.text = itemView.context.getString(R.string.mission_success)
            } else {
                ivIcon.setImageResource(R.drawable.ic_error)
                tvStatus.text = itemView.context.getString(R.string.mission_failed)
            }
        }
    }

    class HistoryDiffCallback : DiffUtil.ItemCallback<AlarmLog>() {
        override fun areItemsTheSame(oldItem: AlarmLog, newItem: AlarmLog) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: AlarmLog, newItem: AlarmLog) =
            oldItem == newItem
    }
}