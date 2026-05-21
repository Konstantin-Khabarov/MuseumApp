package com.example.museumapp.ui.halls

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.museumapp.data.repository.HallItem
import com.example.museumapp.databinding.ItemHallBinding

class HallAdapter(private val onClick: (HallItem) -> Unit) :
    ListAdapter<HallItem, HallAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(private val binding: ItemHallBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: HallItem) {
            binding.textHallName.text = item.name?.takeIf { it.isNotBlank() } ?: "Без названия"

            val parts = listOfNotNull(
                item.hallNumber?.let { "№$it" },
                item.museumName
            )
            binding.textHallNumberAndMuseum.text = parts.joinToString(" · ")

            binding.root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemHallBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<HallItem>() {
            override fun areItemsTheSame(a: HallItem, b: HallItem) = a.hallId == b.hallId
            override fun areContentsTheSame(a: HallItem, b: HallItem) = a == b
        }
    }
}
