package com.example.museumapp.ui.authors

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.museumapp.data.model.Author
import com.example.museumapp.databinding.ItemAuthorBinding

class AuthorAdapter(
    private val onItemClick: (Author) -> Unit
) : ListAdapter<Author, AuthorAdapter.AuthorViewHolder>(AuthorDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AuthorViewHolder {
        val binding = ItemAuthorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AuthorViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: AuthorViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class AuthorViewHolder(
        private val binding: ItemAuthorBinding,
        private val onItemClick: (Author) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(author: Author) {
            binding.textAuthorName.text = author.name
            binding.textAuthorId.text = "ID: ${author.id}"

            binding.root.setOnClickListener {
                onItemClick(author)
            }
        }
    }
}

class AuthorDiffCallback : DiffUtil.ItemCallback<Author>() {
    override fun areItemsTheSame(oldItem: Author, newItem: Author): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Author, newItem: Author): Boolean {
        return oldItem == newItem
    }
}