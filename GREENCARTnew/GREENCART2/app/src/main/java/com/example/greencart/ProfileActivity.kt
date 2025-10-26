package com.example.greencart

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_profile
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { startActivity(Intent(this, HomeActivity::class.java)); true }
                R.id.nav_explore -> { startActivity(Intent(this, ExploreActivity::class.java)); true }
                R.id.nav_rewards -> { startActivity(Intent(this, RewardsActivity::class.java)); true }
                R.id.nav_profile -> true
                else -> false
            }
        }

        val tvName = findViewById<TextView>(R.id.tvName)
        val tvEmail = findViewById<TextView>(R.id.tvEmail)
        val tvMemberSince = findViewById<TextView>(R.id.tvMemberSince)
        val tvScanCount = findViewById<TextView>(R.id.tvScanCount)
        val tvPoints = findViewById<TextView>(R.id.tvPoints)
        val btnSignOut = findViewById<MaterialButton>(R.id.btnSignOut)

        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        // Populate user info
        val displayName = user?.displayName?.takeIf { it.isNotBlank() }
            ?: user?.email?.substringBefore("@")?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            ?: "Eco Shopper"
        val email = user?.email ?: "guest@example.com"
        tvName.text = displayName
        tvEmail.text = email

        val createdTs = user?.metadata?.creationTimestamp
        if (createdTs != null && createdTs > 0) {
            val dateStr = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(createdTs))
            tvMemberSince.text = getString(R.string.profile_member_since, dateStr)
        }

        // Load stats
        val prefs = getSharedPreferences("eco_prefs", MODE_PRIVATE)
        fun refreshStats() {
            val scans = prefs.getInt("scan_count", 0)
            val points = prefs.getInt("points", 0)
            tvScanCount.text = scans.toString()
            tvPoints.text = points.toString()
        }
        refreshStats()

        btnSignOut.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finishAffinity()
        }
    }
}
