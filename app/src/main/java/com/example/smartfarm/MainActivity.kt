package com.example.smartfarm

import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.smartfarm.databinding.ActivityMainBinding
import com.example.smartfarm.databinding.CustomToolbarBinding
import com.example.smartfarm.mqtt.MqttNotificationHandler
import com.example.smartfarm.util.AppNotifier
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var navController: NavController

    @Inject
    lateinit var mqttNotifHandler: MqttNotificationHandler

    @Inject
    lateinit var notifier: AppNotifier

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        delegate.localNightMode = androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        navController = navHostFragment.navController

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_profile, R.id.navigation_notifications
            )
        )
        navView.setupWithNavController(navController)

        // hide bottom navbar from certain area
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val hideOn = setOf(R.id.loginFragment, R.id.registerFragment,  R.id.navigation_dailyInformationsFragment, R.id.dailyInputFragment, R.id.addCoopFragment)
            navView.visibility = if (destination.id in hideOn) View.GONE else View.VISIBLE

            val showTopBarOn = setOf(
                R.id.navigation_home,
                R.id.addCoopFragment,
                R.id.navigation_profile,
                R.id.navigation_notifications,
            )

            binding.topBar.root.visibility =
                if (destination.id in showTopBarOn) View.VISIBLE else View.GONE
        }

        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid   // atau pakai user_id dari backend kamu

        if (userId != null) {
            Log.d("MQTT_NOTIF", "Starting mqtt notif for userId=$userId")
            mqttNotifHandler.start(userId)
        } else {
            Log.d("MQTT_NOTIF", "User is null, mqtt notif not started")
        }

    }

    override fun onStart() {
        super.onStart()
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid
        if (userId != null) {
            mqttNotifHandler.start(userId)
        }
    }

    override fun onBackPressed() {
        val current = navController.currentDestination?.id
        val prev = navController.previousBackStackEntry?.destination?.id

        if (current == R.id.loginFragment) {
            moveTaskToBack(true)
            return
        }

        if ((prev == R.id.loginFragment || prev == R.id.registerFragment) && current != R.id.registerFragment) {
            finish()
            return
        }

        val handled = navController.navigateUp()
        if (!handled) {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
//        mqttNotifHandler.stop()
    }
}