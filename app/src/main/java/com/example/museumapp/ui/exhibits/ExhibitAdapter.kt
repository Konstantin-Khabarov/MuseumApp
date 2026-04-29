package com.example.museumapp.ui.exhibits

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.museumapp.data.model.Exhibit
import com.example.museumapp.databinding.ItemExhibitBinding

class ExhibitAdapter(
    private val onItemClick: (Exhibit) -> Unit
) : ListAdapter<Exhibit, ExhibitAdapter.ExhibitViewHolder>(ExhibitDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExhibitViewHolder {
        val binding = ItemExhibitBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ExhibitViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExhibitViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ExhibitViewHolder(
        private val binding: ItemExhibitBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(exhibit: Exhibit) {
            binding.textExhibitTitle.text = exhibit.title
            //binding.textExhibitDescription.text = exhibit.description.take(100) + "..."

            // 🔥 Клик передаёт все нужные данные
            binding.root.setOnClickListener {
                onItemClick(exhibit)
            }
        }
    }
}

class ExhibitDiffCallback : DiffUtil.ItemCallback<Exhibit>() {
    override fun areItemsTheSame(oldItem: Exhibit, newItem: Exhibit) =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Exhibit, newItem: Exhibit) =
        oldItem == newItem
}