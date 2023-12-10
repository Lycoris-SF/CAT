package io.objectbox.example.kotlin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import io.objectbox.example.kotlin.databinding.ActivityNavmenuBinding

class NavmenuActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityNavmenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNavmenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.ob_home, R.id.data_manager
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Check if the intent has the desired destination
        if (intent.hasExtra(EXTRA_NAV_DESTINATION)) {
            val destinationId = intent.getIntExtra(EXTRA_NAV_DESTINATION, 0)
            navigateToDestination(navController, destinationId)
        }
    }

    private fun navigateToDestination(navController: NavController, destinationId: Int) {
        navController.navigate(destinationId)
    }

    companion object {
        const val EXTRA_NAV_DESTINATION = "extra_nav_destination"

        fun intent(context: Context, destinationId: Int): Intent {
            return Intent(context, NavmenuActivity::class.java).apply {
                putExtra(EXTRA_NAV_DESTINATION, destinationId)
            }
        }
        fun intentWithDestination(context: Context, destinationId: Int): Intent {
            return Intent(context, NavmenuActivity::class.java).apply {
                putExtra(EXTRA_NAV_DESTINATION, destinationId)
            }
        }
    }

    /*override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.navmenu, menu)
        return true
    }*/

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}