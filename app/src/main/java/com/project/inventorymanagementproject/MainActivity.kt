package com.project.inventorymanagementproject

import android.R.attr.fragment
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.project.inventorymanagementproject.interfaces.NavigationListener
import com.project.inventorymanagementproject.ui.HomeFragment


class MainActivity : AppCompatActivity(), NavigationListener {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()

        findViewById<BottomNavigationView>(R.id.nav_view).setupWithNavController(navController)
    }

    public override fun openBottomNav(){
        findViewById<BottomNavigationView>(R.id.nav_view).isVisible = true
    }

    public override fun closeBottomNav() {
        findViewById<BottomNavigationView>(R.id.nav_view).isVisible = false
    }
}