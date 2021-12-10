package com.example.workwithcontentprovider

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import com.example.workwithcontentprovider.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding :ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        savedInstanceState.let { supportFragmentManager.beginTransaction()
            .replace(R.id.containerForFragment, MainFragment.newInstance()).commit() }
    }
}