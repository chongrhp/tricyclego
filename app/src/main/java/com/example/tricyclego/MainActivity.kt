package com.example.tricyclego

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.tricyclego.databinding.ActivityMainBinding
import com.example.tricyclego.fragments.AccountProfile
import com.example.tricyclego.fragments.ActivityFragment
import com.example.tricyclego.fragments.ChatFragment
import com.example.tricyclego.fragments.MapFragment
import com.example.tricyclego.fragments.ReportFragment

class MainActivity : AppCompatActivity() {
    private val fragActivity = ActivityFragment()
    private val fragChat = ChatFragment()
    private val fragReport = ReportFragment()
    private val fragMap = MapFragment()
    private val fragAccount = AccountProfile()
    private val fragmentManager = supportFragmentManager
    private lateinit var binding : ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this


        fragmentManager.beginTransaction().apply {
            add(R.id.flFragment1, fragMap, "goMap")
        }.commit()

        binding.bottomNavigation.setOnItemSelectedListener {

         when(it.itemId){
             R.id.mnuHome -> showFragments(fragMap, true)
             R.id.mnuActivity -> showFragments(fragActivity, false)
             R.id.mnuChat -> showFragments(fragChat, false)
             R.id.mnuReport -> showFragments(fragReport, false)
             R.id.mnuAccount -> showFragments(fragAccount, false)
         }
            true


        }

    }

    private fun showFragment(isHome: Boolean){
        binding.flFragment.isVisible = !isHome
        binding.flFragment1.isVisible = isHome

    }

    private fun showFragments(fragment: Fragment, isHome: Boolean){
        supportFragmentManager.beginTransaction().apply {
            if(!isHome)replace(R.id.flFragment, fragment).commit()
            showFragment(isHome)
        }
    }


}

