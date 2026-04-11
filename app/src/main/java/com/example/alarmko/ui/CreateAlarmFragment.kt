package com.example.alarmko.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.alarmko.R
import com.example.alarmko.alarm.AlarmScheduler
import com.example.alarmko.data.model.Alarm
import com.example.alarmko.data.model.MissionType
import com.example.alarmko.data.model.PhotoCategory
import com.example.alarmko.data.repository.AlarmRepository
import com.example.alarmko.exceptions.AlarmValidationException
import com.example.alarmko.exceptions.ErrorCode
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class CreateAlarmFragment : Fragment() {

    private lateinit var pickerHour: NumberPicker

    private lateinit var pickerMinute: NumberPicker
    private lateinit var chipGroupDays: ChipGroup
    private lateinit var etTaskDescription: TextInputEditText
    private lateinit var chipGroupMissions: ChipGroup
    private lateinit var chipGroupNotify: ChipGroup
    private lateinit var chipGroupCategories: ChipGroup
    private lateinit var layoutPhotoCategory: LinearLayout
    private lateinit var btnSaveAlarm: MaterialButton
    private lateinit var repository: AlarmRepository
    private lateinit var scheduler: AlarmScheduler
    private lateinit var layoutStepTarget: LinearLayout
    private lateinit var chipGroupSteps: ChipGroup

    private lateinit var layoutDifficulty: LinearLayout

    private lateinit var chipGroupDifficulty: ChipGroup

    private var editingAlarmId: Int = -1
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

        val alarmId = arguments?.getInt("ALARM_ID", -1) ?: -1
        if (alarmId != -1) {
            editingAlarmId = alarmId
            loadAlarm(alarmId)
            // Сменяме заглавието на toolbar-а
            view.findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
                .title = getString(R.string.edit_alarm_title)
        }

        setupMissionListener()

        btnSaveAlarm.setOnClickListener {
            saveAlarm()
        }

        val toolbar = view.findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun initViews(view: View) {

        pickerHour = view.findViewById(R.id.pickerHour)
        pickerMinute = view.findViewById(R.id.pickerMinute)


        pickerHour.minValue = 0
        pickerHour.maxValue = 23
        pickerHour.value = 7


        pickerMinute.minValue = 0
        pickerMinute.maxValue = 59
        pickerMinute.value = 30

        pickerHour.displayedValues = Array(24) {
            String.format("%02d", it)
        }
        pickerMinute.displayedValues = Array(60) {
            String.format("%02d", it)
        }
        chipGroupDays = view.findViewById(R.id.chipGroupDays)
        etTaskDescription = view.findViewById(R.id.etTaskDescription)
        chipGroupMissions = view.findViewById(R.id.chipGroupMissions)
        chipGroupNotify = view.findViewById(R.id.chipGroupNotify)
        chipGroupCategories = view.findViewById(R.id.chipGroupCategories)
        layoutPhotoCategory = view.findViewById(R.id.layoutPhotoCategory)
        btnSaveAlarm = view.findViewById(R.id.btnSaveAlarm)

        layoutStepTarget = view.findViewById(R.id.layoutStepTarget)
        chipGroupSteps = view.findViewById(R.id.chipGroupSteps)
        layoutDifficulty = view.findViewById(R.id.layoutDifficulty)
        chipGroupDifficulty = view.findViewById(R.id.chipGroupDifficulty)
    }

    private fun setupMissionListener() {
        chipGroupMissions.setOnCheckedStateChangeListener { _, checkedIds ->
            // Показваме/скриваме категориите за снимане
            if (checkedIds.contains(R.id.chipMissionPhoto)) {
                layoutPhotoCategory.visibility = View.VISIBLE
            } else {
                layoutPhotoCategory.visibility = View.GONE
                chipGroupCategories.clearCheck()
            }
            // Показваме/скриваме избора на стъпки
            if (checkedIds.contains(R.id.chipMissionSteps)) {
                layoutStepTarget.visibility = View.VISIBLE
            } else {
                layoutStepTarget.visibility = View.GONE
            }
            if (checkedIds.contains(R.id.chipMissionMath)) {
                layoutDifficulty.visibility = View.VISIBLE
            } else {
                layoutDifficulty.visibility = View.GONE
            }
        }
    }

    private fun saveAlarm() {
        try {
            val hour = pickerHour.value
            val minute = pickerMinute.value
            val title = etTaskDescription.text.toString().trim()
            val repeatDays = getSelectedDays()
            val missionType = getSelectedMission()
            val notifyBefore = getNotifyBefore()
            val photoCategory = getSelectedCategory()

            if (missionType == null) {
                throw AlarmValidationException(ErrorCode.ALARM_NO_MISSION_SELECTED)
            }

            if (missionType == MissionType.PHOTO && photoCategory == null) {
                throw AlarmValidationException(ErrorCode.ALARM_NO_MISSION_SELECTED)
            }

            val alarm = Alarm(
                title = if (title.isEmpty()) getString(R.string.default_alarm_title) else title,
                hour = hour,
                minute = minute,
                repeatDays = repeatDays,
                missionType = missionType,
                notifyBeforeMinutes = notifyBefore,
                photoCategory = photoCategory?.name,
                stepTarget = getSelectedStepTarget(),
                missionDifficulty = getSelectedDifficulty(),
                isActive = true
            )

            lifecycleScope.launch {
                try {
                    if (editingAlarmId == -1) {
                        // Нова аларма
                        val id = repository.insertAlarm(alarm)
                        val savedAlarm = alarm.copy(id = id.toInt())
                        scheduler.schedule(savedAlarm)
                    } else {
                        // Редактиране на съществуваща
                        val updatedAlarm = alarm.copy(id = editingAlarmId)
                        repository.updateAlarm(updatedAlarm)
                        scheduler.cancel(updatedAlarm)
                        scheduler.schedule(updatedAlarm)
                    }
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
                ErrorCode.ALARM_INVALID_TITLE ->
                    getString(R.string.error_alarm_invalid_title)
                ErrorCode.ALARM_NO_MISSION_SELECTED ->
                    getString(R.string.error_alarm_no_mission)
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
            R.id.chipMissionPhoto -> MissionType.PHOTO
            R.id.chipMissionSteps -> MissionType.STEPS
            else -> null
        }
    }

    private fun getSelectedCategory(): PhotoCategory? {
        return when (chipGroupCategories.checkedChipId) {
            R.id.chipCategoryKitchen -> PhotoCategory.KITCHEN
            R.id.chipCategoryBathroom -> PhotoCategory.BATHROOM
            R.id.chipCategoryHealth -> PhotoCategory.HEALTH
            R.id.chipCategoryWorkspace -> PhotoCategory.WORKSPACE
            R.id.chipCategoryLivingRoom -> PhotoCategory.LIVING_ROOM
            else -> null
        }
    }

    private fun getSelectedStepTarget(): Int {
        return when (chipGroupSteps.checkedChipId) {
            R.id.chipSteps10 -> 10
            R.id.chipSteps30 -> 30
            R.id.chipSteps50 -> 50
            else -> 20
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
    private fun getSelectedDifficulty(): Int {
        return when (chipGroupDifficulty.checkedChipId) {
            R.id.chipDifficultyMedium -> 2
            R.id.chipDifficultyHard -> 3
            else -> 1
        }
    }

    private fun loadAlarm(alarmId: Int) {
        lifecycleScope.launch {
            val alarm = repository.getAlarmById(alarmId) ?: return@launch
            requireActivity().runOnUiThread {
                // Зареждаме часа
                pickerHour.value = alarm.hour
                pickerMinute.value = alarm.minute

                // Зареждаме описанието
                etTaskDescription.setText(alarm.title)

                // Зареждаме дните
                alarm.repeatDays.forEach { day ->
                    when (day) {
                        '1' -> view?.findViewById<com.google.android.material.chip.Chip>(R.id.chipMon)?.isChecked = true
                        '2' -> view?.findViewById<com.google.android.material.chip.Chip>(R.id.chipTue)?.isChecked = true
                        '3' -> view?.findViewById<com.google.android.material.chip.Chip>(R.id.chipWed)?.isChecked = true
                        '4' -> view?.findViewById<com.google.android.material.chip.Chip>(R.id.chipThu)?.isChecked = true
                        '5' -> view?.findViewById<com.google.android.material.chip.Chip>(R.id.chipFri)?.isChecked = true
                        '6' -> view?.findViewById<com.google.android.material.chip.Chip>(R.id.chipSat)?.isChecked = true
                        '7' -> view?.findViewById<com.google.android.material.chip.Chip>(R.id.chipSun)?.isChecked = true
                    }
                }

                // Зареждаме мисията
                when (alarm.missionType) {
                    MissionType.MATH -> {
                        view?.findViewById<com.google.android.material.chip.Chip>(R.id.chipMissionMath)?.isChecked = true
                        layoutDifficulty.visibility = View.VISIBLE
                        when (alarm.missionDifficulty) {
                            2 -> view?.findViewById<com.google.android.material.chip.Chip>(R.id.chipDifficultyMedium)?.isChecked = true
                            3 -> view?.findViewById<com.google.android.material.chip.Chip>(R.id.chipDifficultyHard)?.isChecked = true
                            else -> view?.findViewById<com.google.android.material.chip.Chip>(R.id.chipDifficultyEasy)?.isChecked = true
                        }
                    }
                    MissionType.PHOTO -> view?.findViewById<com.google.android.material.chip.Chip>(R.id.chipMissionPhoto)?.isChecked = true
                    MissionType.STEPS -> {
                        view?.findViewById<com.google.android.material.chip.Chip>(
                            R.id.chipMissionSteps
                        )?.isChecked = true
                        layoutStepTarget.visibility = View.VISIBLE
                        when (alarm.stepTarget) {
                            10 -> view?.findViewById<com.google.android.material.chip.Chip>(R.id.chipSteps10)?.isChecked = true
                            30 -> view?.findViewById<com.google.android.material.chip.Chip>(R.id.chipSteps30)?.isChecked = true
                            50 -> view?.findViewById<com.google.android.material.chip.Chip>(R.id.chipSteps50)?.isChecked = true
                            else -> view?.findViewById<com.google.android.material.chip.Chip>(R.id.chipSteps20)?.isChecked = true
                        }
                    }
                }

                // Зареждаме категорията
                when (alarm.photoCategory) {
                    PhotoCategory.KITCHEN.name -> view?.findViewById<com.google.android.material.chip.Chip>(R.id.chipCategoryKitchen)?.isChecked = true
                    PhotoCategory.BATHROOM.name -> view?.findViewById<com.google.android.material.chip.Chip>(R.id.chipCategoryBathroom)?.isChecked = true
                    PhotoCategory.HEALTH.name -> view?.findViewById<com.google.android.material.chip.Chip>(R.id.chipCategoryHealth)?.isChecked = true
                    PhotoCategory.WORKSPACE.name -> view?.findViewById<com.google.android.material.chip.Chip>(R.id.chipCategoryWorkspace)?.isChecked = true
                    PhotoCategory.LIVING_ROOM.name -> view?.findViewById<com.google.android.material.chip.Chip>(R.id.chipCategoryLivingRoom)?.isChecked = true
                }

                // Зареждаме известието
                when (alarm.notifyBeforeMinutes) {
                    5 -> view?.findViewById<com.google.android.material.chip.Chip>(R.id.chipNotify5)?.isChecked = true
                    10 -> view?.findViewById<com.google.android.material.chip.Chip>(R.id.chipNotify10)?.isChecked = true
                    15 -> view?.findViewById<com.google.android.material.chip.Chip>(R.id.chipNotify15)?.isChecked = true
                    30 -> view?.findViewById<com.google.android.material.chip.Chip>(R.id.chipNotify30)?.isChecked = true
                }
            }
        }
    }
}