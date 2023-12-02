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
import com.squidsentry.mobile.databinding.FragmentTimeframesWeekBinding
import com.squidsentry.mobile.ui.viewmodel.ThingSpeakViewModel
import com.squidsentry.mobile.ui.viewmodel.TimeframeViewModel
import java.time.LocalDate

class TimeframesWeekFragment : Fragment() {

    private var _binding: FragmentTimeframesWeekBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var timeframeViewModel: TimeframeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("HHHHHHHHTIMEFRAMESWEEK", "onCreate")
        // sync and get the data from parent fragment
        timeframeViewModel = ViewModelProvider(requireActivity())[TimeframeViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentTimeframesWeekBinding.inflate(inflater, container, false)
        Log.i("HHHHHHHHTIMEFRAMESWEEK", "onCreateView")

        binding.timeframesWeekParameter.text = timeframeViewModel.waterParameter.value.toString()
        binding.timeframesWeekUom.text = timeframeViewModel.waterParameterUom.value.toString()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.i("HHHHHHHHTIMEFRAMESWEEK", "onViewCreated")

        // sync and get the data from parent fragment
        val thingspeakViewModel = ViewModelProvider(requireActivity())[ThingSpeakViewModel::class.java]
        thingspeakViewModel.isDone.observe(viewLifecycleOwner){selectedDate ->
            val water_parameter: String = timeframeViewModel.waterParameter.value.toString()
            val timeframesDate: LocalDate = timeframeViewModel.timeframesDate.value!!
            Log.i(
                "HHHHHHHHTIMEFRAMESWEEK",
                "prepping to display graph" + selectedDate.toString()
            )
            Log.i(
                "HHHHHHHHTIMEFRAMESWEEK",
                "water parameter" + timeframeViewModel.waterParameter.value.toString()
            )
            Log.i(
                "HHHHHHHHTIMEFRAMESWEEK",
                "displaying " + thingspeakViewModel.getSelectedWaterQualityData(water_parameter, timeframesDate).toString()
            )
            Log.i(
                "HHHHHHHHTIMEFRAMESWEEK",
                "displaying measured " + thingspeakViewModel.getSelectedWaterQualityData(water_parameter, timeframesDate)
                    ?.weeklyWaterQuality?.measured?.size.toString()
            )
            val weekList: List<FloatEntry>? =
                thingspeakViewModel.getSelectedWaterQualityData(water_parameter, timeframesDate)?.weeklyWaterQuality?.measured?.toList()
            val max_measured = weekList?.maxWithOrNull(Comparator.comparingDouble { it.y.toDouble() })
            val min_measured = weekList?.minWithOrNull(Comparator.comparingDouble { it.y.toDouble() })
            if (weekList != null) {
                if(weekList.isNotEmpty()) {
                    val weekProducer = ChartEntryModelProducer(weekList)
                    binding.timeframesWeekChart.entryProducer = weekProducer
                    binding.timeframesWeekHigh.text = max_measured?.y.toString()
                    binding.timeframesWeekLow.text = min_measured?.y.toString()
                }else{
                    binding.timeframesWeekChart.setModel(entryModelOf(entriesOf(0f,0f,0f,0f,0f,0f)))
                    binding.timeframesWeekHigh.text = ""
                    binding.timeframesWeekLow.text = ""
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}