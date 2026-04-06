package com.example.alarmko.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.alarmko.R
import com.example.alarmko.data.model.CameraSettings
import com.example.alarmko.data.model.PhotoObject
import com.example.alarmko.data.repository.AlarmRepository
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private lateinit var repository: AlarmRepository

    // Map от PhotoObject към Switch id
    private val switchMap = mapOf(
        PhotoObject.GLASS to R.id.switchGlass,
        PhotoObject.FRIDGE to R.id.switchFridge,
        PhotoObject.PLATE to R.id.switchPlate,
        PhotoObject.WATER_BOTTLE to R.id.switchWaterBottle,
        PhotoObject.COFFEE_CUP to R.id.switchCoffeeCup,
        PhotoObject.TOOTHBRUSH to R.id.switchToothbrush,
        PhotoObject.SOAP to R.id.switchSoap,
        PhotoObject.TOWEL to R.id.switchTowel,
        PhotoObject.MIRROR to R.id.switchMirror,
        PhotoObject.PILL to R.id.switchPill,
        PhotoObject.THERMOMETER to R.id.switchThermometer,
        PhotoObject.VITAMINS to R.id.switchVitamins,
        PhotoObject.NOTEBOOK to R.id.switchNotebook,
        PhotoObject.PEN to R.id.switchPen,
        PhotoObject.KEYBOARD to R.id.switchKeyboard,
        PhotoObject.MOUSE to R.id.switchMouse,
        PhotoObject.KEYS to R.id.switchKeys,
        PhotoObject.BOOK to R.id.switchBook,
        PhotoObject.REMOTE to R.id.switchRemote,
        PhotoObject.WINDOW to R.id.switchWindow
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = AlarmRepository(requireContext())

        loadSettings(view)
        setupSwitchListeners(view)
        setupThemeSwitch(view)
    }

    private fun setupThemeSwitch(view: View) {
        val switchDark = view.findViewById<SwitchMaterial>(R.id.switchDarkTheme)

        // Четем текущата тема от SharedPreferences
        val prefs = requireContext().getSharedPreferences("alarmko_prefs", 0)
        val isDark = prefs.getBoolean("dark_theme", false)
        switchDark.isChecked = isDark

        switchDark.setOnCheckedChangeListener { _, isChecked ->
            // Запазваме избора
            prefs.edit().putBoolean("dark_theme", isChecked).apply()

            // Прилагаме темата веднага
            val mode = if (isChecked) {
                androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
            } else {
                androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
            }
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(mode)
        }
    }

    private fun loadSettings(view: View) {
        repository.allCameraSettings.observe(viewLifecycleOwner) { settingsList ->
            val settingsMap = settingsList.associateBy { it.objectName }

            switchMap.forEach { (photoObject, switchId) ->
                val switch = view.findViewById<SwitchMaterial>(switchId)
                val isEnabled = settingsMap[photoObject.name]?.isEnabled ?: false
                switch.isChecked = isEnabled
            }
        }
    }

    private fun setupSwitchListeners(view: View) {
        switchMap.forEach { (photoObject, switchId) ->
            val switch = view.findViewById<SwitchMaterial>(switchId)
            switch.setOnCheckedChangeListener { _, isChecked ->
                lifecycleScope.launch {
                    try {
                        repository.saveCameraSettings(
                            CameraSettings(
                                objectName = photoObject.name,
                                isEnabled = isChecked
                            )
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}