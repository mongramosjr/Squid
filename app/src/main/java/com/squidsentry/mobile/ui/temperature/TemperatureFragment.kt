package com.squidsentry.mobile.ui.temperature

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squidsentry.mobile.R
import com.squidsentry.mobile.ui.home.HomeViewModel

class TemperatureFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    companion object {
        fun newInstance() = TemperatureFragment()
    }

    private lateinit var viewModel: TemperatureViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("TemperatureHHHHHHHH", "onCreate")
        //homeViewModel = HomeViewModel()
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.i("TemperatureHHHHHHHH", "onCreateView")
        return inflater.inflate(R.layout.fragment_temperature, container, false)
    }

}