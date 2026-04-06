package com.example.alarmko.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.alarmko.R
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
        setGreetingAndDate()
        observeAlarms()
        observeStreak()
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

    private fun setGreetingAndDate() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        tvGreeting.text = when {
            hour < 12 -> getString(R.string.good_morning)
            hour < 18 -> getString(R.string.good_afternoon)
            else -> getString(R.string.good_evening)
        }

        val dateFormat = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault())
        tvDate.text = dateFormat.format(Date())
    }

    private fun observeAlarms() {
        repository.allAlarms.observe(viewLifecycleOwner) { alarms ->
            val nextAlarm = alarms
                .filter { it.isActive }
                .sortedBy { TimeUtils.getNextAlarmMillis(it.hour, it.minute) }
                .firstOrNull()

            if (nextAlarm == null) {
                tvNextAlarmTime.text = "--:--"
                tvNextAlarmTitle.text = getString(R.string.no_alarms)
                chipDays.text = ""
                chipMission.text = ""
                chipDays.visibility = View.GONE
                chipMission.visibility = View.GONE
            } else {
                tvNextAlarmTime.text = TimeUtils.formatTime(nextAlarm.hour, nextAlarm.minute)
                tvNextAlarmTitle.text = nextAlarm.title
                chipDays.text = TimeUtils.formatDaysShort(nextAlarm.repeatDays, requireContext())
                chipMission.text = getMissionName(nextAlarm.missionType.name)
                chipDays.visibility = View.VISIBLE
                chipMission.visibility = View.VISIBLE
            }
        }
    }

    private fun observeStreak() {
        repository.allLogs.observe(viewLifecycleOwner) { logs ->
            val streak = calculateStreak(logs.map { it.triggeredAt to it.missionSuccess })
            tvStreakCount.text = streak.toString()
            tvBestStreak.text = "${getString(R.string.best_streak_prefix)} $streak ${getString(R.string.days)}"
        }
    }

    private fun calculateStreak(logs: List<Pair<Long, Boolean>>): Int {
        if (logs.isEmpty()) return 0

        // Сортираме от най-нов към най-стар
        val sorted = logs.sortedByDescending { it.first }

        var streak = 0
        var lastDate = ""

        for ((timestamp, success) in sorted) {
            if (!success) break

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = dateFormat.format(Date(timestamp))

            if (lastDate.isEmpty() || date != lastDate) {
                streak++
                lastDate = date
            }
        }

        return streak
    }

    private fun getMissionName(missionType: String): String {
        return when (missionType) {
            "MATH" -> getString(R.string.mission_math)
            "PHOTO" -> getString(R.string.mission_photo)
            else -> ""
        }
    }
}