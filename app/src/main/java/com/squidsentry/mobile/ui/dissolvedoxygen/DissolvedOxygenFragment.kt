package com.squidsentry.mobile.ui.dissolvedoxygen

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squidsentry.mobile.R

class DissolvedOxygenFragment : Fragment() {

    companion object {
        fun newInstance() = DissolvedOxygenFragment()
    }

    private lateinit var viewModel: DissolvedOxygenViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this).get(DissolvedOxygenViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dissolved_oxygen, container, false)
    }
}