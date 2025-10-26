package com.example.greencart

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ProductAdapter(
    private var items: List<Product>
) : RecyclerView.Adapter<ProductAdapter.ProductVH>() {

    class ProductVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvProductName)
        private val tvBrand: TextView = itemView.findViewById(R.id.tvProductBrand)
        private val tvPrice: TextView = itemView.findViewById(R.id.tvProductPrice)
        private val tvEco: TextView = itemView.findViewById(R.id.tvEcoScore)

        fun bind(item: Product) {
            tvName.text = item.name
            tvBrand.text = item.brand
            tvPrice.text = item.price
            tvEco.text = "Eco ${item.ecoScore}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductVH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ProductVH(view)
    }

    override fun onBindViewHolder(holder: ProductVH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<Product>) {
        items = newItems
        notifyDataSetChanged()
    }
}
