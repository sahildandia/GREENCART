package com.example.greencart

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_home
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true // already here
                R.id.nav_explore -> {
                    startActivity(Intent(this, ExploreActivity::class.java))
                    true
                }
                R.id.nav_rewards -> {
                    startActivity(Intent(this, RewardsActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }

        // Quick links
        findViewById<android.view.View>(R.id.btn_scan_product).setOnClickListener {
            startActivity(Intent(this, ScanActivity::class.java))
        }
        findViewById<android.view.View>(R.id.btn_browse_brands).setOnClickListener {
            startActivity(Intent(this, ExploreActivity::class.java))
        }
        findViewById<android.view.View>(R.id.btn_eco_tips).setOnClickListener {
            startActivity(Intent(this, TipsActivity::class.java))
        }
        // Map Challenges to Community for now (assumption based on spec)
        findViewById<android.view.View>(R.id.btn_challenges).setOnClickListener {
            startActivity(Intent(this, CommunityActivity::class.java))
        }
        // Open Learn
        findViewById<android.view.View>(R.id.btn_open_learn).setOnClickListener {
            startActivity(Intent(this, LearnActivity::class.java))
        }

        // Handle search submit -> open Explore with query
        val searchBar = findViewById<EditText>(R.id.search_bar)
        searchBar.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                val q = v.text?.toString()?.trim().orEmpty()
                val intent = Intent(this, ExploreActivity::class.java)
                intent.putExtra("query", q)
                startActivity(intent)
                true
            } else false
        }
    }
}
