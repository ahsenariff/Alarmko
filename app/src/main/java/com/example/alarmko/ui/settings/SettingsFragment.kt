package com.example.alarmko.ui.settings

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.alarmko.R
import com.example.alarmko.data.model.CameraSettings
import com.example.alarmko.data.model.PhotoObject
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.switchmaterial.SwitchMaterial
import java.util.Locale

class SettingsFragment : Fragment() {

    private val viewModel: SettingsViewModel by viewModels()

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

        loadSettings(view)
        setupSwitchListeners(view)
        setupThemeSwitch(view)
        setupLanguageSwitch(view)
    }

    private fun setupThemeSwitch(view: View) {
        val switchDark = view.findViewById<SwitchMaterial>(R.id.switchDarkTheme)
        val prefs = requireContext().getSharedPreferences("alarmko_prefs", 0)
        val isDark = prefs.getBoolean("dark_theme", false)
        switchDark.isChecked = isDark

        switchDark.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("dark_theme", isChecked).apply()
            val mode = if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
            AppCompatDelegate.setDefaultNightMode(mode)
        }
    }

    private fun loadSettings(view: View) {
        viewModel.allCameraSettings.observe(viewLifecycleOwner) { settingsList ->
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
                viewModel.saveCameraSettings(
                    CameraSettings(
                        objectName = photoObject.name,
                        isEnabled = isChecked
                    )
                )
            }
        }
    }

    private fun setupLanguageSwitch(view: View) {
        val chipGroup = view.findViewById<ChipGroup>(R.id.chipGroupLanguage)
        val chipBg = view.findViewById<Chip>(R.id.chipLangBg)
        val chipEn = view.findViewById<Chip>(R.id.chipLangEn)

        val prefs = requireContext().getSharedPreferences("alarmko_prefs", 0)
        val savedLang = prefs.getString("app_language", "bg") ?: "bg"

        if (savedLang == "bg") chipBg.isChecked = true else chipEn.isChecked = true

        chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            val langCode = when {
                checkedIds.contains(R.id.chipLangBg) -> "bg"
                checkedIds.contains(R.id.chipLangEn) -> "en"
                else -> return@setOnCheckedStateChangeListener
            }
            prefs.edit().putString("app_language", langCode).apply()
            applyLanguage(langCode)
            requireActivity().recreate()
        }
    }

    private fun applyLanguage(langCode: String) {
        val locale = Locale(langCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        requireContext().resources.updateConfiguration(
            config,
            requireContext().resources.displayMetrics
        )
    }
}