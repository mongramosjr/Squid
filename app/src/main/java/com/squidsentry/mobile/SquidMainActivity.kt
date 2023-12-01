package com.squidsentry.mobile

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.squidsentry.mobile.databinding.ActivityMainSquidBinding
import com.squidsentry.mobile.ui.viewmodel.ThingSpeakViewModel


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainSquidBinding

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    //Note: can communicate between children fragments
    lateinit var thingspeakViewModel: ThingSpeakViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainSquidBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //setContentView(R.layout.activity_main_squid)

        thingspeakViewModel = ViewModelProvider(this)[ThingSpeakViewModel::class.java]

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
/*
    fun onCreadte(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager.findFragmentById(
            R.id.nav_host_container
        ) as NavHostFragment
        navController = navHostFragment.navController

        // Setup the bottom navigation view with navController
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNavigationView.setupWithNavController(navController)

        // Setup the ActionBar with navController and 3 top level destinations
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.titleScreen, R.id.leaderboard,  R.id.register)
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
    }
*/
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration)
    }
}