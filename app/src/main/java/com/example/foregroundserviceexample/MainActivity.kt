package com.example.foregroundserviceexample

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.foregroundserviceexample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnStart.setOnClickListener{

            val song = Song(
                "Lập trình hướng đối tượng",
                "Kteam",
                "https://yt3.ggpht.com/ytc/AMLnZu9MYAIcu9e1aZyFZq9L0sqvBIxgH4WYqs44s5qbCg=s900-c-k-c0x00ffffff-no-rj",
                "https://tainhacmienphi.biz/get/song/api/283123"
            )

            val bundle = Bundle().apply {
                putSerializable("song", song)
            }

            val intent = Intent(this, MyService::class.java)
            intent.putExtra("bundle", bundle)
            intent.putExtra("key_data_intent", binding.edt.text.toString())

            startService(intent)
        }

        binding.btnStop.setOnClickListener {
            val intent = Intent(this, MyService::class.java)

            stopService(intent)
        }



    }


}