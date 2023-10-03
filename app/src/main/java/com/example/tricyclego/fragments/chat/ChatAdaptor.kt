package com.example.tricyclego.fragments.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tricyclego.data.Chats
import com.example.tricyclego.databinding.ChatMessagesBinding

class ChatAdaptor(private val chats:List<Chats>):RecyclerView.Adapter<ChatViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val inflate = LayoutInflater.from(parent.context)
        val binding = ChatMessagesBinding.inflate(inflate, parent, false )
        return ChatViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return chats.size
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(chats[position])
        holder.itemView.setOnClickListener {
            //
        }
    }
}