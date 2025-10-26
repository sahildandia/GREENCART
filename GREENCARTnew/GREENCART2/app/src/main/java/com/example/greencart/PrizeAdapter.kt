package com.example.greencart

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class PrizeAdapter(
    private val prizes: List<Prize>,
    private var userPoints: Int = 0,
    private val onRedeem: (Prize) -> Unit
) : RecyclerView.Adapter<PrizeAdapter.PrizeViewHolder>() {

    fun updateUserPoints(points: Int) {
        userPoints = points
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrizeViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_prize, parent, false)
        return PrizeViewHolder(v)
    }

    override fun onBindViewHolder(holder: PrizeViewHolder, position: Int) {
        val prize = prizes[position]
        holder.title.text = prize.title
        holder.points.text = "${prize.pointsRequired} points"
        if (prize.imageResId != null) {
            holder.img.setImageResource(prize.imageResId)
        } else {
            holder.img.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        val canRedeem = userPoints >= prize.pointsRequired
        holder.btnRedeem.isEnabled = canRedeem
        holder.btnRedeem.text = if (canRedeem) "Redeem" else "Need ${prize.pointsRequired - userPoints}"

        holder.btnRedeem.setOnClickListener {
            if (canRedeem) {
                onRedeem(prize)
            } else {
                Toast.makeText(holder.itemView.context, "Not enough points", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int = prizes.size

    class PrizeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.imgPrize)
        val title: TextView = view.findViewById(R.id.tvPrizeTitle)
        val points: TextView = view.findViewById(R.id.tvPrizePoints)
        val btnRedeem: MaterialButton = view.findViewById(R.id.btnPrizeRedeem)
    }
}

