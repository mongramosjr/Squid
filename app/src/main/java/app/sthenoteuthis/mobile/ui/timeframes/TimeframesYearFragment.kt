package app.sthenoteuthis.mobile.ui.timeframes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import app.sthenoteuthis.mobile.databinding.FragmentTimeframesYearBinding
import app.sthenoteuthis.mobile.ui.viewmodel.ThingSpeakViewModel
import app.sthenoteuthis.mobile.ui.viewmodel.TimeframeViewModel
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entriesOf
import com.patrykandpatrick.vico.core.entry.entryModelOf
import java.time.LocalDate

class TimeframesYearFragment : Fragment() {

    private var _binding: FragmentTimeframesYearBinding? = null
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
        _binding = FragmentTimeframesYearBinding.inflate(inflater, container, false)

        binding.timeframesYearParameter.text = timeframeViewModel.waterParameter.value.toString()
        binding.timeframesYearUom.text = timeframeViewModel.waterParameterUom.value.toString()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // sync and get the data from parent fragment
        val thingspeakViewModel = ViewModelProvider(requireActivity())[ThingSpeakViewModel::class.java]
        thingspeakViewModel.isDone.observe(viewLifecycleOwner){requestDone ->
            val water_parameter: String = timeframeViewModel.waterParameter.value.toString()
            val timeframesDate: LocalDate = timeframeViewModel.timeframesDate.value!!

            val yearList: List<FloatEntry>? =
                thingspeakViewModel.getSelectedWaterQualityData(water_parameter, timeframesDate)?.yearlyWaterQuality?.measured?.toList()
            val max_measured = yearList?.maxWithOrNull(Comparator.comparingDouble { it.y.toDouble() })
            val min_measured = yearList?.minWithOrNull(Comparator.comparingDouble { it.y.toDouble() })
            if (yearList != null) {
                if(yearList.isNotEmpty()) {
                    val yearProducer = ChartEntryModelProducer(yearList)
                    binding.timeframesYearChart.entryProducer = yearProducer
                    binding.timeframesYearHigh.text = max_measured?.y.toString()
                    binding.timeframesYearLow.text = min_measured?.y.toString()
                }else{
                    binding.timeframesYearChart.setModel(entryModelOf(entriesOf(0f,0f,0f,0f,0f,0f)))
                    binding.timeframesYearHigh.text = ""
                    binding.timeframesYearLow.text = ""
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}