package com.example.greencart

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class ChallengeAdapter(
    private val items: List<Challenge>,
    private val completedIds: MutableSet<String>,
    private val onComplete: (Challenge) -> Unit
) : RecyclerView.Adapter<ChallengeAdapter.ChallengeVH>() {

    inner class ChallengeVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvChTitle)
        private val tvDesc: TextView = itemView.findViewById(R.id.tvChDesc)
        private val tvReward: TextView = itemView.findViewById(R.id.tvChReward)
        private val btnComplete: MaterialButton = itemView.findViewById(R.id.btnComplete)

        fun bind(item: Challenge) {
            tvTitle.text = item.title
            tvDesc.text = item.description
            tvReward.text = "Reward +${item.rewardPoints} pts"

            val isDone = completedIds.contains(item.id)
            btnComplete.isEnabled = !isDone
            btnComplete.text = if (isDone) "Completed" else "Complete"

            btnComplete.setOnClickListener {
                if (!completedIds.contains(item.id)) {
                    onComplete(item)
                    notifyItemChanged(bindingAdapterPosition)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChallengeVH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_challenge, parent, false)
        return ChallengeVH(view)
    }

    override fun onBindViewHolder(holder: ChallengeVH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
