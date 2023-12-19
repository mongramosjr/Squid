package app.sthenoteuthis.mobile.ui.turbidity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.tabs.TabLayout
import app.sthenoteuthis.mobile.R
import app.sthenoteuthis.mobile.adapter.TimeframesPagerAdapter
import app.sthenoteuthis.mobile.databinding.FragmentTurbidityBinding
import app.sthenoteuthis.mobile.ui.viewmodel.TURBIDITY
import app.sthenoteuthis.mobile.ui.viewmodel.ThingSpeakViewModel
import app.sthenoteuthis.mobile.ui.viewmodel.TimeframeViewModel
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale


class TurbidityFragment : Fragment() {

    private var _binding: FragmentTurbidityBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var adapterTimeframes: TimeframesPagerAdapter
    private lateinit var timeframesSelectorButton : MaterialButton
    private lateinit var timeframesDate: LocalDate

    //Note: can communicate between children fragments
    lateinit var thingspeakViewModel: ThingSpeakViewModel
    lateinit var timeframeViewModel: TimeframeViewModel

    // TODO: What the fuck is this, no need to call this object
    companion object {
        fun newInstance() = TurbidityFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("TurbidityHHHHHHHH", "onCreate")
        timeframeViewModel = ViewModelProvider(requireActivity())[TimeframeViewModel::class.java]
        thingspeakViewModel = ViewModelProvider(requireActivity())[ThingSpeakViewModel::class.java]

        //set the water parameter
        timeframeViewModel.selectedWaterParameter(TURBIDITY)
        timeframeViewModel.selectedWaterParameterUom(" NTU")
        // Call the ThingSpeak API
        //TODO: query based on the current date and timeframe
        // getWaterQuality(selected_date, timeframe)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.i("TurbidityHHHHHHHH", "onCreateView")
        _binding = FragmentTurbidityBinding.inflate(inflater, container, false)

        viewPager = binding.differentTimeframesPager

        tabLayout = binding.differentTimeframesTablayout

        timeframesSelectorButton =  binding.differentTimeframesDateSelector

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
            // change the date
            timeframesDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
            Toast.makeText(binding.root.context, date, Toast.LENGTH_SHORT).show()
            timeframesSelectorButton.text = date

            //propagate the selected date to be used by all children
            Log.i("TemperatureHHHHHHHH", "setting date to display"
                    + viewPager.currentItem.toString() + ": "  + timeframesDate.toString())

            // query based on the selected date and timeframe
            // getWaterQuality(selected_date, timeframe)
            // #1. query ThingSpeak
            // #2. send changes in date
            thingspeakViewModel.getWaterQuality(Instant.ofEpochMilli(it), viewPager.currentItem, true)
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

        Log.i("PHHHHHHHHH", "onViewCreated")
        binding.fragmentTurbidityToolbar.setNavigationIcon(R.drawable.arrow_back_24)
        binding.fragmentTurbidityToolbar.setTitle(TURBIDITY)
        binding.fragmentTurbidityToolbar.setNavigationOnClickListener { me: View ->
            Navigation.findNavController(me).navigate(R.id.action_turbidityFragment_to_homeFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}