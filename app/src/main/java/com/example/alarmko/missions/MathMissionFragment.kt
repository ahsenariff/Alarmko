package com.example.alarmko.missions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.alarmko.R
import com.example.alarmko.exceptions.MissionFailedException
import com.example.alarmko.exceptions.ErrorCode
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlin.random.Random

class MathMissionFragment : Fragment() {

    private lateinit var tvQuestion: TextView
    private lateinit var etAnswer: TextInputEditText
    private lateinit var btnSubmit: MaterialButton

    private var correctAnswer: Int = 0
    private var onMissionSuccess: (() -> Unit)? = null

    fun setOnMissionSuccessListener(listener: () -> Unit) {
        onMissionSuccess = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mission_math, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvQuestion = view.findViewById(R.id.tvMathQuestion)
        etAnswer = view.findViewById(R.id.etMathAnswer)
        btnSubmit = view.findViewById(R.id.btnSubmitAnswer)

        generateQuestion()

        btnSubmit.setOnClickListener {
            checkAnswer()
        }
    }

    private fun generateQuestion() {
        try {
            val a = Random.nextInt(1, 20)
            val b = Random.nextInt(1, 20)
            val operator = listOf("+", "-", "*").random()

            correctAnswer = when (operator) {
                "+" -> a + b
                "-" -> a - b
                "*" -> a * b
                else -> a + b
            }

            tvQuestion.text = "$a $operator $b = ?"
        } catch (e: Exception) {
            throw MissionFailedException(ErrorCode.MISSION_FAILED, e)
        }
    }

    private fun checkAnswer() {
        try {
            val userAnswer = etAnswer.text.toString().trim()

            if (userAnswer.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.enter_answer),
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            val answer = userAnswer.toIntOrNull()
                ?: throw MissionFailedException(ErrorCode.MISSION_FAILED)

            if (answer == correctAnswer) {
                onMissionSuccess?.invoke()
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.wrong_answer),
                    Toast.LENGTH_SHORT
                ).show()
                etAnswer.text?.clear()
                generateQuestion()
            }
        } catch (e: MissionFailedException) {
            Toast.makeText(
                requireContext(),
                getString(R.string.error_mission_failed),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}