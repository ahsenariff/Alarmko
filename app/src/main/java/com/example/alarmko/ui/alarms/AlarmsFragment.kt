package com.example.alarmko.ui.alarms

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.alarmko.R
import com.example.alarmko.ui.alarms.AlarmAdapter
import com.example.alarmko.ui.alarms.AlarmsViewModel
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

class AlarmsFragment : Fragment() {
    private lateinit var rvAlarms: RecyclerView
    private lateinit var fabAddAlarm: ExtendedFloatingActionButton
    private lateinit var tvNoAlarms: TextView
    private lateinit var adapter: AlarmAdapter
    private val viewModel: AlarmsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_alarms, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                val updated = alarm.copy(isActive = isActive)
                viewModel.updateAlarm(updated)
            },
            onClick = { alarm ->
                val bundle = Bundle().apply {
                    putInt("ALARM_ID", alarm.id)
                }
                findNavController().navigate(R.id.createAlarmFragment, bundle)
            }
        )
        rvAlarms.layoutManager = LinearLayoutManager(requireContext())
        rvAlarms.adapter = adapter

        val itemTouchHelper = androidx.recyclerview.widget.ItemTouchHelper(
            object : androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback(
                0,
                androidx.recyclerview.widget.ItemTouchHelper.LEFT or
                        androidx.recyclerview.widget.ItemTouchHelper.RIGHT
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean = false

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val position = viewHolder.adapterPosition
                    val alarm = adapter.currentList[position]
                    viewModel.deleteAlarm(alarm)
                }
            }
        )
        itemTouchHelper.attachToRecyclerView(rvAlarms)
    }

    private fun observeAlarms() {
        viewModel.allAlarms.observe(viewLifecycleOwner) { alarms ->
            adapter.submitList(alarms)
            tvNoAlarms.visibility = if (alarms.isEmpty()) View.VISIBLE else View.GONE
        }
    }
}