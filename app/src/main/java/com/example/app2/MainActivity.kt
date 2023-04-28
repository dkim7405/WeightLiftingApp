package com.example.app2

import android.content.Intent
import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.example.app2.buttons.LogsActivity
import com.example.app2.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()


        binding.mainImageButtonAnalysis.setOnClickListener {
        }

        binding.mainImageButtonLogs.setOnClickListener {
            val intentLogs = Intent(this, LogsActivity::class.java)
            startActivity(intentLogs)
        }

        binding.mainImageButtonVideos.setOnClickListener {

        }

        binding.mainImageButtonTimer.setOnClickListener {

        }

        binding.mainImageButtonSettings.setOnClickListener {

        }

    }
}