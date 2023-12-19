package app.sthenoteuthis.mobile.ui.timeframes

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.lifecycle.ViewModelProvider
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entriesOf
import com.patrykandpatrick.vico.core.entry.entryModelOf
import app.sthenoteuthis.mobile.databinding.FragmentTimeframesDayBinding
import app.sthenoteuthis.mobile.ui.notifications.WaterQualityAssessment
import app.sthenoteuthis.mobile.ui.notifications.WaterQualityNotification
import app.sthenoteuthis.mobile.ui.viewmodel.ThingSpeakViewModel
import app.sthenoteuthis.mobile.ui.viewmodel.TimeframeViewModel
import java.time.LocalDate

class TimeframesDayFragment : Fragment() {

    private var _binding: FragmentTimeframesDayBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    
    private lateinit var timeframeViewModel: TimeframeViewModel

    private lateinit var waterQualityAssessment: WaterQualityAssessment

    private lateinit var timeframesDayRange : LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("HHHHHHHHTIMEFRAMESDAY", "onCreate")
        // sync and get the data from parent fragment
        waterQualityAssessment = WaterQualityAssessment(0f, 0f)
        timeframeViewModel = ViewModelProvider(requireActivity())[TimeframeViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentTimeframesDayBinding.inflate(inflater, container, false)
        Log.i("HHHHHHHHTIMEFRAMESDAY", "onCreateView")

        binding.timeframesDayParameter.text = timeframeViewModel.waterParameter.value.toString()
        binding.timeframesDayUom.text = timeframeViewModel.waterParameterUom.value.toString()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.i("HHHHHHHHTIMEFRAMESDAY", "onViewCreated")

        var warningMessage: String = ""
        timeframesDayRange = binding.timeframesDayRange

        // sync and get the data from parent fragment
        val thingspeakViewModel =
            ViewModelProvider(requireActivity())[ThingSpeakViewModel::class.java]
        thingspeakViewModel.isDone.observe(viewLifecycleOwner) { requestDone ->
            val water_parameter: String = timeframeViewModel.waterParameter.value.toString()
            val timeframesDate: LocalDate = timeframeViewModel.timeframesDate.value!!
            val selectedDate: LocalDate =
                thingspeakViewModel.instantToLocalDate(requestDone.selectedDate)

            waterQualityAssessment = WaterQualityAssessment(0f, 0f)

            Log.i(
                "HHHHHHHHTIMEFRAMESDAY",
                "water parameter" + timeframeViewModel.waterParameter.value.toString()
            )
            Log.i(
                "HHHHHHHHTIMEFRAMESDAY",
                "displaying " + timeframesDate.toString()
            )
            Log.i(
                "HHHHHHHHTIMEFRAMESDAY",
                "prepping to display graph" + selectedDate.toString()
            )
            val dayList: List<FloatEntry>? =
                thingspeakViewModel.getSelectedWaterQualityData(
                    water_parameter,
                    selectedDate
                )?.dailyWaterQuality?.measured?.toList()
            val max_measured =
                dayList?.maxWithOrNull(Comparator.comparingDouble { it.y.toDouble() })
            val min_measured =
                dayList?.minWithOrNull(Comparator.comparingDouble { it.y.toDouble() })
            if (dayList != null) {
                if (dayList.isNotEmpty()) {
                    val dayProducer = ChartEntryModelProducer(dayList)
                    binding.timeframesDayChart.entryProducer = dayProducer
                    binding.timeframesDayHigh.text = max_measured?.y.toString()
                    binding.timeframesDayLow.text = min_measured?.y.toString()
                } else {
                    binding.timeframesDayChart.setModel(
                        entryModelOf(
                            entriesOf(
                                0f,
                                0f,
                                0f,
                                0f,
                                0f,
                                0f
                            )
                        )
                    )
                    binding.timeframesDayHigh.text = ""
                    binding.timeframesDayLow.text = ""
                }
            }


            if (min_measured != null) {
                if (max_measured != null) {
                    waterQualityAssessment =
                        WaterQualityAssessment(min_measured.y, max_measured.y)

                    warningMessage =
                        waterQualityAssessment.checkWaterQuality(water_parameter)
                }
            }
        }

        // TODO: buttonclicklistener
        timeframesDayRange.setOnClickListener {
            if(!waterQualityAssessment.checkStatus()) {
                showAlertWithMessage(warningMessage)
            }
         }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showAlertWithMessage(message: String) {
        val alertDialog = WaterQualityNotification.newInstance(message)
        alertDialog.show(childFragmentManager, "WaterQualityNotification")
    }

    private fun showDifferentAlerts() {
        showAlertWithMessage("Water quality is within acceptable range")
    }

    private fun showNoDataAlerts() {
        showAlertWithMessage("No captured adata")
    }
}