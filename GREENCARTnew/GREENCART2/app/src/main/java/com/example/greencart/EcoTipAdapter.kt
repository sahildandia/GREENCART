package com.example.greencart

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EcoTipAdapter(private val items: List<EcoTip>) : RecyclerView.Adapter<EcoTipAdapter.TipVH>() {
    class TipVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTipTitle)
        private val tvDesc: TextView = itemView.findViewById(R.id.tvTipDesc)
        fun bind(item: EcoTip) {
            tvTitle.text = item.title
            tvDesc.text = item.description
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TipVH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tip, parent, false)
        return TipVH(view)
    }

    override fun onBindViewHolder(holder: TipVH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}

