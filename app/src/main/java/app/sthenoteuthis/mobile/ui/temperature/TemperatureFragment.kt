package app.sthenoteuthis.mobile.ui.temperature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.viewpager2.widget.ViewPager2
import app.sthenoteuthis.mobile.R
import app.sthenoteuthis.mobile.SquidUtils
import app.sthenoteuthis.mobile.adapter.TimeframesPagerAdapter
import app.sthenoteuthis.mobile.databinding.FragmentTemperatureBinding
import app.sthenoteuthis.mobile.ui.viewmodel.TEMPERATURE
import app.sthenoteuthis.mobile.ui.viewmodel.ThingSpeakViewModel
import app.sthenoteuthis.mobile.ui.viewmodel.TimeframeViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.tabs.TabLayout
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale


class TemperatureFragment : Fragment() {

    private var _binding: FragmentTemperatureBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var adapterTimeframes: TimeframesPagerAdapter
    private lateinit var timeframesSelectorButton : MaterialButton
    private lateinit var timeframesDate: LocalDate

    //Note: can communicate between children fragments
    lateinit var thingspeakViewModel: ThingSpeakViewModel
    lateinit var timeframeViewModel: TimeframeViewModel
    // I cant make this work
    //private val thingspeakViewModel: ThingSpeakViewModel by viewModels()
    //private val timeframeViewModel: TimeframeViewModel by viewModels()

    // TODO: What the fuck is this, no need to call this object
    companion object {
        fun newInstance() = TemperatureFragment()

        private const val TAG = "TemperatureFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        timeframeViewModel = ViewModelProvider(requireActivity())[TimeframeViewModel::class.java]
        thingspeakViewModel = ViewModelProvider(requireActivity())[ThingSpeakViewModel::class.java]

        //set the water parameter
        timeframeViewModel.selectedWaterParameter(TEMPERATURE)
        timeframeViewModel.selectedWaterParameterUom(" °C")
        // Call the ThingSpeak API
        //TODO: query based on the current date and timeframe
        // getWaterQuality(selected_date, timeframe)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTemperatureBinding.inflate(inflater, container, false)

        viewPager = binding.differentTimeframesPager

        tabLayout = binding.differentTimeframesTablayout

        timeframesSelectorButton =  binding.differentTimeframesDateSelector

        binding.aboutTemperature.setText(
            HtmlCompat.fromHtml(getString(R.string.about_temperature_in_philippines),
                HtmlCompat.FROM_HTML_MODE_COMPACT
            ))

        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select date")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        adapterTimeframes = TimeframesPagerAdapter(this.childFragmentManager, lifecycle)
        viewPager.adapter = adapterTimeframes

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(timeframeTab: TabLayout.Tab) {
                viewPager.currentItem = timeframeTab.position
                timeframeViewModel.selectedTabPosition(viewPager.currentItem)
            }
            override fun onTabUnselected(timeframeTab: TabLayout.Tab) {}
            override fun onTabReselected(timeframeTab: TabLayout.Tab) {}
        })

        viewPager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                tabLayout.getTabAt(position)?.select()
            }
        })

        datePicker.addOnPositiveButtonClickListener {
            val sdf = SimpleDateFormat("EEE, MMM dd", Locale.getDefault())
            val date = sdf.format(it)
            // change to localdate
            val timeframesDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
            Toast.makeText(binding.root.context, date, Toast.LENGTH_SHORT).show()
            timeframesSelectorButton.text = date

            // query based on the selected date and timeframe
            // getWaterQuality(selected_date, timeframe)
            // #1. query ThingSpeak
            // #2. send changes in date
            if (SquidUtils.isDeviceOffline(requireContext())) {
                thingspeakViewModel.queryFeeds(Instant.ofEpochMilli(it), viewPager.currentItem, true)
            }else{
                thingspeakViewModel.fetchWaterQuality(Instant.ofEpochMilli(it), viewPager.currentItem, true)
            }
            timeframeViewModel.selectedTimeframesDate(timeframesDate)
        }

        timeframesSelectorButton.setOnClickListener {
            datePicker.show(this.childFragmentManager, "timeframes")
        }

        timeframeViewModel.timeframesDate.observe(viewLifecycleOwner){selectedDate ->
            val formatter = DateTimeFormatter.ofPattern("EEE, MMM dd")
                .withZone(ZoneId.systemDefault())
            val dateformatted = formatter.format(selectedDate)
            timeframesSelectorButton.text = dateformatted.toString()
        }


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fragmentTemperatureToolbar.setNavigationIcon(R.drawable.arrow_back_24)
        binding.fragmentTemperatureToolbar.setTitle(TEMPERATURE)
        binding.fragmentTemperatureToolbar.setNavigationOnClickListener { me: View ->
            Navigation.findNavController(me).navigate(R.id.action_temperatureFragment_to_homeFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}