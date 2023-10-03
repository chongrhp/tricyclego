package com.example.tricyclego

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.tricyclego.data.varPassId
import com.example.tricyclego.data.varUserId
import com.example.tricyclego.databinding.ActivityLoginBinding
import com.example.tricyclego.databinding.LoginDialogBinding
import com.example.tricyclego.registration.Register
import com.google.firebase.auth.FirebaseAuth

class Login : AppCompatActivity() {
    private lateinit var binding : ActivityLoginBinding
    private lateinit var mAuth : FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        binding.lifecycleOwner = this


        //initialize all variables
        //MVVM view Model
        val viewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        binding.loginScreenViewModel = viewModel

        //get shared preferences
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val userPrefKey = sharedPreferences.getString("user_id","blank").toString()


        //Firebase authentication
        mAuth = FirebaseAuth.getInstance()
        val txtEmail = binding.txtLoginUser
        val txtPassword = binding.txtLoginPassword



        //Observe login if validated...
        viewModel.loginResult.observe(this) { success ->
            if (success) {
                //Toast.makeText(applicationContext, "Successfully Login", Toast.LENGTH_LONG).show()
                val intent = Intent(this, MainActivity::class.java)
                varUserId = viewModel.userId.value.toString()
                varPassId = varUserId
                startActivity(intent)
                finish()
            } //else {showMessage("Authentication Failed!")}
            binding.progressBar.isVisible = false
        }

        viewModel.userLogin.observe(this){
            if(!it) showMessage(viewModel.stringTemp.value.toString())
        }



        //Login user to firebase
        binding.btnLoginUser.setOnClickListener {


            if(txtEmail.text.isEmpty() && txtPassword.text.isEmpty()) showMessage("Please enter <font color='#0F9D58'><i>user name</i></font> or <font color='#0F9D58'><i>email address</i></font> and <font color='#0F9D58'><i>password</i></font>. Thank you!")
            else if(txtEmail.text.isEmpty()) showMessage("Please enter <font color='#0F9D58'><i>user name</i></font> or <font color='#0F9D58'><i>email address</i></font>. Thank you!")
            else if(txtPassword.text.isEmpty()) showMessage("Please enter <font color='#0F9D58'><i>password</i></font>. Thank you!")
            else{
                binding.progressBar.isVisible = true
                val email = txtEmail.text.toString()
                val password = txtPassword.text.toString()
                viewModel.performLogin(
                    mAuth, email, password,
                    userPrefKey, sharedPreferences, binding.progressBar
                )
            }
        }


        //Move to registration screen
        binding.txtRegisterScreen.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
            finish()
        }

    }

    private fun showMessage(msg: String){
        val alertMessage = AlertDialog.Builder(this)
        val dialogView = LayoutInflater.from(this).inflate(R.layout.login_dialog, null)
        val binding = LoginDialogBinding.bind(dialogView)

        val btnOk = binding.btnOk
        binding.txtLoginMessage.text = Html.fromHtml(msg)
        alertMessage.setView(dialogView)
        val showDialog = alertMessage.create()

        btnOk.setOnClickListener {
            showDialog.dismiss()
        }

        showDialog.show()
    }
}