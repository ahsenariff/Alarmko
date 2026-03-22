package com.example.alarmko.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TimePicker
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.alarmko.R
import com.example.alarmko.alarm.AlarmScheduler
import com.example.alarmko.data.model.Alarm
import com.example.alarmko.data.model.MissionType
import com.example.alarmko.data.repository.AlarmRepository
import com.example.alarmko.exceptions.AlarmValidationException
import com.example.alarmko.exceptions.ErrorCode
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class CreateAlarmFragment : Fragment() {

    private lateinit var timePicker: TimePicker
    private lateinit var chipGroupDays: ChipGroup
    private lateinit var etTaskDescription: TextInputEditText
    private lateinit var chipGroupMissions: ChipGroup
    private lateinit var chipGroupNotify: ChipGroup
    private lateinit var btnSaveAlarm: MaterialButton

    private lateinit var repository: AlarmRepository
    private lateinit var scheduler: AlarmScheduler

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_alarm, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = AlarmRepository(requireContext())
        scheduler = AlarmScheduler(requireContext())

        initViews(view)

        btnSaveAlarm.setOnClickListener {
            saveAlarm()
        }
        val toolbar = view.findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun initViews(view: View) {
        timePicker = view.findViewById(R.id.timePicker)
        chipGroupDays = view.findViewById(R.id.chipGroupDays)
        etTaskDescription = view.findViewById(R.id.etTaskDescription)
        chipGroupMissions = view.findViewById(R.id.chipGroupMissions)
        chipGroupNotify = view.findViewById(R.id.chipGroupNotify)
        btnSaveAlarm = view.findViewById(R.id.btnSaveAlarm)
        timePicker.setIs24HourView(true)
    }

    private fun saveAlarm() {
        try {
            val hour = timePicker.hour
            val minute = timePicker.minute
            val title = etTaskDescription.text.toString().trim()
            val repeatDays = getSelectedDays()
            val missionType = getSelectedMission()
            val notifyBefore = getNotifyBefore()

            // Валидация
            if (missionType == null) {
                throw AlarmValidationException(ErrorCode.ALARM_NO_MISSION_SELECTED)
            }

            val alarm = Alarm(
                title = title,
                hour = hour,
                minute = minute,
                repeatDays = repeatDays,
                missionType = missionType,
                notifyBeforeMinutes = notifyBefore,
                isActive = true
            )

            lifecycleScope.launch {
                try {
                    val id = repository.insertAlarm(alarm)
                    val savedAlarm = alarm.copy(id = id.toInt())
                    scheduler.schedule(savedAlarm)
                    requireActivity().runOnUiThread {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.alarm_saved),
                            Toast.LENGTH_SHORT
                        ).show()
                        requireActivity().onBackPressed()
                    }
                } catch (e: Exception) {
                    requireActivity().runOnUiThread {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.error_database),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        } catch (e: AlarmValidationException) {
            val message = when (e.errorCode) {
                ErrorCode.ALARM_INVALID_TITLE -> getString(R.string.error_alarm_invalid_title)
                ErrorCode.ALARM_NO_MISSION_SELECTED -> getString(R.string.error_alarm_no_mission)
                else -> getString(R.string.error_alarm_scheduling)
            }
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun getSelectedDays(): String {
        val days = StringBuilder()
        if (view?.findViewById<com.google.android.material.chip.Chip>(R.id.chipMon)?.isChecked == true) days.append("1")
        if (view?.findViewById<com.google.android.material.chip.Chip>(R.id.chipTue)?.isChecked == true) days.append("2")
        if (view?.findViewById<com.google.android.material.chip.Chip>(R.id.chipWed)?.isChecked == true) days.append("3")
        if (view?.findViewById<com.google.android.material.chip.Chip>(R.id.chipThu)?.isChecked == true) days.append("4")
        if (view?.findViewById<com.google.android.material.chip.Chip>(R.id.chipFri)?.isChecked == true) days.append("5")
        if (view?.findViewById<com.google.android.material.chip.Chip>(R.id.chipSat)?.isChecked == true) days.append("6")
        if (view?.findViewById<com.google.android.material.chip.Chip>(R.id.chipSun)?.isChecked == true) days.append("7")
        return days.toString()
    }

    private fun getSelectedMission(): MissionType? {
        return when (chipGroupMissions.checkedChipId) {
            R.id.chipMissionMath -> MissionType.MATH
            R.id.chipMissionShake -> MissionType.SHAKE
            R.id.chipMissionQr -> MissionType.QR_CODE
            R.id.chipMissionPhoto -> MissionType.PHOTO
            R.id.chipMissionSteps -> MissionType.STEPS
            else -> null
        }
    }

    private fun getNotifyBefore(): Int {
        return when (chipGroupNotify.checkedChipId) {
            R.id.chipNotify5 -> 5
            R.id.chipNotify10 -> 10
            R.id.chipNotify15 -> 15
            R.id.chipNotify30 -> 30
            else -> 0
        }
    }

}