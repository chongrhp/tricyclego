package com.example.tricyclego.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tricyclego.data.ChatId
import com.example.tricyclego.data.Chats
import com.example.tricyclego.data.chatEnabled
import com.example.tricyclego.data.chatId
import com.example.tricyclego.data.varDriverId
import com.example.tricyclego.data.varDriverName
import com.example.tricyclego.data.varPassName
import com.example.tricyclego.data.varUserId
import com.example.tricyclego.databinding.FragmentChatBinding
import com.example.tricyclego.fragments.chat.ChatAdaptor
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ChatFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adaptor: ChatAdaptor
    private val chatList = mutableListOf<Chats>()
    private lateinit var _binding : FragmentChatBinding
    val binding get() = _binding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        _binding.lifecycleOwner = this

        recyclerView = binding.chatRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())


        //Initialize variables
        if(chatId.isNotEmpty() && chatEnabled) loadChat()
        else if(chatId.isEmpty()) checkChat()


        val btnSend = binding.btnSendMessage
        val dbs = FirebaseFirestore.getInstance()
        val messageCollection = dbs.collection("chats")
        val chatMessage = messageCollection
            .whereEqualTo("chatId", chatId)

        chatMessage.addSnapshotListener { queries, e ->
            if(e != null){
                Toast.makeText(this.requireContext(),"New record is coming..",Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            for (docChange in queries!!.documentChanges) {
                when (docChange.type) {
                    DocumentChange.Type.ADDED -> {
                        // Handle newly added message
                        //val message = docChange.document.toObject(Message::class.java)
                        // Update your UI with the new message
                        //Toast.makeText(this.requireContext(),"Added ${docChange.document.data?.get("messages").toString()}",Toast.LENGTH_SHORT).show()
                    }
                    DocumentChange.Type.MODIFIED -> {
                        //docChange.document.id
                        // Handle modified message (if necessary)
                        //val updatedMessage = docChange.document.toObject(Message::class.java)
                        // Update your UI with the modified message
                        Toast.makeText(this.requireContext(),"Modified ${docChange.document.data["messages"].toString()}",Toast.LENGTH_SHORT).show()
                    }
                    DocumentChange.Type.REMOVED -> {
                        // Handle removed message (if necessary)
                        //val removedMessage = docChange.document.toObject(Message::class.java)
                        // Update your UI to remove the deleted message
                        //Toast.makeText(this.requireContext(),"Remove ${docChange.document.data?.get("messages").toString()}",Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }



        btnSend.setOnClickListener {
            if(chatEnabled) prepareMessage()

        }




        return binding.root
    }

    private fun prepareMessage(){
        val passengerId = varUserId
        val driverId = "333333"
        val dateChat = Timestamp.now()
        val messages = binding.edtChat.text.toString()
        val passengerMsg = true
        val chatting = Chats(chatId, passengerId, driverId, dateChat, messages, passengerMsg)
        sendMessage(chatting)
        chatList.add(chatting)
        adaptor = ChatAdaptor(chatList)
        adaptor.notifyItemInserted(chatList.size -1)
        recyclerView.adapter = adaptor
        recyclerView.scrollToPosition(chatList.size -1)
        binding.edtChat.text.clear()
    }

    private fun sendMessage(chatMsg: Chats){
        Firebase.firestore.collection("chats")
            .add(chatMsg)
            .addOnCompleteListener {

                Toast.makeText(context,"sending successfully!",Toast.LENGTH_SHORT).show()
            }

            .addOnFailureListener {
                Toast.makeText(context,"sending failed!",Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkChat() {

        val dbc = FirebaseFirestore.getInstance()
        val collectRef = dbc.collection("chatId")
            .whereEqualTo("passId", varUserId)
            .whereEqualTo("driverId", varDriverId)
            .whereEqualTo("chatOpen", true)

        collectRef.get()
            .addOnSuccessListener { task ->
                for (doc in task) {
                    chatId = doc.id
                    val cEnabled = doc.get("chatOpen") as Boolean
                    chatEnabled = cEnabled
                    loadChat()
                }
            }
            .addOnCompleteListener {}
    }

    private fun createChat(){
        val newChatId = ChatId(Timestamp.now(), varUserId, varDriverId, varPassName, varDriverName, true)
        val db = FirebaseFirestore.getInstance()
        val collectRef = db.collection("chatId")
        collectRef.add(newChatId)
            .addOnSuccessListener {
                chatId = it.id
                loadChat()
            }
    }

    private fun loadChat(){
        binding.progressBar3.isVisible = true
        val db= FirebaseFirestore.getInstance()
        val collectionRef = db.collection("chats")


        val query = collectionRef
            .whereEqualTo("chatId", chatId)
            //.whereEqualTo("passengerId", varUserId)

        query.get()
            .addOnSuccessListener {querySnapShot ->

                for(documents in querySnapShot.documents){
                    val chats = documents.data
                    val dateChat = chats?.get("dateChat") as Timestamp
                    val driverId = chats["driverId"].toString()
                    val messages = chats["messages"].toString()
                    val passengerId = chats["passengerId"].toString()
                    val passengerMsg = chats["passengerMsg"] as Boolean

                    val newChatMsg = Chats(chatId, passengerId,
                        driverId, dateChat, messages, passengerMsg)
                    chatList.add(newChatMsg)

                }

                adaptor = ChatAdaptor(chatList)
                recyclerView.adapter = adaptor
                Toast.makeText(this.requireContext(),"Successfully",Toast.LENGTH_SHORT).show()
                binding.progressBar3.isVisible = false
            }



            .addOnFailureListener {
                Toast.makeText(this.requireContext(), "Failed to load chat...", Toast.LENGTH_SHORT).show()
            }
    }


    override fun onDestroy() {
        super.onDestroy()
        // Remove the listener when the activity is destroyed to avoid memory leaks.
        //listenerRegistration.remove()
    }

}