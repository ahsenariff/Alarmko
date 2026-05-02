package com.example.alarmko.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.alarmko.R

class HistoryFragment : Fragment() {

    private lateinit var rvHistory: RecyclerView
    private lateinit var tvNoHistory: TextView
    private lateinit var adapter: HistoryAdapter
    private val viewModel: HistoryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvHistory = view.findViewById(R.id.rvHistory)
        tvNoHistory = view.findViewById(R.id.tvNoHistory)

        adapter = HistoryAdapter()
        rvHistory.layoutManager = LinearLayoutManager(requireContext())
        rvHistory.adapter = adapter

        observeHistory()
    }

    private fun observeHistory() {
        viewModel.allLogs.observe(viewLifecycleOwner) { logs ->
            if (logs.isEmpty()) {
                tvNoHistory.visibility = View.VISIBLE
                rvHistory.visibility = View.GONE
            } else {
                tvNoHistory.visibility = View.GONE
                rvHistory.visibility = View.VISIBLE
                adapter.submitList(logs)
            }
        }
    }
}