package com.example.museumapp.ui.museums

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.museumapp.data.model.Museum
import com.example.museumapp.databinding.ItemMuseumBinding

class MuseumAdapter(
    private val onItemClick: (Museum) -> Unit
) : ListAdapter<Museum, MuseumAdapter.MuseumViewHolder>(MuseumDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MuseumViewHolder {
        val binding = ItemMuseumBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MuseumViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: MuseumViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MuseumViewHolder(
        private val binding: ItemMuseumBinding,
        private val onItemClick: (Museum) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(museum: Museum) {
            binding.textMuseumName.text = museum.name
            val location = listOfNotNull(museum.city.takeIf { it.isNotBlank() }, museum.country)
                .joinToString(", ")
            binding.textMuseumSubtitle.text = location.ifBlank { "Город не указан" }
            binding.root.setOnClickListener { onItemClick(museum) }
        }
    }
}

class MuseumDiffCallback : DiffUtil.ItemCallback<Museum>() {
    override fun areItemsTheSame(oldItem: Museum, newItem: Museum): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Museum, newItem: Museum): Boolean {
        return oldItem == newItem
    }
}
