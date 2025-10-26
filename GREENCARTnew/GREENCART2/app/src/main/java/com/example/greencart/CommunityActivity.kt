package com.example.greencart

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView

class CommunityActivity : AppCompatActivity() {
    private lateinit var adapter: ChallengeAdapter
    private lateinit var completed: MutableSet<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_community)

        val prefs = getSharedPreferences("eco_prefs", MODE_PRIVATE)
        completed = (prefs.getStringSet("completed_challenges", emptySet()) ?: emptySet()).toMutableSet()

        val challenges = listOf(
            Challenge("c1", "Zero Plastic Day", "Avoid buying any single-use plastic today.", 20),
            Challenge("c2", "Refill & Reuse", "Visit a refill station for soap or detergent.", 15),
            Challenge("c3", "Compost Start", "Set up a small compost bin for kitchen scraps.", 25),
            Challenge("c4", "Public Transit", "Take public transport or bike for one trip.", 10)
        )

        val rv = findViewById<RecyclerView>(R.id.rv_challenges)
        adapter = ChallengeAdapter(challenges, completed) { ch ->
            if (completed.add(ch.id)) {
                val newPoints = prefs.getInt("points", 0) + ch.rewardPoints
                prefs.edit()
                    .putStringSet("completed_challenges", completed)
                    .putInt("points", newPoints)
                    .apply()
                Toast.makeText(this, getString(R.string.challenge_completed_toast, ch.rewardPoints), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, getString(R.string.challenge_already_completed), Toast.LENGTH_SHORT).show()
            }
        }
        rv.adapter = adapter
    }
}
