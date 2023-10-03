package com.example.tricyclego.fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.tricyclego.Login
import com.example.tricyclego.databinding.FragmentAccountProfileBinding
import com.google.firebase.firestore.FirebaseFirestore

class AccountProfile : Fragment() {
    init {
        // Retain the fragment instance across configuration changes
        retainInstance = true
    }

    private lateinit var sharedPreferences : SharedPreferences
    private lateinit var _binding : FragmentAccountProfileBinding
    val binding get() = _binding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAccountProfileBinding.inflate(inflater, container, false)
        _binding.lifecycleOwner = this
        sharedPreferences = this.requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        val firstName = sharedPreferences.getString("first_name","No first name")
        val middleName = sharedPreferences.getString("middle_name","No first name")
        val lastName = sharedPreferences.getString("last_name","No first name")
        val email = sharedPreferences.getString("email","No first name")
        val contactNo = sharedPreferences.getString("contact_no","No Contact no.")
        val userImageId = sharedPreferences.getString("userImageId","No images")
        val userAddress = sharedPreferences.getString("address","No address")

        binding.txtFirstName.text = firstName
        binding.txtMiddleName.text = middleName
        binding.txtLastName.text = lastName
        binding.txtEmail.text = email
        binding.txtContactNo.text = contactNo
        binding.txtAddress.text = userAddress

        binding.btnLogout.setOnClickListener {
            val intent = Intent(requireActivity(),Login::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        getOneData(userImageId.toString())
        return binding.root
    }

    private fun getOneData(documentID: String){
        val db = FirebaseFirestore.getInstance()
        val collectionRef = db.collection("user_image")
        val documentRef = collectionRef.document(documentID)

        documentRef.get()
            .addOnSuccessListener { documentSnapShot ->
                if(documentSnapShot.exists()){
                    val image = documentSnapShot.data
                    val imageLink = image?.get("imageLink").toString()

                    Glide.with(binding.root.context)
                        .load(imageLink)
                        .centerCrop()
                        .circleCrop()
                        //.apply(RequestOptions.overrideOf(200, 200))
                        .into(binding.imageView)
                }
            }
    }


}