package com.example.android.projemanage.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import android.widget.Toast
import com.example.android.projemanage.R

class Splash_Screen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash__screen)

        window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,  //Inorder to hide status bar
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
        )

        Handler().postDelayed({

            var currentUserID = FireStoreClass().getCurrentUserId()

            if(currentUserID.isNotEmpty()){
                startActivity(Intent(this,MainActivity::class.java))

            }else {
                val intent = Intent(this, IntroActivity::class.java)
                startActivity(intent)
            }
            finish()
        },2500)

    }
}
