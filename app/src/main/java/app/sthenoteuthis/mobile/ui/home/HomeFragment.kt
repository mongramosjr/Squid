package app.sthenoteuthis.mobile.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entriesOf
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.views.chart.ChartView
import app.sthenoteuthis.mobile.data.model.Feed
import app.sthenoteuthis.mobile.R
import app.sthenoteuthis.mobile.SquidUtils
import app.sthenoteuthis.mobile.data.SquidDatabase
import app.sthenoteuthis.mobile.data.model.FeedEntityDao
import app.sthenoteuthis.mobile.databinding.FragmentHomeBinding
import app.sthenoteuthis.mobile.ui.viewmodel.DAILY_TIMEFRAME
import app.sthenoteuthis.mobile.ui.viewmodel.ThingSpeakViewModel
import app.sthenoteuthis.mobile.ui.viewmodel.TimeframeViewModel
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.random.Random


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    // remote database
    lateinit var thingspeakViewModel: ThingSpeakViewModel

    // local database
    private var feeds: MutableList<Feed> = mutableListOf()
    private lateinit var feedEntityDao: FeedEntityDao


    lateinit var timeframeViewModel: TimeframeViewModel


    companion object {
        private const val TAG = "HomeTab"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        timeframeViewModel = ViewModelProvider(requireActivity())[TimeframeViewModel::class.java]
        thingspeakViewModel = ViewModelProvider(requireActivity())[ThingSpeakViewModel::class.java]
        Log.d(TAG, thingspeakViewModel.toString() + " --> " + this.toString())

        val database = SquidDatabase.getDatabase(requireContext())
        feedEntityDao = database.feedEntityDao()

        // use the feeds stored locally, but first check the network
        if(SquidUtils.isDeviceOffline(requireContext())){
            queryRecentFeeds()
        }else{
            // Call the last entries in ThingSpeak
            fetchRecentWaterQuality()
        }


    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view: View = binding.root
        //val view = inflater.inflate(R.layout.fragment_home, container, false)

        // click to open the detailed graph of each parameters
        // (using findById instead of databinding)
        view.findViewById<TextView>(R.id.last_measure_pH).setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.action_homeFragment_to_potentialOfHydrogenFragment)
        }
        view.findViewById<CardView>(R.id.cardview_pH).setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.action_homeFragment_to_potentialOfHydrogenFragment)
        }
        view.findViewById<CardView>(R.id.cardview_dissolvedoxygen).setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.action_homeFragment_to_dissolvedOxygenFragment)
        }
        view.findViewById<CardView>(R.id.cardview_salinity).setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.action_homeFragment_to_salinityFragment)
        }
        view.findViewById<CardView>(R.id.cardview_tds).setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.action_homeFragment_to_tdsFragment)
        }
        view.findViewById<CardView>(R.id.cardview_temperature).setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.action_homeFragment_to_temperatureFragment)
        }
        view.findViewById<CardView>(R.id.cardview_turbidity).setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.action_homeFragment_to_turbidityFragment)
        }

        //fetch feeds remotely and display chart asyncronously
        fetchFeedsDisplayChart(view)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.aiQuery.setOnClickListener{
            // TODO: open dialog box with ChatGPT seach query with default interpretation
            // of the current water quality data
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun fetchFeedsDisplayChart(root: View) {

        thingspeakViewModel.isLoading.observe(viewLifecycleOwner){isLoading->
            // Set the result text to Loading
            if (isLoading) {
                //TODO: display chart with random
                fun getRandomEntries() = entriesOf( Random.nextFloat() * 16f)
                displayEmptyChart(root, R.id.chart_view_temperature, 7f)
                displayEmptyChart(root, R.id.chart_view_pH, 7f)
                displayEmptyChart(root, R.id.chart_view_tds, 7f)
                displayEmptyChart(root, R.id.chart_view_turbidity, 7f)
                displayEmptyChart(root, R.id.chart_view_dissolvedoxygen, 7f)
                displayEmptyChart(root, R.id.chart_view_salinity, 7f)
            }
        }

        thingspeakViewModel.isError.observe(viewLifecycleOwner) { isError ->
            // Hide display image and set the result text to the error message
            if (isError) {
                //TODO: display error message
                Log.i(TAG, "TODO: display error message")
            }
        }

        thingspeakViewModel.thingSpeakData.observe(viewLifecycleOwner){thingSpeakData ->
            displayChart(thingSpeakData, root)
        }

        // fetch feeds to be display on daily, weekly, monthly and yearly chart
        thingspeakViewModel.lastDateEntryCount.observe(viewLifecycleOwner){lastDateEntryCount ->
            if (lastDateEntryCount!=null) {
                Log.i(TAG, "COUNT DATE LAST ENTRY: $lastDateEntryCount")
                if(lastDateEntryCount.equals(1)) {
                    val lastDateEntry = thingspeakViewModel.lastDateEntry.value

                    if (lastDateEntry != null) {
                        if (SquidUtils.isDeviceOffline(requireContext())) {
                            queryFeedsAt(lastDateEntry)
                            thingspeakViewModel.setLastDateEntryCount(1)
                        }else{
                            fetchWaterQualityAt(lastDateEntry)
                            thingspeakViewModel.setLastDateEntryCount(1)
                        }
                    }
                }
            }
        }
    }

    private fun displayChart(thingSpeakData: List<Feed>, root: View){

        val feeds: ListIterator<Feed> = thingSpeakData.listIterator()
        displayChart(feeds, root)
    }

    private fun displayChart(feeds: ListIterator<Feed>, root: View){

        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        //2023-04-06T00:13:00Z
        //val format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssX")



        var pH = mutableListOf <FloatEntry>().apply {  }
        var temperature = mutableListOf <FloatEntry>().apply {  }
        var salinity = mutableListOf <FloatEntry>().apply {  }
        var dissolvedoxygen = mutableListOf <FloatEntry>().apply {  }
        var tds = mutableListOf <FloatEntry>().apply {  }
        var turbidity = mutableListOf <FloatEntry>().apply {  }

        var last_measure_pH: Float = 0f
        var last_measure_temperature: Float = 0f
        var last_measure_salinity: Float = 0f
        var last_measure_dissolvedoxygen: Float = 0f
        var last_measure_tds: Float = 0f
        var last_measure_turbidity: Float = 0f

        var date_last_pH: LocalDateTime? = null
        var date_last_temperature: LocalDateTime? = null
        var date_last_salinity: LocalDateTime? = null
        var date_last_dissolvedoxygen: LocalDateTime? = null
        var date_last_tds: LocalDateTime? = null
        var date_last_turbidity: LocalDateTime? = null

        var last_feed: Feed? = null



        var idx_ph: Int = 0
        var idx_temperature: Int = 0
        var idx_salinity: Int = 0
        var idx_dissolvedoxygen: Int = 0
        var idx_tds: Int = 0
        var idx_turbidity: Int = 0

        while (feeds.hasNext()) {
            val e = feeds.next()

            last_feed = e

            if(e.pH!=null) {
                pH.add(idx_ph, FloatEntry(idx_ph.toFloat(), e.pH!!.toFloat()))
                last_measure_pH = e.pH!!.toFloat()
                date_last_pH = OffsetDateTime.parse(e.createdAt).toLocalDateTime()
                idx_ph++
            }
            if(e.temperature!=null) {
                temperature.add(idx_temperature, FloatEntry(idx_temperature.toFloat(), e.temperature!!.toFloat()))
                last_measure_temperature = e.temperature!!.toFloat()
                date_last_temperature = OffsetDateTime.parse(e.createdAt).toLocalDateTime()
                idx_temperature++
            }
            if(e.salinity!=null) {
                salinity.add(idx_salinity, FloatEntry(idx_salinity.toFloat(), e.salinity!!.toFloat()))
                last_measure_salinity = e.salinity!!.toFloat()
                date_last_salinity = OffsetDateTime.parse(e.createdAt).toLocalDateTime()
                idx_salinity++
            }
            if(e.dissolvedOxygen!=null) {
                dissolvedoxygen.add(idx_dissolvedoxygen, FloatEntry(idx_dissolvedoxygen.toFloat(), e.dissolvedOxygen!!.toFloat()))
                last_measure_dissolvedoxygen = e.dissolvedOxygen!!.toFloat()
                date_last_dissolvedoxygen = OffsetDateTime.parse(e.createdAt).toLocalDateTime()
                idx_dissolvedoxygen++
            }
            if(e.tds!=null) {
                tds.add(idx_tds, FloatEntry(idx_tds.toFloat(), e.tds!!.toFloat()))
                last_measure_tds = e.tds!!.toFloat()
                date_last_tds = OffsetDateTime.parse(e.createdAt).toLocalDateTime()
                idx_tds++
            }
            if(e.turbidity!=null) {
                turbidity.add(idx_turbidity, FloatEntry(idx_turbidity.toFloat(), e.turbidity!!.toFloat()))
                last_measure_turbidity = e.turbidity!!.toFloat()
                date_last_turbidity = OffsetDateTime.parse(e.createdAt).toLocalDateTime()
                idx_turbidity++
            }
        }


        /* NOTE: another way of showing graph
        val phList: List<FloatEntry> = pH.toList()
        root.findViewById<ChartView>(R.id.chart_view_pH)
            .setModel(entryModelOf(phList))
        */
        val phList: List<FloatEntry> = pH.toList()
        val phProducer = ChartEntryModelProducer(phList)
        root.findViewById<ChartView>(R.id.chart_view_pH).entryProducer = phProducer
        if (date_last_pH != null) {
            root.findViewById<TextView>(R.id.date_last_measure_pH).text = date_last_pH.format(dateTimeFormatter).toString()
            root.findViewById<TextView>(R.id.last_measure_pH).text = last_measure_pH.toString()
        }

        val temperatureList: List<FloatEntry> = temperature.toList()
        val temperatureProducer = ChartEntryModelProducer(temperatureList)
        root.findViewById<ChartView>(R.id.chart_view_temperature).entryProducer = temperatureProducer
        if (date_last_temperature != null) {
            root.findViewById<TextView>(R.id.date_last_measure_temperature).text = date_last_temperature.format(dateTimeFormatter).toString()
            root.findViewById<TextView>(R.id.last_measure_temperature).text = last_measure_temperature.toString()
        }

        val salinityList: List<FloatEntry> = salinity.toList()
        val salinityProducer = ChartEntryModelProducer(salinityList)
        root.findViewById<ChartView>(R.id.chart_view_salinity).entryProducer = salinityProducer
        if (date_last_salinity != null) {
            root.findViewById<TextView>(R.id.date_last_measure_salinity).text = date_last_salinity.format(dateTimeFormatter).toString()
            root.findViewById<TextView>(R.id.last_measure_salinity).text = last_measure_salinity.toString()
        }

        val dissolvedoxygenList: List<FloatEntry> = dissolvedoxygen.toList()
        val dissolvedoxygenProducer = ChartEntryModelProducer(dissolvedoxygenList)
        root.findViewById<ChartView>(R.id.chart_view_dissolvedoxygen).entryProducer = dissolvedoxygenProducer
        if (date_last_dissolvedoxygen != null) {
            root.findViewById<TextView>(R.id.date_last_measure_dissolvedoxygen).text = date_last_dissolvedoxygen.format(dateTimeFormatter).toString()
            root.findViewById<TextView>(R.id.last_measure_dissolvedoxygen).text = last_measure_dissolvedoxygen.toString()
        }

        val tdsList: List<FloatEntry> = tds.toList()
        val tdsProducer = ChartEntryModelProducer(tdsList)
        root.findViewById<ChartView>(R.id.chart_view_tds).entryProducer = tdsProducer
        if (date_last_tds != null) {
            root.findViewById<TextView>(R.id.date_last_measure_tds).text = date_last_tds.format(dateTimeFormatter).toString()
            root.findViewById<TextView>(R.id.last_measure_tds).text = last_measure_tds.toString()
        }

        val turbidityList: List<FloatEntry> = turbidity.toList()
        val turbidityProducer = ChartEntryModelProducer(turbidityList)
        root.findViewById<ChartView>(R.id.chart_view_turbidity).entryProducer = turbidityProducer
        if (date_last_turbidity != null) {
            root.findViewById<TextView>(R.id.date_last_measure_turbidity).text = date_last_turbidity.format(dateTimeFormatter).toString()
            root.findViewById<TextView>(R.id.last_measure_turbidity).text = last_measure_turbidity.toString()
        }

        if(last_feed?.let { SquidUtils.isWaterQualityGood(it) } == true) {
            root.findViewById<TextView>(R.id.last_measure_status).text =
                getString(R.string.status_very_good)
        }else{
            root.findViewById<TextView>(R.id.last_measure_status).text =
                getString(R.string.status_unusual)
        }
    }

    private fun displayEmptyChart(view: View, id: Int, value: Float)
    {
        view.findViewById<ChartView>(id)
            .setModel(entryModelOf(entriesOf(value, value, value,value,value, value)))
    }

    private fun queryFeedsAt(dateNow: Instant = Instant.now()){
        Log.d(TAG, "queryFeedsAt --> $dateNow")
        val timeframesDate = dateNow.atZone(ZoneId.systemDefault()).toLocalDate()
        thingspeakViewModel.queryFeeds(dateNow, DAILY_TIMEFRAME, true)
        timeframeViewModel.selectedTimeframesDate(timeframesDate)
    }

    fun queryRecentFeeds(){
        if(!thingspeakViewModel.lastDateEntry.isInitialized) {
            thingspeakViewModel.queryLastFeeds()
        }
    }


    private fun fetchWaterQualityAt(dateNow: Instant = Instant.now()){
        Log.d(TAG, "fetchWaterQualityAt --> $dateNow")
        val timeframesDate = dateNow.atZone(ZoneId.systemDefault()).toLocalDate()
        thingspeakViewModel.fetchWaterQuality(dateNow, DAILY_TIMEFRAME, true)
        timeframeViewModel.selectedTimeframesDate(timeframesDate)
    }

    private fun fetchRecentWaterQuality(){
        if(!thingspeakViewModel.lastDateEntry.isInitialized) {
            thingspeakViewModel.fetchLastWaterQuality()
        }
    }


}