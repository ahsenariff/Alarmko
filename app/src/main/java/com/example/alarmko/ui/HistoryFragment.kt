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

class HistoryFragment : Fragment() {

    private lateinit var rvHistory: RecyclerView
    private lateinit var tvNoHistory: TextView
    private lateinit var adapter: HistoryAdapter
    private lateinit var repository: AlarmRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = AlarmRepository(requireContext())

        rvHistory = view.findViewById(R.id.rvHistory)
        tvNoHistory = view.findViewById(R.id.tvNoHistory)

        adapter = HistoryAdapter()
        rvHistory.layoutManager = LinearLayoutManager(requireContext())
        rvHistory.adapter = adapter

        observeHistory()
    }

    private fun observeHistory() {
        repository.allLogs.observe(viewLifecycleOwner) { logs ->
            if (logs.isEmpty()) {
                tvNoHistory.visibility = View.VISIBLE
                rvHistory.visibility = View.GONE
            } else {
                tvNoHistory.visibility = View.GONE
                rvHistory.visibility = View.VISIBLE
                // Показваме най-новите първи
                adapter.submitList(logs.sortedByDescending { it.triggeredAt })
            }
        }
    }
}