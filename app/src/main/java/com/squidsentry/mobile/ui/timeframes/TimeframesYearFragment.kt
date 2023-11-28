package com.squidsentry.mobile.ui.timeframes

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squidsentry.mobile.R
import com.squidsentry.mobile.databinding.FragmentTimeframesYearBinding

class TimeframesYearFragment : Fragment() {

    private var _binding: FragmentTimeframesYearBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentTimeframesYearBinding.inflate(inflater, container, false)
        return binding.root
        //return inflater.inflate(R.layout.fragment_timeframes_year, container, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}