package com.example.alarmko.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.alarmko.R
import com.example.alarmko.data.db.AppDatabase
import com.example.alarmko.data.repository.AlarmRepository
import com.example.alarmko.utils.TimeUtils
import com.google.android.material.chip.Chip
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private lateinit var tvGreeting: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvNextAlarmTime: TextView
    private lateinit var tvNextAlarmTitle: TextView
    private lateinit var tvStreakCount: TextView
    private lateinit var tvBestStreak: TextView
    private lateinit var chipDays: Chip
    private lateinit var chipMission: Chip

    private lateinit var repository: AlarmRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = AlarmRepository(requireContext())

        initViews(view)
        setDate()
        observeAlarms()
    }

    private fun initViews(view: View) {
        tvGreeting = view.findViewById(R.id.tvGreeting)
        tvDate = view.findViewById(R.id.tvDate)
        tvNextAlarmTime = view.findViewById(R.id.tvNextAlarmTime)
        tvNextAlarmTitle = view.findViewById(R.id.tvNextAlarmTitle)
        tvStreakCount = view.findViewById(R.id.tvStreakCount)
        tvBestStreak = view.findViewById(R.id.tvBestStreak)
        chipDays = view.findViewById(R.id.chipDays)
        chipMission = view.findViewById(R.id.chipMission)
    }

    private fun setDate() {
        val dateFormat = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault())
        tvDate.text = dateFormat.format(Date())

        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        tvGreeting.text = when {
            hour < 12 -> getString(R.string.good_morning)
            hour < 18 -> getString(R.string.good_afternoon)
            else -> getString(R.string.good_evening)
        }
    }

    private fun observeAlarms() {
        repository.allAlarms.observe(viewLifecycleOwner) { alarms ->
            if (alarms.isEmpty()) {
                tvNextAlarmTime.text = "--:--"
                tvNextAlarmTitle.text = getString(R.string.no_alarms)
                chipDays.text = ""
                chipMission.text = ""
                return@observe
            }

            val today = TimeUtils.getTodayDayNumber()
            val nextAlarm = alarms
                .filter { it.isActive }
                .sortedBy { TimeUtils.getNextAlarmMillis(it.hour, it.minute) }
                .firstOrNull()

            nextAlarm?.let { alarm ->
                tvNextAlarmTime.text = TimeUtils.formatTime(alarm.hour, alarm.minute)
                tvNextAlarmTitle.text = alarm.title
                chipDays.text = TimeUtils.formatDaysShort(alarm.repeatDays)
                chipMission.text = getMissionName(alarm.missionType.name)
            }
        }
    }

    private fun getMissionName(missionType: String): String {
        return when (missionType) {
            "MATH" -> getString(R.string.mission_math)
            "SHAKE" -> getString(R.string.mission_shake)
            "QR_CODE" -> getString(R.string.mission_qr)
            "PHOTO" -> getString(R.string.mission_photo)
            "STEPS" -> getString(R.string.mission_steps)
            else -> ""
        }
    }
}