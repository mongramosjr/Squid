package com.squidsentry.mobile.ui.myself

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.squidsentry.mobile.databinding.FragmentMyselfBinding

class MyselfFragment : Fragment() {

    private var _binding: FragmentMyselfBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val myselfViewModel =
            ViewModelProvider(this).get(MyselfViewModel::class.java)

        _binding = FragmentMyselfBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textMyself
        myselfViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}