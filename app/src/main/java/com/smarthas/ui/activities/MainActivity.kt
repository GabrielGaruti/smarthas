package com.smarthas.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.smarthas.R
import com.smarthas.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Setup Navigation
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        
        // Setup BottomNavigationView
        val bottomNav = binding.bottomNavigation
        NavigationUI.setupWithNavController(bottomNav, navController)
        
        // Handle navigation item selections
        bottomNav.setOnItemSelectedListener { menuItem ->
            NavigationUI.onNavDestinationSelected(menuItem, navController) ||
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    navController.navigate(R.id.homeFragment)
                    true
                }
                R.id.nav_new_measurement -> {
                    navController.navigate(R.id.newMeasurementFragment)
                    true
                }
                R.id.nav_history -> {
                    navController.navigate(R.id.historyFragment)
                    true
                }
                R.id.nav_credits -> {
                    navController.navigate(R.id.creditsFragment)
                    true
                }
                else -> false
            }
        }
    }
}
