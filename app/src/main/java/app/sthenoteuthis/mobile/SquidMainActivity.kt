package app.sthenoteuthis.mobile

import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import androidx.room.Room
import app.sthenoteuthis.mobile.data.SquidDatabase
import app.sthenoteuthis.mobile.data.ThingSpeakApiRepository
import app.sthenoteuthis.mobile.data.ThingSpeakRepository
import app.sthenoteuthis.mobile.databinding.ActivityMainSquidBinding
import app.sthenoteuthis.mobile.ui.login.EmailPhoneLoginViewModel
import app.sthenoteuthis.mobile.ui.login.EmailPhoneLoginViewModelFactory
import app.sthenoteuthis.mobile.ui.viewmodel.FirebaseViewModel
import app.sthenoteuthis.mobile.ui.viewmodel.ThingSpeakViewModel
import app.sthenoteuthis.mobile.ui.viewmodel.ThingSpeakViewModelFactory
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainSquidBinding

    // NavController
    private lateinit var navController: NavController
    // AppBarConfiguration
    private lateinit var appBarConfiguration: AppBarConfiguration
    // Bottom Bar Navigation
    private lateinit var navViewSquid: BottomNavigationView

    //Note: can communicate between children fragments
    private lateinit var firebaseViewModel: FirebaseViewModel

    // RoomDatabase
    private val applicationScope = CoroutineScope(SupervisorJob())
    // Using by lazy so the database and the repository are only created when they're needed
    // rather than when the application starts
    // note this is a singleton instance
    private val database by lazy { SquidDatabase.getDatabase(this, applicationScope) }

    // ThingSpeakViewModel
    private val thingSpeakViewModelFactory: ThingSpeakViewModelFactory by lazy {
        val repository = ThingSpeakRepository(database.feedEntityDao(), ThingSpeakApiRepository()) // Initialize your repository here
        ThingSpeakViewModelFactory(repository)
    }
    private val thingspeakViewModel: ThingSpeakViewModel by viewModels { thingSpeakViewModelFactory }



    // TODO: remove emailPhoneloginViewModel
    private lateinit var emailPhoneloginViewModel: EmailPhoneLoginViewModel


    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        val isDebuggable = 0 != applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE
        Log.d("HHHHHHHH", "onCreate --> " + this.toString())

        thingspeakViewModel.defaultValues()

        firebaseViewModel = ViewModelProvider(this)[FirebaseViewModel::class.java]
        firebaseViewModel.initFirebaseAuth()

        Log.d("HHHHHHHH", thingspeakViewModel.toString() + " --> " + this.toString())

        // for login
        emailPhoneloginViewModel = ViewModelProvider(this,
            EmailPhoneLoginViewModelFactory()
        )[EmailPhoneLoginViewModel::class.java]

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

        // TODO: Remove this test
        // Use IO instead of MAIN dispatcher
        CoroutineScope(Dispatchers.IO).launch {
            Log.d("HAHAHAHAMONGCOUNT", thingspeakViewModel.countFeedsLocal().toString())
        }

        CoroutineScope(Dispatchers.IO).launch {
            Log.d("HAHAHAHAMONGCOUNTUSER", database.loggedInAccountDao().size().toString())
        }

    }

    public override fun onStart() {
        super.onStart()
        Log.w(TAG, "onStart")

        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = firebaseViewModel.auth?.currentUser
        if (currentUser != null) {
            reload()
        }
    }

    private fun reload() {
        firebaseViewModel.auth?.currentUser!!.reload().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this.applicationContext, "Reload successful!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this.applicationContext, "Failed to reload user.", Toast.LENGTH_SHORT).show()
            }
        }
    }



    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration)
    }
}