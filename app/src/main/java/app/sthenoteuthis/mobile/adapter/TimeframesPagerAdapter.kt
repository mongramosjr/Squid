package app.sthenoteuthis.mobile.adapter


import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import app.sthenoteuthis.mobile.ui.timeframes.TimeframesDayFragment
import app.sthenoteuthis.mobile.ui.timeframes.TimeframesWeekFragment
import app.sthenoteuthis.mobile.ui.timeframes.TimeframesMonthFragment
import app.sthenoteuthis.mobile.ui.timeframes.TimeframesYearFragment

private const val NUM_TABS = 4

public class TimeframesPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return NUM_TABS
    }

    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> return TimeframesDayFragment()
            1 -> return TimeframesWeekFragment()
            2 -> return TimeframesMonthFragment()
            3 -> return TimeframesYearFragment()
        }
        return TimeframesDayFragment()
    }



}
