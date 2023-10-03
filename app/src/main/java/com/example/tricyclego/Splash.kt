package com.example.tricyclego

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.example.tricyclego.databinding.ActivitySplashBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Splash : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private var SPLASH_DELAY : Long = 2000
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)
        val imgView = binding.imageView3

        Glide
            .with(this.applicationContext)
            .load(R.drawable.loadingmeter)
            .override(150,120)
            .into(imgView)

        CoroutineScope(Dispatchers.Main).launch {
            delay(SPLASH_DELAY)
            val myIntent = Intent(this@Splash, Login::class.java)
            startActivity(myIntent)
            finish()
        }
    }
}