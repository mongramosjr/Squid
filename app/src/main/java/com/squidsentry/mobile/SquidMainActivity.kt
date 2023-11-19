package com.squidsentry.mobile

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.squidsentry.mobile.databinding.ActivityMainSquidBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainSquidBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainSquidBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navViewSquid

        val navController = findNavController(R.id.nav_host_fragment_activity_main_squid)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        // val appBarConfiguration = AppBarConfiguration(
        //    setOf(
        //        R.id.navigation_home, R.id.navigation_devices, R.id.navigation_myself
        //    )
        //)
        // setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }
}