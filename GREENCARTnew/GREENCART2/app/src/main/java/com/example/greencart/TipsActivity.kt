package com.example.greencart

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView

class TipsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tips)

        val tips = listOf(
            EcoTip("Bring your own bag", "Keep a reusable tote in your car or backpack to avoid single-use bags."),
            EcoTip("Choose glass over plastic", "Glass and metal are easier to recycle and often reusable."),
            EcoTip("Buy in bulk", "Reduce packaging waste by buying staples in larger quantities."),
            EcoTip("Refill, don’t rebuy", "Use refill stations for detergents, soaps, and cleaners."),
            EcoTip("Compost organics", "Divert food scraps to compost to cut landfill methane.")
        )
        val rv = findViewById<RecyclerView>(R.id.rv_tips)
        rv.adapter = EcoTipAdapter(tips)
    }
}
