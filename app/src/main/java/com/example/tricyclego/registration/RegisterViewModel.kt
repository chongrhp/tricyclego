package com.example.tricyclego.registration

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tricyclego.data.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference

class RegisterViewModel: ViewModel() {
    private val _validateEntry = MutableLiveData<Boolean>()
    val validateEntry : LiveData<Boolean> get() = _validateEntry

    private val _resultEmpty = MutableLiveData<String>()
    val resultEmpty : LiveData<String> get() = _resultEmpty

    private val _isUploadImage = MutableLiveData<Boolean>()
    val isUploadImage : LiveData<Boolean> get () = _isUploadImage

    private val _imageUserId = MutableLiveData<String>()
    val imageUserId : LiveData<String> get() = _imageUserId

    private val _registerUser = MutableLiveData<Boolean>()
    val registerUser : LiveData<Boolean> get()= _registerUser

    private val _userId = MutableLiveData<String>()
    val userId : LiveData<String> get() = _userId

    private val _addUser = MutableLiveData<Boolean>()
    val addUser : LiveData<Boolean> get() = _addUser


    fun uploadUserImage(storageRef: StorageReference,
                        imageUri: Uri,
                        imageName: String) {
            storageRef.putFile(imageUri)
                .addOnCompleteListener {task->
                if(task.isSuccessful){
                    storageRef.downloadUrl.addOnSuccessListener {uri->
                        val imageLink = uri.toString()
                        val db = Firebase.firestore

                        val userImage = hashMapOf(
                            "imageName" to imageName,
                            "imageLink" to imageLink,
                        )

                        db.collection("user_image")
                            .add(userImage)
                            .addOnSuccessListener {task ->
                                _imageUserId.value = task.id
                                _isUploadImage.value = true
                            }
                            .addOnFailureListener { _isUploadImage.value = false }
                    }
                } else _isUploadImage.value = false
                }
                .addOnFailureListener {
                    _resultEmpty.value = "${it.message}"
                }
    }

    fun addUser(user: Users){
        Firebase.firestore.collection("users")
            .add(user)
            .addOnCompleteListener { task ->
                _addUser.value = task.isSuccessful
            }
    }

    fun registerNewUser(regUser: FirebaseAuth, email:String, password: String){
        regUser.createUserWithEmailAndPassword(email,password)
            .addOnSuccessListener { task ->
                _userId.value = task.user?.uid.toString()
            }
            .addOnCompleteListener { task ->
                _registerUser.value = task.isSuccessful
            }
    }

    fun checkEmpty(firstname: String, middle_name: String, lastname: String, email:String,
                   contact_no: String, address:String, password:String,confirmPassword: String){
        val emptyString: Boolean

        if (firstname.isEmpty()) {
            emptyString = false
            _resultEmpty.value = "Please enter first name."
        }else if(middle_name.isEmpty()) {
            emptyString = false
            _resultEmpty.value = "Please enter middle name."
        }else if (lastname.isEmpty()){
            emptyString = false
            _resultEmpty.value = "Please enter last name."
        }else if (email.isEmpty()){
            emptyString = false
            _resultEmpty.value = "Please enter email address."
        }else if (contact_no.isEmpty()){
            emptyString = false
            _resultEmpty.value = "Please enter contact no"
        }else if (address.isEmpty()){
            emptyString = false
            _resultEmpty.value = "Please enter home address"
        }else if (password.isEmpty()){
            emptyString = false
            _resultEmpty.value = "password is empty"
        }else if (password != confirmPassword){
            emptyString = false
            _resultEmpty.value = "incorrect confirm password"
        } else emptyString = true

        _validateEntry.value = emptyString
    }


}