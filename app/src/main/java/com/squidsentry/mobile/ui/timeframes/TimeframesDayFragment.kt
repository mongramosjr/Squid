package com.squidsentry.mobile.ui.timeframes

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entriesOf
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.views.chart.ChartView
import com.squidsentry.mobile.Feed
import com.squidsentry.mobile.R
import com.squidsentry.mobile.ThingSpeak
import com.squidsentry.mobile.databinding.FragmentTimeframesDayBinding
import com.squidsentry.mobile.ui.home.HomeViewModel
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter


class TimeframesDayFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel

    private var _binding: FragmentTimeframesDayBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.i("PHHHHHHHHHTIMEFRAMES", "onCreate")
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        // Call the ThingSpeak API
        homeViewModel.getThingSpeakData()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentTimeframesDayBinding.inflate(inflater, container, false)
        return binding.root
        //return inflater.inflate(R.layout.fragment_timeframes_day, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i("PHHHHHHHHHTIMEFRAMES", "onViewCreated")
        homeViewModel.thingSpeakData.observe(viewLifecycleOwner) { thingSpeakData ->
            displayChart(thingSpeakData, view)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun displayChart(thingSpeakData: ThingSpeak?, view: View){

        // inject pH data
        var feeds: ListIterator<Feed>?

        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        //2023-04-06T00:13:00Z
        //val format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssX")

        if (thingSpeakData != null) {
            feeds = thingSpeakData.feeds?.listIterator()

            var pH = mutableListOf <FloatEntry>().apply {  }

            var last_measure_pH: Float = 0f

            var date_last_pH: LocalDateTime? = null

            Log.i("PHHHHHHHHHTIMEFRAMES", "prepping to display")

            if(feeds!=null) {

                var idx_ph: Int = 0

                while (feeds.hasNext()) {
                    val e = feeds.next()
                    if(e.field1!=null) {
                        pH.add(idx_ph, FloatEntry(idx_ph.toFloat(), e.field1.toFloat()))
                        last_measure_pH = e.field1.toFloat()
                        date_last_pH = OffsetDateTime.parse(e.createdAt).toLocalDateTime()
                        idx_ph++
                    }
                }
            }

            val phList: List<FloatEntry> = pH.toList()
            val phProducer = ChartEntryModelProducer(phList)
            binding.timeframesDayChart.entryProducer = phProducer
            if (date_last_pH != null) {
                binding.timeframesDayHigh.text = last_measure_pH.toString()
            }
        }else{
            Log.i("PHHHHHHHHHTIMEFRAMES", "no displayChart")
            displayEmptyChart( 1f)
            //displayEmptyChart(view, R.id.chart_fragment_pH, 1f)
        }
    }

    //private fun displayEmptyChart(view: View, id: Int, value: Float)
    private fun displayEmptyChart(value: Float)
    {
        binding.timeframesDayChart.setModel(entryModelOf(entriesOf(value, value, value,value,value, value)))
    }
}