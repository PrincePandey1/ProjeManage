package com.example.android.projemanage.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.example.android.projemanage.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_sign_in.*
import models.User

class SignIn : BaseActivity() {

    private  lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        auth = FirebaseAuth.getInstance()

        window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
        )

        btn_sign_in.setOnClickListener {
            signInRegisteredUser()
        }
        setupActionBar()
    }


    private fun setupActionBar(){
        setSupportActionBar(toolbar_sign_in_activity)

        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black)
        }
        toolbar_sign_in_activity.setNavigationOnClickListener {
            onBackPressed()
        }

    }

    //<------Signing user using email and password----->

    private fun signInRegisteredUser(){
        val email: String = et_email_signin.text.toString().trim { it <= ' ' }
        val password: String = et_password_signin.text.toString().trim { it <= ' ' }

        if (validateForm(email, password)){
            showProgressDialog("please wait")


            //Authenticating user using email and password
            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task->
                        if (task.isSuccessful) {
                            hideProgressDialog()
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("Sign in", "signInWithEmail:success")
                            val user = auth.currentUser
                            startActivity(Intent(this, MainActivity::class.java))
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("Sign in", "signInWithEmail:failure", task.exception)
                            Toast.makeText(baseContext, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show()

                        }
                    }

        }
    }
    private fun validateForm( email: String, password:String): Boolean {

        return when {
            TextUtils.isEmpty(email) -> {
                showErrorSnackBar("Please enter a email")
                false
            }
            TextUtils.isEmpty(password) -> {
                showErrorSnackBar("Please enter a Password")
                false
            }
            else -> {
                return true
            }
        }
    }

    fun signInSuccess(loggedInUser: User?) {
        hideProgressDialog()
        startActivity(Intent(this, MainActivity::class.java))
        finish()

    }
}