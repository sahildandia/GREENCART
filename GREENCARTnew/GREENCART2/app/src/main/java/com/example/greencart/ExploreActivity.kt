package com.example.greencart

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView

class ExploreActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_explore)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_explore
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    true
                }
                R.id.nav_explore -> true // already here
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

        // Setup products list
        val sampleProducts = listOf(
            Product(name = "Bamboo Toothbrush", brand = "EcoSmile", price = "$3.99", ecoScore = 92),
            Product(name = "Reusable Glass Bottle", brand = "PureSip", price = "$12.99", ecoScore = 88),
            Product(name = "Organic Cotton Tote", brand = "GreenCarry", price = "$6.49", ecoScore = 85),
            Product(name = "Stainless Steel Straw Set", brand = "EcoSip", price = "$4.49", ecoScore = 90),
            Product(name = "Recycled Paper Notebook", brand = "EarthNotes", price = "$2.49", ecoScore = 80)
        )
        val rv = findViewById<RecyclerView>(R.id.rv_products)
        rv.adapter = ProductAdapter(sampleProducts)
    }
}
