package com.example.tricyclego.registration

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.tricyclego.Login
import com.example.tricyclego.R
import com.example.tricyclego.data.Users
import com.example.tricyclego.data.user_type
import com.example.tricyclego.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File


@Suppress("DEPRECATION")
class Register : AppCompatActivity() {
    private lateinit var binding : ActivityRegisterBinding
    private lateinit var mAuth : FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storageRef: StorageReference
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var startActivityLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryLauncher: ActivityResultLauncher<String>

    private var imageUri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_register)
        binding.lifecycleOwner = this

        //Initialize firebase
        initStorage()
        mAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        //initialize variables and values
        val txtFirstName = binding.txtRegisterFirstName
        val txtMiddleName = binding.txtRegisterMiddleName
        val txtLastName = binding.txtRegisterLastName
        val txtEmail = binding.txtRegisterEmail
        val txtContactNo = binding.txtRegisterContact
        val txtAddress = binding.txtRegisterAddress
        val txtPassword = binding.txtRegisterUserPassword
        val txtConfirmPassword = binding.txtRegisteConfirmPassword
        val txtUserName = binding.txtRegisterUserName


        val viewModel = ViewModelProvider(this)[RegisterViewModel::class.java]
        binding.registerViewModel = viewModel

        //Livedata for validation of entry

        viewModel.validateEntry.observe(this, Observer {validated ->
            if(validated){

                //If validated then proceed to registration of authorized user
                viewModel.registerNewUser(mAuth, txtEmail.text.toString(), txtPassword.text.toString())
                viewModel.registerUser.observe(this, Observer {success ->
                    if(success) {

                        //Prepare user registration data class
                        val user = Users(txtFirstName.text.toString(),
                            txtMiddleName.text.toString(),
                            txtLastName.text.toString(),
                            txtEmail.text.toString(),
                            txtContactNo.text.toString(),
                            txtAddress.text.toString(),
                            viewModel.userId.value.toString() ,
                            viewModel.imageUserId.value.toString(),
                            user_type)

                        //Add new user to firebase collect
                        viewModel.addUser(user)

                        //Observe registration for new user
                        viewModel.addUser.observe(this, Observer {registered ->
                            if(registered) {
                                Toast.makeText(applicationContext,"Registration Successful!",Toast.LENGTH_SHORT).show()
                                val myIntent = Intent(this@Register, Login::class.java)
                                startActivity(myIntent)
                                finish()
                            }
                            else Toast.makeText(applicationContext,"User Registration Failed!",Toast.LENGTH_SHORT).show()
                        })
                    }
                    else Toast.makeText(applicationContext,"Registration Failed!",Toast.LENGTH_SHORT).show()
                })
            } else Toast.makeText(applicationContext,viewModel.resultEmpty.value,Toast.LENGTH_SHORT).show()
            binding.registerProgressBar.isVisible = false
        })


        //if image uploaded then start checking registration
        viewModel.isUploadImage.observe(this, Observer {isUploaded ->

           if(isUploaded){
               Toast.makeText(applicationContext,"Image uploaded.",Toast.LENGTH_SHORT).show()
               viewModel.checkEmpty(
                   txtFirstName.text.toString(),
                   txtMiddleName.text.toString(),
                   txtLastName.text.toString(),
                   txtEmail.text.toString(),
                   txtContactNo.text.toString(),
                   txtAddress.text.toString(),
                   txtPassword.text.toString(),
                   txtConfirmPassword.text.toString())
           } else {
               Toast.makeText(applicationContext,"Failed ${viewModel.resultEmpty}",Toast.LENGTH_SHORT).show()
               binding.registerProgressBar.isVisible = false
           }
       })

        binding.txtLoginScreen.setOnClickListener {
            val myIntent = Intent(this@Register, Login::class.java)
            startActivity(myIntent)
            finish()
        }

        //user must be the same with email address
        binding.txtRegisterEmail.setOnFocusChangeListener { _, b ->
            if(!b) txtUserName.setText(txtEmail.text.toString())
        }

        //Launch activity for camera
        startActivityLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ){result: ActivityResult ->
            if(result.resultCode == RESULT_OK){

                val file = File(Environment.getExternalStorageDirectory(), "MyPhoto.jpg")
                val uri = FileProvider.getUriForFile(this,this.applicationContext.packageName + ".provider",file)

                imageUri = uri
                val bitmap = (result.data?.extras?.get("data")) as? Bitmap
                    ?: return@registerForActivityResult
                Glide.with(this)
                    .load(bitmap)
                    .centerCrop()
                    .circleCrop()
                    .into(binding.imageView2)

            }
        }

        //Launch gallery directory
        galleryLauncher = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ){uri: Uri? ->
            if(uri != null){
                imageUri = uri
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver,uri)
                //binding.imageView.setImageBitmap(bitmap)
                Glide.with(this)
                    .load(bitmap)
                    .centerCrop()
                    .circleCrop()
                    .into(binding.imageView2)
            }
        }

        //begin image uploading....
        binding.btnRegisUser.setOnClickListener {
            //checkEmpty()
            if(imageUri != null) {
                binding.registerProgressBar.isVisible = true
                storageRef = storageRef.child(System.currentTimeMillis().toString())
                imageUri?.let {
                    viewModel.uploadUserImage(storageRef, it,binding.txtRegisterFirstName.text.toString())
                    //Toast.makeText(applicationContext, "image id: $imageId", Toast.LENGTH_SHORT).show()
                }
            } else if(imageUri == null) Toast.makeText(applicationContext,"Please provide image. Thank you",Toast.LENGTH_SHORT).show()
            else if(viewModel.validateEntry.value == false) Toast.makeText(applicationContext,"Validate all entries. Thank you!",Toast.LENGTH_SHORT).show()
        }


        //Check permission for image source
        binding.imageButton.setOnClickListener {
            //showDialog() //avoid launching camera
            galleryLauncher.launch("image/*")
        }
    }

    //Show dialog permission
    private fun showDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Dialog Title")
        dialogBuilder.setMessage("Dialog Message")
        dialogBuilder.setPositiveButton("Camera"){dialog, _ ->
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100)
            } else {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityLauncher.launch(intent)
            }
            dialog.dismiss()
        }

        dialogBuilder.setNegativeButton("Gallery"){ dialog, _ ->
            galleryLauncher.launch("image/*")
            dialog.dismiss()
        }

        val dialog = dialogBuilder.create()
        dialog.show()
    }

    //Permission require during image lookup
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityLauncher.launch(intent)
        }
    }

    private fun initStorage(){
        //initialize firebase objects
        storageRef = FirebaseStorage.getInstance().reference.child("images")
        firebaseFirestore = FirebaseFirestore.getInstance()


    }
}