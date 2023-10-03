package com.example.tricyclego

import android.content.SharedPreferences
import android.widget.ProgressBar
import androidx.core.view.isVisible
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tricyclego.data.user_type
import com.example.tricyclego.data.varPassName
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginViewModel: ViewModel() {
    private val _loginResult = MutableLiveData<Boolean>()
    val loginResult : LiveData<Boolean> get() = _loginResult

    private val _userId = MutableLiveData<String>()
    val userId : LiveData<String> get() = _userId
    private val _stringTemp = MutableLiveData<String>()
    val stringTemp : LiveData<String> get() = _stringTemp
    private val _userLogin = MutableLiveData<Boolean>()
    val userLogin : LiveData<Boolean> get() = _userLogin

    fun performLogin(loginFirebase: FirebaseAuth,  username: String, password:String,
                     uSerKey:String, sharedPreferences: SharedPreferences, progress: ProgressBar){
        loginFirebase.signInWithEmailAndPassword(username, password)
            .addOnCompleteListener { task ->
                if(task.isSuccessful) {
                    val curUserKey = task.result.user?.uid.toString()
                    _userId.value = curUserKey
                    _userLogin.value = true

                    if(curUserKey != uSerKey) getUserProfile(curUserKey, sharedPreferences)
                    else _loginResult.value = true
                } else {
                    _stringTemp.value = "Invalid <font color='#0F9D58'><i>user name</i></font> and <font color='#0F9D58'><i>password</i></font>. Try again!"
                    progress.isVisible = false
                }
            }

            .addOnFailureListener {
                _userLogin.value = false
                _stringTemp.value = "System offline..."
                progress.isVisible = false
            }
    }


    private fun getUserProfile(userIDKey: String, sharedPreferences: SharedPreferences){
        val db = FirebaseFirestore.getInstance()
        val collectionRef = db.collection("users")
        val query = collectionRef
            .whereEqualTo("user_type", user_type)

        query.get()
            .addOnSuccessListener {querySnapShot ->
                var isLogin = false
                for(documentSnapShot in querySnapShot.documents){
                    val users = documentSnapShot.data
                    val userId = users?.get("userId").toString()
                    if(userId == userIDKey){

                        createPreference(sharedPreferences, userId,
                        users?.get("email").toString(),
                            users?.get("email").toString(),
                            users?.get("firstname").toString(),
                            users?.get("middlename").toString(),
                            users?.get("lastname").toString(),
                            users?.get("address").toString(),
                            users?.get("contactno").toString(),
                            users?.get("userImageId").toString()
                        )
                        isLogin = true
                        varPassName = users?.get("firstname").toString() +
                                " " +users?.get("lastname").toString()
                    }
                    _loginResult.value = isLogin
                    _stringTemp.value = "Not registered user."
                }
            }

            .addOnFailureListener {
                _stringTemp.value = "No records found"
            }
    }

    private fun createPreference(
        sharedPreferences: SharedPreferences,
        userId: String,
        userName: String,
        firstName: String,
        middleName: String,
        lastName: String,
        userEmail: String,
        userAddress: String,
        userContact: String,
        userImageId:String){
        val editor = sharedPreferences.edit()
        editor.putString("user_id",userId)
        editor.putString("user_name",userName)
        editor.putString("first_name",firstName)
        editor.putString("middle_name",middleName)
        editor.putString("last_name",lastName)
        editor.putString("email",userEmail)
        editor.putString("address",userAddress)
        editor.putString("contact_no",userContact)
        editor.putString("userImageId",userImageId)
        editor.apply()
    }

}