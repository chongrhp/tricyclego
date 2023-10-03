package com.example.tricyclego.fragments.chat

import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.tricyclego.data.Chats
import com.example.tricyclego.databinding.ChatMessagesBinding

class ChatViewHolder(val binding:ChatMessagesBinding):RecyclerView.ViewHolder(binding.root) {
        fun bind(chatMsg: Chats){
            binding.txtReceiver.text = chatMsg.messages
            binding.txtSender.text = chatMsg.messages
            if(chatMsg.passengerMsg) binding.txtReceiver.isVisible = false
            else binding.txtSender.isVisible = false
        }
}