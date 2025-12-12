package com.antigravity.businessapp.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.antigravity.businessapp.R
import com.antigravity.businessapp.data.Item
import com.antigravity.businessapp.data.Party

class PartyAdapter(private val onItemClick: (Party) -> Unit) : ListAdapter<Party, PartyAdapter.PartyViewHolder>(PartyDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_party, parent, false)
        return PartyViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: PartyViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PartyViewHolder(itemView: View, val onItemClick: (Party) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tv_party_name)
        private val tvContact: TextView = itemView.findViewById(R.id.tv_party_contact)
        private val tvType: TextView = itemView.findViewById(R.id.tv_party_type)

        fun bind(party: Party) {
            tvName.text = party.name
            tvContact.text = party.contact
            tvType.text = party.type
            itemView.setOnClickListener { onItemClick(party) }
        }
    }

    class PartyDiffCallback : DiffUtil.ItemCallback<Party>() {
        override fun areItemsTheSame(oldItem: Party, newItem: Party): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Party, newItem: Party): Boolean = oldItem == newItem
    }
}

class StockAdapter(private val onItemClick: (Item) -> Unit) : ListAdapter<Item, StockAdapter.StockViewHolder>(StockDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_stock, parent, false)
        return StockViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: StockViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class StockViewHolder(itemView: View, val onItemClick: (Item) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tv_item_name)
        private val tvStock: TextView = itemView.findViewById(R.id.tv_item_stock)
        private val tvPrice: TextView = itemView.findViewById(R.id.tv_item_price)

        fun bind(item: Item) {
            tvName.text = item.name
            tvStock.text = "Qty: ${item.stockQuantity}"
            tvPrice.text = "₹ ${item.sellingRate}"
            
            if (item.stockQuantity <= item.lowStockLimit) {
                tvStock.setTextColor(android.graphics.Color.RED)
            } else {
                tvStock.setTextColor(android.graphics.Color.BLACK)
            }
            
            itemView.setOnClickListener { onItemClick(item) }
        }
    }

    class StockDiffCallback : DiffUtil.ItemCallback<Item>() {
        override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean = oldItem == newItem
    }
}
