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
    private lateinit var tvDifficulty: TextView
    private lateinit var etAnswer: TextInputEditText
    private lateinit var btnSubmit: MaterialButton

    private var correctAnswer: Int = 0
    private var difficulty: Int = 1
    private var onMissionSuccess: (() -> Unit)? = null

    fun setDifficulty(level: Int) {
        difficulty = level
    }

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
        tvDifficulty = view.findViewById(R.id.tvDifficulty)
        etAnswer = view.findViewById(R.id.etMathAnswer)
        btnSubmit = view.findViewById(R.id.btnSubmitAnswer)

        tvDifficulty.text = when (difficulty) {
            1 -> getString(R.string.difficulty_easy)
            2 -> getString(R.string.difficulty_medium)
            3 -> getString(R.string.difficulty_hard)
            else -> getString(R.string.difficulty_easy)
        }

        generateQuestion()

        btnSubmit.setOnClickListener {
            checkAnswer()
        }
    }

    private fun generateQuestion() {
        try {
            when (difficulty) {
                1 -> {
                    val a = Random.nextInt(1, 11)
                    val b = Random.nextInt(1, 11)
                    val operator = listOf("+", "-").random()
                    correctAnswer = if (operator == "+") a + b else a - b
                    tvQuestion.text = "$a $operator $b = ?"
                }
                2 -> {
                    val a = Random.nextInt(1, 11)
                    val b = Random.nextInt(1, 11)
                    correctAnswer = a * b
                    tvQuestion.text = "$a × $b = ?"
                }
                3 -> {
                    val a = Random.nextInt(1, 21)
                    val b = Random.nextInt(1, 11)
                    val c = Random.nextInt(1, 11)
                    val op1 = listOf("+", "-").random()
                    val op2 = listOf("+", "-", "*").random()

                    val intermediate = if (op1 == "+") a + b else a - b
                    correctAnswer = when (op2) {
                        "+" -> intermediate + c
                        "-" -> intermediate - c
                        "*" -> intermediate * c
                        else -> intermediate + c
                    }
                    tvQuestion.text = "($a $op1 $b) $op2 $c = ?"
                }
                else -> {
                    val a = Random.nextInt(1, 11)
                    val b = Random.nextInt(1, 11)
                    correctAnswer = a + b
                    tvQuestion.text = "$a + $b = ?"
                }
            }
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