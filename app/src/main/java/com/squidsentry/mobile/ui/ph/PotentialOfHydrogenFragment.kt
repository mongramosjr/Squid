package com.squidsentry.mobile.ui.ph

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.Navigation
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entriesOf
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.views.chart.ChartView
import com.squidsentry.mobile.Feed
import com.squidsentry.mobile.R
import com.squidsentry.mobile.ThingSpeak
import com.squidsentry.mobile.databinding.FragmentPotentialOfHydrogenBinding
import com.squidsentry.mobile.ui.home.HomeViewModel
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class PotentialOfHydrogenFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel

    private var _binding: FragmentPotentialOfHydrogenBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    companion object {
        fun newInstance() = PotentialOfHydrogenFragment()
    }

    private lateinit var viewModel: PotentialOfHydrogenViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("PHHHHHHHHH", "onCreate")
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        // Call the ThingSpeak API
        homeViewModel.getThingSpeakData()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.i("PHHHHHHHHH", "onCreateView")

        // NOTE: Using binding
        //return inflater.inflate(R.layout.fragment_potential_of_hydrogen, container, false)
        _binding = FragmentPotentialOfHydrogenBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.i("PHHHHHHHHH", "onViewCreated")
        homeViewModel.thingSpeakData.observe(viewLifecycleOwner) { thingSpeakData ->
            displayChart(thingSpeakData, view)
        }
        // NOTE: Using binding instead of findViewById
        //view.findViewById<Toolbar>(R.id.fragment_ph_toolbar).setNavigationIcon(R.drawable.arrow_back_24)
        binding.fragmentPhToolbar.setNavigationIcon(R.drawable.arrow_back_24)
        binding.fragmentPhToolbar.setNavigationOnClickListener { view ->
            Navigation.findNavController(view).navigate(R.id.action_potentialOfHydrogenFragment_to_homeFragment)
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
        val format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssX")

        if (thingSpeakData != null) {
            feeds = thingSpeakData.feeds?.listIterator()

            var pH = mutableListOf <FloatEntry>().apply {  }

            var last_measure_pH: Float = 0f

            var date_last_pH: LocalDateTime? = null

            Log.i("PHHHHHHHHH", "prepping to display")

            if(feeds!=null) {

                var idx_ph: Int = 0
                var idx_temperature: Int = 0
                var idx_salinity: Int = 0
                var idx_dissolvedoxygen: Int = 0
                var idx_tds: Int = 0
                var idx_turbidity: Int = 0

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
            view.findViewById<ChartView>(R.id.chart_fragment_pH).entryProducer = phProducer
            if (date_last_pH != null) {
                view.findViewById<TextView>(R.id.date_last_measure_pH_fragment).text = date_last_pH.format(dateTimeFormatter).toString()
                view.findViewById<TextView>(R.id.last_measure_pH_fragment).text = last_measure_pH.toString()
            }
        }else{
            Log.i("PHHHHHHHHH", "no displayChart")
            displayEmptyChart(view, R.id.chart_fragment_pH, 1f)
        }
    }

    private fun displayEmptyChart(view: View, id: Int, value: Float)
    {
        view.findViewById<ChartView>(id)
            .setModel(entryModelOf(entriesOf(value, value, value,value,value, value)))
    }

}