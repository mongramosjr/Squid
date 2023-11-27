package com.squidsentry.mobile.ui.turbidity

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squidsentry.mobile.R

class TurbidityFragment : Fragment() {

    companion object {
        fun newInstance() = TurbidityFragment()
    }

    private lateinit var viewModel: TurbidityViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_turbidity, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(TurbidityViewModel::class.java)
        // TODO: Use the ViewModel
    }

}