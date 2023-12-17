package com.squidsentry.mobile.ui.myself

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.squidsentry.mobile.R
import com.squidsentry.mobile.databinding.FragmentMyselfBinding
import com.squidsentry.mobile.ui.ProgressFragment
import com.squidsentry.mobile.ui.viewmodel.FirebaseViewModel
import com.google.firebase.auth.FirebaseUser

class MyselfFragment : ProgressFragment() {

    private var _binding: FragmentMyselfBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var firebaseViewModel: FirebaseViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseViewModel = ViewModelProvider(requireActivity())[FirebaseViewModel::class.java]

        /*
        // November 20, 2023
        val navController = findNavController()
        val currentBackStackEntry = navController.currentBackStackEntry!!
        val savedStateHandle = currentBackStackEntry.savedStateHandle
        savedStateHandle.getLiveData<Boolean>(LoginFragment.LOGIN_SUCCESSFUL)
            .observe(currentBackStackEntry, Observer { success ->
                if (!success) {
                    //val startDestination = navController.graph.startDestinationId
                    //val navOptions = NavOptions.Builder()
                    //    .setPopUpTo(startDestination, true)
                    //    .build()
                    //navController.navigate(startDestination, null, navOptions)
                    print("TODO")
                }
            })
         */
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val myselfViewModel =
            ViewModelProvider(requireActivity())[MyselfViewModel::class.java]

        // Inflate the layout for this fragment
        //val view = inflater.inflate(R.layout.fragment_myself, container, false)
        _binding = FragmentMyselfBinding.inflate(inflater, container, false)

        val textView: TextView = binding.textMyself
        myselfViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.btn_to_login).setOnClickListener {
            Navigation.findNavController(view)
                .navigate(R.id.action_myselfFragment_to_loginFragment)
        }

        binding.btnToLogout.setOnClickListener {
            signOut()
        }

        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = firebaseViewModel.auth?.currentUser
        if (currentUser == null) {
            updateUIButtons(null)
        }else{
            updateUIButtons(currentUser)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun signOut() {
        firebaseViewModel.auth?.signOut()
        updateUIButtons(null)
    }
    private fun updateUIButtons(user: FirebaseUser?) {
        hideProgressBar()
        if (user != null) {
            binding.btnToLogin.visibility = View.GONE
            binding.btnToLogout.visibility = View.VISIBLE
        }else{
            binding.btnToLogin.visibility = View.VISIBLE
            binding.btnToLogout.visibility = View.GONE
            binding.textMyself.text = "Please login!"
        }

    }


}