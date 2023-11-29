package com.squidsentry.mobile.ui.ph

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.squidsentry.mobile.R
import com.squidsentry.mobile.adapter.TimeframesPagerAdapter
import com.squidsentry.mobile.databinding.FragmentPotentialOfHydrogenBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PotentialOfHydrogenFragment : Fragment() {
    private var _binding: FragmentPotentialOfHydrogenBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var adapterTimeframes: TimeframesPagerAdapter

    private lateinit var timeframesSelectorButton : MaterialButton

    private lateinit var timeframesDate: Date
    /*
    NOTE: Testing CalenndarView
    private lateinit var calendarView: CalendarView
     */


    // TODO: What the fuck is this, no need to call this object
    companion object {
        fun newInstance() = PotentialOfHydrogenFragment()
    }

    private lateinit var viewModel: PotentialOfHydrogenViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("PHHHHHHHHH", "onCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.i("PHHHHHHHHH", "onCreateView")

        _binding = FragmentPotentialOfHydrogenBinding.inflate(inflater, container, false)

        viewPager = binding.differentTimeframesPager
        tabLayout = binding.differentTimeframesTablayout

        timeframesSelectorButton =  binding.differentTimeframesDateSelector

        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select date")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        adapterTimeframes = TimeframesPagerAdapter(this.childFragmentManager, lifecycle)
        viewPager.adapter = adapterTimeframes

        tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(timeframeTab: TabLayout.Tab) {
                    viewPager.currentItem = timeframeTab.position
            }
            override fun onTabUnselected(timeframeTab: TabLayout.Tab) {}
            override fun onTabReselected(timeframeTab: TabLayout.Tab) {}
        })

        viewPager.registerOnPageChangeCallback(object: OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                tabLayout.getTabAt(position)?.select()
            }
        })

        datePicker.addOnPositiveButtonClickListener {
            val sdf = SimpleDateFormat("EEE, MMM dd", Locale.getDefault())
            val date = sdf.format(it)
            timeframesDate = Date(it)
            Toast.makeText(binding.root.context, date, Toast.LENGTH_SHORT).show();
            timeframesSelectorButton.text = date
        }

        timeframesSelectorButton.setOnClickListener {
            datePicker.show(this.childFragmentManager, "timeframes")
        }

        /*
        NOTE: Testing CalenndarView
        calendarView = binding.differentTimeframesCalendar
        calendarView.setOnDateChangeListener(object: OnDateChangeListener {
            override fun onSelectedDayChange(
                view: CalendarView,
                year: Int,
                month: Int,
                dayOfMonth: Int
            ) {
                val msg = "Selected date Day: " + dayOfMonth + " Month : " + (month + 1) + " Year " + year;
                Toast.makeText(binding.root.context, msg, Toast.LENGTH_SHORT).show();
            }
        })
        */

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.i("PHHHHHHHHH", "onViewCreated")
        // NOTE: Using binding instead of findViewById
        //view.findViewById<Toolbar>(R.id.fragment_ph_toolbar).setNavigationIcon(R.drawable.arrow_back_24)
        binding.fragmentPhToolbar.setNavigationIcon(R.drawable.arrow_back_24)
        binding.fragmentPhToolbar.setNavigationOnClickListener { me: View ->
            Navigation.findNavController(me).navigate(R.id.action_potentialOfHydrogenFragment_to_homeFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
