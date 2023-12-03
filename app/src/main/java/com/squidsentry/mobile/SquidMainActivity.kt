package com.squidsentry.mobile

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.os.Debug
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.squidsentry.mobile.databinding.ActivityMainSquidBinding
import com.squidsentry.mobile.ui.login.EmailPhoneLoginViewModel
import com.squidsentry.mobile.ui.login.EmailPhoneLoginViewModelFactory
import com.squidsentry.mobile.ui.viewmodel.ThingSpeakViewModel


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainSquidBinding

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    //Note: can communicate between children fragments
    lateinit var thingspeakViewModel: ThingSpeakViewModel
    lateinit var emailPhoneloginViewModel: EmailPhoneLoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {

        val isDebuggable = 0 != applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE
        Log.d("HHHHHHHH", "onCreate --> " + this.toString())




        thingspeakViewModel = ViewModelProvider(this)[ThingSpeakViewModel::class.java]
        thingspeakViewModel.defaultValues()

        Log.d("HHHHHHHH", thingspeakViewModel.toString() + " --> " + this.toString())

        // for login
        emailPhoneloginViewModel = ViewModelProvider(this,
            EmailPhoneLoginViewModelFactory())[EmailPhoneLoginViewModel::class.java]

        super.onCreate(savedInstanceState)

        binding = ActivityMainSquidBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //setContentView(R.layout.activity_main_squid)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main_squid) as NavHostFragment
        navController = navHostFragment.navController

        val navViewSquid: BottomNavigationView = binding.navViewSquid
        navViewSquid.setupWithNavController(navController)

        // Setup the ActionBar with navController and 3 top level destinations
        //appBarConfiguration = AppBarConfiguration(
        //    setOf(R.id.navigation_home, R.id.navigation_devices,  R.id.navigation_myself)
        //)
        //setupActionBarWithNavController(navController, appBarConfiguration)

    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration)
    }
}