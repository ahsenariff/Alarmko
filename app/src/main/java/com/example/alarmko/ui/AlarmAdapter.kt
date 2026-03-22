package com.example.alarmko.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.alarmko.R
import com.example.alarmko.data.model.Alarm
import com.example.alarmko.utils.TimeUtils
import com.google.android.material.chip.Chip
import com.google.android.material.switchmaterial.SwitchMaterial

class AlarmAdapter(
    private val onToggle: (Alarm, Boolean) -> Unit,
    private val onClick: (Alarm) -> Unit
) : ListAdapter<Alarm, AlarmAdapter.AlarmViewHolder>(AlarmDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alarm, parent, false)
        return AlarmViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AlarmViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvTime: TextView = itemView.findViewById(R.id.tvAlarmTime)
        private val tvDays: TextView = itemView.findViewById(R.id.tvAlarmDays)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvAlarmTitle)
        private val switchAlarm: SwitchMaterial = itemView.findViewById(R.id.switchAlarm)
        private val chipMission: Chip = itemView.findViewById(R.id.chipAlarmMission)

        fun bind(alarm: Alarm) {
            tvTime.text = TimeUtils.formatTime(alarm.hour, alarm.minute)
            tvDays.text = TimeUtils.formatDaysShort(alarm.repeatDays)
            tvTitle.text = alarm.title

            chipMission.text = when (alarm.missionType.name) {
                "MATH" -> itemView.context.getString(R.string.mission_math)
                "SHAKE" -> itemView.context.getString(R.string.mission_shake)
                "QR_CODE" -> itemView.context.getString(R.string.mission_qr)
                "PHOTO" -> itemView.context.getString(R.string.mission_photo)
                "STEPS" -> itemView.context.getString(R.string.mission_steps)
                else -> ""
            }

            tvTime.alpha = if (alarm.isActive) 1f else 0.4f
            tvTitle.alpha = if (alarm.isActive) 1f else 0.4f
            tvDays.alpha = if (alarm.isActive) 1f else 0.4f

            switchAlarm.isChecked = alarm.isActive
            switchAlarm.setOnCheckedChangeListener { _, isChecked ->
                onToggle(alarm, isChecked)
            }

            itemView.setOnClickListener {
                onClick(alarm)
            }
        }
    }

    class AlarmDiffCallback : DiffUtil.ItemCallback<Alarm>() {
        override fun areItemsTheSame(oldItem: Alarm, newItem: Alarm) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Alarm, newItem: Alarm) = oldItem == newItem
    }
}