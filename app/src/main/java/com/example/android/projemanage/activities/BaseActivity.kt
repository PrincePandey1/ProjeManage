package com.example.android.projemanage.activities

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.android.projemanage.R
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.progress_dialog.*

open class BaseActivity : AppCompatActivity() {

    private var doubleBackToExitPressedOnce = false

    private lateinit var mProgressDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)
    }

    fun showProgressDialog(text: String){

        mProgressDialog = Dialog(this)
        mProgressDialog.setContentView(R.layout.progress_dialog)
        mProgressDialog.tv_progress_text.text = text

        mProgressDialog.show()
    }

    fun hideProgressDialog(){
        mProgressDialog.dismiss()
    }

    fun getCurrentUserID(): String{ // This how we get current user Id
        return FirebaseAuth.getInstance().currentUser!!.uid
    }
    fun doubleBackToExit(){

        if(doubleBackToExitPressedOnce){
            super.onBackPressed()            // IF THE USER SATISFY THE CONDITION , THEN IT WILL CLOSE THE ACTIVITY
            return
        }

        this.doubleBackToExitPressedOnce = true   // IF IS CLICKED ONCE THEN IT GIVES THE TOAST
        Toast.makeText(this,"Please back once again to exit.",Toast.LENGTH_LONG).show()

        Handler().postDelayed({doubleBackToExitPressedOnce = false},2000)
    }

    fun showErrorSnackBar(message: String){
        val snackBar = Snackbar.make(findViewById(android.R.id.content),message,Snackbar.LENGTH_LONG)

        val snackBarView = snackBar.view
        snackBarView.setBackgroundColor(ContextCompat.getColor(this,
                R.color.snackbar_error_color))
        snackBar.show()    //Create layout of snackbar
    }

}