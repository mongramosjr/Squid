package com.squidsentry.mobile

import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.squidsentry.mobile.databinding.ActivityMainSquidBinding
import com.squidsentry.mobile.ui.login.EmailPhoneLoginViewModel
import com.squidsentry.mobile.ui.login.EmailPhoneLoginViewModelFactory
import com.squidsentry.mobile.ui.viewmodel.FirebaseViewModel
import com.squidsentry.mobile.ui.viewmodel.ThingSpeakViewModel


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainSquidBinding

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    //Note: can communicate between children fragments
    lateinit var thingspeakViewModel: ThingSpeakViewModel
    lateinit var firebaseViewModel: FirebaseViewModel



    lateinit var emailPhoneloginViewModel: EmailPhoneLoginViewModel
    lateinit var navViewSquid: BottomNavigationView


    override fun onCreate(savedInstanceState: Bundle?) {

        val isDebuggable = 0 != applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE
        Log.d("HHHHHHHH", "onCreate --> " + this.toString())

        thingspeakViewModel = ViewModelProvider(this)[ThingSpeakViewModel::class.java]
        thingspeakViewModel.defaultValues()

        firebaseViewModel = ViewModelProvider(this)[FirebaseViewModel::class.java]
        firebaseViewModel.initFirebaseAuth()


        Log.d("HHHHHHHH", thingspeakViewModel.toString() + " --> " + this.toString())

        // for login
        emailPhoneloginViewModel = ViewModelProvider(this,
            EmailPhoneLoginViewModelFactory())[EmailPhoneLoginViewModel::class.java]

        super.onCreate(savedInstanceState)

        binding = ActivityMainSquidBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //setContentView(R.layout.activity_main_squid)

        // NavHostFragment with navGraph navigation_main_activity
        val navHostFragment =
            supportFragmentManager.findFragmentById(binding.navHostFragmentActivityMainSquid.id) as NavHostFragment
        navController = navHostFragment.navController

        // bottom navigation bar
        navViewSquid = binding.navViewSquid

        // join bottom navigation bar and navController
        navViewSquid.setupWithNavController(navController)

        // Setup the ActionBar with navController and 3 top level destinations
        //appBarConfiguration = AppBarConfiguration(
        //    setOf(R.id.navigation_home, R.id.navigation_devices,  R.id.navigation_myself)
        //)
        //setupActionBarWithNavController(navController, appBarConfiguration)

        // change the visibility of bottom navigation bar
        navController.addOnDestinationChangedListener{ _, destination, _ ->
            when (destination.id) {
                R.id.loginFragment -> {
                    // Hide the bottom navigation bar
                    navViewSquid.visibility = View.GONE
                }
                R.id.signupFragment -> {
                    // Hide the bottom navigation bar
                    navViewSquid.visibility = View.GONE
                }
                else -> {
                    // Show the bottom navigation bar for other destinations
                    navViewSquid.visibility = View.VISIBLE
                }
            }
        }


        val intent = intent
        if (intent != null) {
            val receivedValue = intent.getStringExtra("key")

            // Use the received data to determine which fragment to show
            // Example: Use the value to decide which fragment to display
            if (receivedValue != null && receivedValue == "login") {
                //supportFragmentManager.beginTransaction()
                //    .replace(R.id.fragment_container, SecondFragment())
                //    .commit()
                //navController.navigate(R.id.action_myselfFragment_to_loginFragment);
            }

        }

    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = firebaseViewModel.auth?.currentUser
        if (currentUser != null) {
            //reload()
        }
    }



    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration)
    }
}