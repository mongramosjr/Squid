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
import com.squidsentry.mobile.databinding.FragmentTimeframesYearBinding
import com.squidsentry.mobile.ui.viewmodel.ThingSpeakViewModel
import com.squidsentry.mobile.ui.viewmodel.TimeframeViewModel

class TimeframesYearFragment : Fragment() {

    private var _binding: FragmentTimeframesYearBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var timeframeViewModel: TimeframeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("HHHHHHHHTIMEFRAMESYEAR", "onCreate")
        // sync and get the data from parent fragment
        timeframeViewModel = ViewModelProvider(requireActivity())[TimeframeViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentTimeframesYearBinding.inflate(inflater, container, false)
        Log.i("HHHHHHHHTIMEFRAMESYEAR", "onCreateView")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.i("HHHHHHHHTIMEFRAMESYEAR", "onViewCreated")

        // sync and get the data from parent fragment
        val thingspeakViewModel = ViewModelProvider(requireActivity())[ThingSpeakViewModel::class.java]
        thingspeakViewModel.isDone.observe(viewLifecycleOwner){selectedDate ->
            val water_parameter: String = timeframeViewModel.waterParameter.value.toString()
            Log.i(
                "HHHHHHHHTIMEFRAMESYEAR",
                "prepping to display graph" + selectedDate.toString()
            )
            Log.i(
                "HHHHHHHHTIMEFRAMESYEAR",
                "water parameter" + timeframeViewModel.waterParameter.value.toString()
            )
            Log.i(
                "HHHHHHHHTIMEFRAMESYEAR",
                "displaying " + thingspeakViewModel.getSelectedWaterQualityData(water_parameter).toString()
            )
            Log.i(
                "HHHHHHHHTIMEFRAMESYEAR",
                "displaying measured " + thingspeakViewModel.getSelectedWaterQualityData(water_parameter)
                    ?.yearlyWaterQuality?.measured?.size.toString()
            )
            val dayList: List<FloatEntry>? =
                thingspeakViewModel.getSelectedWaterQualityData(water_parameter)?.yearlyWaterQuality?.measured?.toList()
            if (dayList != null) {
                if(dayList.isNotEmpty()) {
                    val dayProducer = ChartEntryModelProducer(dayList)
                    binding.timeframesYearChart.entryProducer = dayProducer
                    binding.timeframesYearHigh.text = dayList.last().y.toString()
                }else{
                    binding.timeframesYearChart.setModel(entryModelOf(entriesOf(0f,0f,0f,0f,0f,0f)))
                    binding.timeframesYearHigh.text = "--"
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}