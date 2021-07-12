package com.example.android.projemanage.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import com.example.android.projemanage.R
import kotlinx.android.synthetic.main.activity_intro.*

class IntroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,  //Inorder to hide status bar
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
        )

        btn_sign_up_intro.setOnClickListener{
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
        }

        btn_sign_in_intro.setOnClickListener {
            startActivity(Intent(this, SignIn::class.java))
        }

    }
}