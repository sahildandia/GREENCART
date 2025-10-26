package com.example.greencart

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton

class RewardsActivity : AppCompatActivity() {
    private lateinit var tvPoints: TextView
    private lateinit var tvScans: TextView
    private var prizeAdapter: PrizeAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rewards)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_rewards
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    true
                }
                R.id.nav_explore -> {
                    startActivity(Intent(this, ExploreActivity::class.java))
                    true
                }
                R.id.nav_rewards -> true // already here
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }

        tvPoints = findViewById(R.id.tvRewardsPointsValue)
        tvScans = findViewById(R.id.tvRewardsScansValue)

        // Prizes RecyclerView setup
        val rvPrizes = findViewById<RecyclerView>(R.id.rvPrizes)
        rvPrizes.layoutManager = LinearLayoutManager(this)

        // Sample prizes (include a 5000 point prize)
        val samplePrizes = listOf(
            Prize(id = "p1", title = "Reusable Water Bottle", pointsRequired = 5000, imageResId = null),
            Prize(id = "p2", title = "Eco Tote Bag", pointsRequired = 2000, imageResId = null),
            Prize(id = "p3", title = "Plant-a-Tree Voucher", pointsRequired = 10000, imageResId = null)
        )

        val prefs = getSharedPreferences("eco_prefs", MODE_PRIVATE)
        val currentPoints = prefs.getInt("points", 0)

        prizeAdapter = PrizeAdapter(samplePrizes, currentPoints) { prize ->
            // Redeem handler: deduct points and persist
            val pointsNow = prefs.getInt("points", 0)
            if (pointsNow >= prize.pointsRequired) {
                val newPoints = pointsNow - prize.pointsRequired
                prefs.edit().putInt("points", newPoints).apply()
                prizeAdapter?.updateUserPoints(newPoints)
                tvPoints.text = newPoints.toString()
                Toast.makeText(this, "Redeemed: ${prize.title}", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Not enough points to redeem ${prize.title}", Toast.LENGTH_SHORT).show()
            }
        }
        rvPrizes.adapter = prizeAdapter

        // Redeem CTA (keeps existing behavior: show instructions)
        val btnRedeem = findViewById<MaterialButton>(R.id.btnRedeem)
        btnRedeem.setOnClickListener {
            Toast.makeText(this, "Tap a prize's Redeem button to claim it.", Toast.LENGTH_SHORT).show()
        }

        refreshSummary()
    }

    override fun onResume() {
        super.onResume()
        refreshSummary()
    }

    private fun refreshSummary() {
        val prefs = getSharedPreferences("eco_prefs", MODE_PRIVATE)
        val points = prefs.getInt("points", 0)
        val scans = prefs.getInt("scan_count", 0)
        tvPoints.text = points.toString()
        tvScans.text = scans.toString()
        prizeAdapter?.updateUserPoints(points)
    }
}
