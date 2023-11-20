package com.squidsentry.mobile.ui.myself

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.squidsentry.mobile.R
import com.squidsentry.mobile.databinding.FragmentMyselfBinding
import com.squidsentry.mobile.ui.login.LoginFragment
import com.squidsentry.mobile.ui.login.LoginViewModel
import com.squidsentry.mobile.ui.login.LoginViewModelFactory

class MyselfFragment : Fragment() {

    private var _binding: FragmentMyselfBinding? = null

    private lateinit var loginViewModel: LoginViewModel


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val navController = findNavController()

        val currentBackStackEntry = navController.currentBackStackEntry!!
        val savedStateHandle = currentBackStackEntry.savedStateHandle
        savedStateHandle.getLiveData<Boolean>(LoginFragment.LOGIN_SUCCESSFUL)
            .observe(currentBackStackEntry, Observer { success ->
                if (!success) {
                    val startDestination = navController.graph.startDestinationId
                    val navOptions = NavOptions.Builder()
                        .setPopUpTo(startDestination, true)
                        .build()
                    navController.navigate(startDestination, null, navOptions)
                }
            })
    }

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

        //test
        //loginViewModel.logout()

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navController = findNavController()

        //navController.navigate(R.id.login_fragment)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}