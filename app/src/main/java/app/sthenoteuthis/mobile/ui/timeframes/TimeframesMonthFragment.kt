package app.sthenoteuthis.mobile.ui.timeframes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import app.sthenoteuthis.mobile.databinding.FragmentTimeframesMonthBinding
import app.sthenoteuthis.mobile.ui.viewmodel.ThingSpeakViewModel
import app.sthenoteuthis.mobile.ui.viewmodel.TimeframeViewModel
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entriesOf
import com.patrykandpatrick.vico.core.entry.entryModelOf
import java.time.LocalDate

class TimeframesMonthFragment : Fragment() {

    private var _binding: FragmentTimeframesMonthBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var timeframeViewModel: TimeframeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // sync and get the data from parent fragment
        timeframeViewModel = ViewModelProvider(requireActivity())[TimeframeViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentTimeframesMonthBinding.inflate(inflater, container, false)

        binding.timeframesMonthParameter.text = timeframeViewModel.waterParameter.value.toString()
        binding.timeframesMonthUom.text = timeframeViewModel.waterParameterUom.value.toString()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /// sync and get the data from parent fragment
        val thingspeakViewModel = ViewModelProvider(requireActivity())[ThingSpeakViewModel::class.java]
        thingspeakViewModel.isDone.observe(viewLifecycleOwner){requestDone ->
            val water_parameter: String = timeframeViewModel.waterParameter.value.toString()
            val timeframesDate: LocalDate = timeframeViewModel.timeframesDate.value!!

            val monthList: List<FloatEntry>? =
                thingspeakViewModel.getSelectedWaterQualityData(water_parameter, timeframesDate)?.monthlyWaterQuality?.measured?.toList()
            val max_measured = monthList?.maxWithOrNull(Comparator.comparingDouble { it.y.toDouble() })
            val min_measured = monthList?.minWithOrNull(Comparator.comparingDouble { it.y.toDouble() })
            if (monthList != null) {
                if(monthList.isNotEmpty()) {
                    val monthProducer = ChartEntryModelProducer(monthList)
                    binding.timeframesMonthChart.entryProducer = monthProducer
                    binding.timeframesMonthHigh.text = max_measured?.y.toString()
                    binding.timeframesMonthLow.text = min_measured?.y.toString()
                }else{
                    binding.timeframesMonthChart.setModel(entryModelOf(entriesOf(0f,0f,0f,0f,0f,0f)))
                    binding.timeframesMonthHigh.text = ""
                    binding.timeframesMonthLow.text = ""
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}