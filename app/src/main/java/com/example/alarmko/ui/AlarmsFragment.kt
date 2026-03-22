package com.example.alarmko.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.alarmko.R
import com.example.alarmko.data.repository.AlarmRepository
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import kotlinx.coroutines.launch

class AlarmsFragment : Fragment() {

    private lateinit var rvAlarms: RecyclerView
    private lateinit var fabAddAlarm: ExtendedFloatingActionButton
    private lateinit var tvNoAlarms: TextView
    private lateinit var adapter: AlarmAdapter
    private lateinit var repository: AlarmRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_alarms, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = AlarmRepository(requireContext())

        initViews(view)
        setupRecyclerView()
        observeAlarms()

        fabAddAlarm.setOnClickListener {
            findNavController().navigate(R.id.createAlarmFragment)
        }
    }

    private fun initViews(view: View) {
        rvAlarms = view.findViewById(R.id.rvAlarms)
        fabAddAlarm = view.findViewById(R.id.fabAddAlarm)
        tvNoAlarms = view.findViewById(R.id.tvNoAlarms)
    }

    private fun setupRecyclerView() {
        adapter = AlarmAdapter(
            onToggle = { alarm, isActive ->
                lifecycleScope.launch {
                    val updated = alarm.copy(isActive = isActive)
                    repository.updateAlarm(updated)
                }
            },
            onClick = { alarm ->
                // TODO: навигация към редактиране
            }
        )
        rvAlarms.layoutManager = LinearLayoutManager(requireContext())
        rvAlarms.adapter = adapter
    }

    private fun observeAlarms() {
        repository.allAlarms.observe(viewLifecycleOwner) { alarms ->
            adapter.submitList(alarms)
            tvNoAlarms.visibility = if (alarms.isEmpty()) View.VISIBLE else View.GONE
        }
    }
}