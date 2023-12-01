package com.squidsentry.mobile.ui.timeframes

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entriesOf
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.squidsentry.mobile.databinding.FragmentTimeframesMonthBinding
import com.squidsentry.mobile.ui.viewmodel.ThingSpeakViewModel
import com.squidsentry.mobile.ui.viewmodel.TimeframeViewModel
import java.time.LocalDate

class TimeframesMonthFragment : Fragment() {

    private var _binding: FragmentTimeframesMonthBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var timeframeViewModel: TimeframeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("HHHHHHHHTIMEFRAMESMONTH", "onCreate")
        // sync and get the data from parent fragment
        timeframeViewModel = ViewModelProvider(requireActivity())[TimeframeViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentTimeframesMonthBinding.inflate(inflater, container, false)
        Log.i("HHHHHHHHTIMEFRAMESMONTH", "onCreateView")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.i("HHHHHHHHTIMEFRAMESMONTH", "onViewCreated")

        /// sync and get the data from parent fragment
        val thingspeakViewModel = ViewModelProvider(requireActivity())[ThingSpeakViewModel::class.java]
        thingspeakViewModel.isDone.observe(viewLifecycleOwner){selectedDate ->
            val water_parameter: String = timeframeViewModel.waterParameter.value.toString()
            val timeframesDate: LocalDate = timeframeViewModel.timeframesDate.value!!
            Log.i(
                "HHHHHHHHTIMEFRAMESMONTH",
                "prepping to display graph" + selectedDate.toString()
            )
            Log.i(
                "HHHHHHHHTIMEFRAMESMONTH",
                "water parameter" + timeframeViewModel.waterParameter.value.toString()
            )
            Log.i(
                "HHHHHHHHTIMEFRAMESMONTH",
                "displaying " + thingspeakViewModel.getSelectedWaterQualityData(water_parameter, timeframesDate).toString()
            )
            Log.i(
                "HHHHHHHHTIMEFRAMESMONTH",
                "displaying measured " + thingspeakViewModel.getSelectedWaterQualityData(water_parameter, timeframesDate)
                    ?.monthlyWaterQuality?.measured?.size.toString()
            )
            val monthList: List<FloatEntry>? =
                thingspeakViewModel.getSelectedWaterQualityData(water_parameter, timeframesDate)?.monthlyWaterQuality?.measured?.toList()
            if (monthList != null) {
                if(monthList.isNotEmpty()) {
                    val dayProducer = ChartEntryModelProducer(monthList)
                    binding.timeframesMonthChart.entryProducer = dayProducer
                    binding.timeframesMonthHigh.text = monthList.last().y.toString()
                }else{
                    binding.timeframesMonthChart.setModel(entryModelOf(entriesOf(0f,0f,0f,0f,0f,0f)))
                    binding.timeframesMonthHigh.text = "--"
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}