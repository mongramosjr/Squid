package com.squidsentry.mobile.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entriesOf
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.views.chart.ChartView
import com.squidsentry.mobile.Feed
import com.squidsentry.mobile.R
import com.squidsentry.mobile.ThingSpeak
import com.squidsentry.mobile.databinding.FragmentHomeBinding
import kotlin.random.Random

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    //private lateinit var squidErrorImg: ImageView
    //private lateinit var result: TextView

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //homeViewModel = HomeViewModel()
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        // Call the ThingSpeak API
        homeViewModel.getThingSpeakData()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root


        //homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        homeViewModel.getThingSpeakData()

        Log.i("MMMMMMMM", "onCreateView Home fragment")

        //val textView: TextView = binding.textHome
        //homeViewModel.text.observe(viewLifecycleOwner) {
        //    textView.text = it
        //}

        subscribe(root)
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun subscribe(root: View) {

        homeViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Set the result text to Loading
            if (isLoading) {
                //result.text = resources.getString(R.string.loading)
                //TODO: display chart with random
                fun getRandomEntries() = entriesOf( Random.nextFloat() * 16f)
                root.findViewById<ChartView>(R.id.chart_view_temperature)
                    .setModel(entryModelOf(entriesOf(96f,33,23f,455f,76f)))
            }

        }

        homeViewModel.isError.observe(viewLifecycleOwner) { isError ->
            // Hide display image and set the result text to the error message
            if (isError) {
                //squidErrorImg.visibility = View.GONE
                //result.text = homeViewModel.errorMessage
                //TODO: display error message
            }
        }

        homeViewModel.thingSpeakData.observe(viewLifecycleOwner) { thingSpeakData ->
            displayChart(thingSpeakData, root)
        }
    }

    private fun displayChart(thingSpeakData: ThingSpeak?, root: View){

        // inject pH data
        var feeds: ListIterator<Feed>?

        if (thingSpeakData != null) {
            feeds = thingSpeakData.feeds?.listIterator()

            var pH = mutableListOf <FloatEntry>().apply {  }
            var temperature = mutableListOf <FloatEntry>().apply {  }
            var salinity = mutableListOf <FloatEntry>().apply {  }
            var dissolvedoxygen = mutableListOf <FloatEntry>().apply {  }
            var tds = mutableListOf <FloatEntry>().apply {  }
            var turbidity = mutableListOf <FloatEntry>().apply {  }

            if(feeds!=null) {
                
                var idx_ph: Int = 0
                var idx_temperature: Int = 0
                var idx_salinity: Int = 0
                var idx_dissolvedoxygent: Int = 0
                var idx_tds: Int = 0
                var idx_turbidity: Int = 0

                while (feeds.hasNext()) {
                    val e = feeds.next()


                    if(e.field1!=null) {
                        pH.add(idx_ph, FloatEntry(idx_ph.toFloat(), e.field1.toFloat()))
                        idx_ph++
                        Log.e("OOOOOO", e.field1.toString() + ":" + idx_ph.toFloat())
                    }
                    if(e.field2!=null) {
                        temperature.add(idx_temperature, FloatEntry(idx_temperature.toFloat(), e.field2.toFloat()))
                        idx_temperature++
                    }
                    if(e.field3!=null) {
                        salinity.add(idx_salinity, FloatEntry(idx_salinity.toFloat(), e.field3.toFloat()))
                        idx_salinity++
                    }
                    if(e.field4!=null) {
                        dissolvedoxygen.add(idx_dissolvedoxygent, FloatEntry(idx_dissolvedoxygent.toFloat(), e.field4.toFloat()))
                        idx_dissolvedoxygent++
                    }
                    if(e.field5!=null) {
                        tds.add(idx_tds, FloatEntry(idx_tds.toFloat(), e.field5.toFloat()))
                        idx_tds++
                    }
                    if(e.field6!=null) {
                        temperature.add(idx_turbidity, FloatEntry(idx_turbidity.toFloat(), e.field6.toFloat()))
                        idx_turbidity++
                    }


                }
            }

            Log.e("OOOOOOOOOOO", pH.count().toString())
            /*
            val phList: List<FloatEntry> = pH.toList()
            root.findViewById<ChartView>(R.id.chart_view_pH)
                .setModel(entryModelOf(phList))
            */
            val phList: List<FloatEntry> = pH.toList()
            val phProducer = ChartEntryModelProducer(phList)
            root.findViewById<ChartView>(R.id.chart_view_pH).entryProducer = phProducer

            val temperatureList: List<FloatEntry> = temperature.toList()
            val temperatureProducer = ChartEntryModelProducer(temperatureList)
            root.findViewById<ChartView>(R.id.chart_view_temperature).entryProducer = temperatureProducer

            val salinityList: List<FloatEntry> = salinity.toList()
            val salinityProducer = ChartEntryModelProducer(salinityList)
            root.findViewById<ChartView>(R.id.chart_view_salinity).entryProducer = salinityProducer

            val dissolvedoxygenList: List<FloatEntry> = dissolvedoxygen.toList()
            val dissolvedoxygenProducer = ChartEntryModelProducer(dissolvedoxygenList)
            root.findViewById<ChartView>(R.id.chart_view_dissolvedoxygen).entryProducer = dissolvedoxygenProducer

            val tdsList: List<FloatEntry> = tds.toList()
            val tdsProducer = ChartEntryModelProducer(tdsList)
            root.findViewById<ChartView>(R.id.chart_view_tds).entryProducer = tdsProducer

            val turbidityList: List<FloatEntry> = turbidity.toList()
            val turbidityProducer = ChartEntryModelProducer(turbidityList)
            root.findViewById<ChartView>(R.id.chart_view_turbidity).entryProducer = turbidityProducer


        }else{
            Log.i("MMMMMMMM", "no displayChart")
        }
    }

}