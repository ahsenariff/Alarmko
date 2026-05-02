package com.example.alarmko.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.alarmko.R
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
    private lateinit var tvTimeUntil: TextView

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
        tvTimeUntil = view.findViewById(R.id.tvTimeUntil)
    }

    private fun setGreetingAndDate() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val (greetingText, greetingIcon) = when {
            hour < 12 -> Pair(getString(R.string.good_morning), R.drawable.ic_sun)
            hour < 18 -> Pair(getString(R.string.good_afternoon), R.drawable.ic_afternoon)
            else -> Pair(getString(R.string.good_evening), R.drawable.ic_moon)
        }
        tvGreeting.text = greetingText
        view?.findViewById<ImageView>(R.id.ivGreetingIcon)?.setImageResource(greetingIcon)

        val dateFormat = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault())
        tvDate.text = dateFormat.format(Date())
    }

    private fun observeAlarms() {
        viewModel.allAlarms.observe(viewLifecycleOwner) { alarms ->
            val nextAlarm = viewModel.getNextAlarm(alarms)

            if (nextAlarm == null) {
                tvNextAlarmTime.text = "--:--"
                tvNextAlarmTitle.text = getString(R.string.no_alarms)
                chipDays.text = ""
                chipMission.text = ""
                chipDays.visibility = View.GONE
                chipMission.visibility = View.GONE
                tvTimeUntil.visibility = View.GONE
            } else {
                tvNextAlarmTime.text = TimeUtils.formatTime(nextAlarm.hour, nextAlarm.minute)
                tvNextAlarmTitle.text = nextAlarm.title
                chipDays.text = TimeUtils.formatDaysShort(nextAlarm.repeatDays, requireContext())
                chipMission.text = getMissionName(nextAlarm.missionType.name)
                chipDays.visibility = View.VISIBLE
                chipMission.visibility = View.VISIBLE
                tvTimeUntil.text = getString(
                    R.string.time_until_alarm,
                    TimeUtils.getTimeUntilAlarm(
                        nextAlarm.hour, nextAlarm.minute,
                        requireContext(), nextAlarm.repeatDays
                    )
                )
                tvTimeUntil.visibility = View.VISIBLE
            }
        }
    }

    private fun observeStreak() {
        viewModel.allLogs.observe(viewLifecycleOwner) { logs ->
            val streak = viewModel.calculateStreak(logs)
            tvStreakCount.text = "$streak 🔥"
            tvBestStreak.text = "${getString(R.string.best_streak_prefix)} $streak ${getString(R.string.days)}"
        }
    }

    private fun getMissionName(missionType: String): String {
        return when (missionType) {
            "MATH" -> getString(R.string.mission_math)
            "PHOTO" -> getString(R.string.mission_photo)
            "STEPS" -> getString(R.string.mission_steps)
            else -> ""
        }
    }
}