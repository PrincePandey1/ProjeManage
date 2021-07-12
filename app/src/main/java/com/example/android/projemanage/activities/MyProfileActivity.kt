package com.example.android.projemanage.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.WindowManager
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.android.projemanage.R
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_my_profile.*
import models.User
import java.io.IOException

class MyProfileActivity : BaseActivity() {

    companion object{
        const val READ_STORAGE_PERMISSION_CODE = 1
        const val PICK_IMAGE_REQUEST_CODE = 2
    }

    private var mSelectedImageFileUri: Uri? = null
    private var mProfileImageURL : String = ""
    private lateinit var mUserDetails: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)

     //   <-------to change the status bar color----->
        if (Build.VERSION.SDK_INT >= 21) {
            val window = this.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = this.resources.getColor(R.color.sky_blue)
        }

        setupActionBar()

        FireStoreClass().loadUserData(this)



        iv_profile_user_image.setOnClickListener {
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)
                    ==PackageManager.PERMISSION_GRANTED){                     //To get the image from external storage
                showImageChooser()
            }else{
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        READ_STORAGE_PERMISSION_CODE)
            }
        }

        btn_update.setOnClickListener {
            if(mSelectedImageFileUri != null){
                uploadUserImage()
            }else{
                showProgressDialog("Please wait")
                updateUserProfileData()
            }
        }

    }//onCreate


    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,                //Permission To access external storage
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == READ_STORAGE_PERMISSION_CODE ){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                showImageChooser()
            }
        }else{
            Toast.makeText(this,"Oops, you just denied the permission for storage" , Toast.LENGTH_LONG).show()
        }
    }

    private fun showImageChooser(){
        val galleryIntent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)   //Permission to excess image from external Storage
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE_REQUEST_CODE && data!!.data != null){
            mSelectedImageFileUri = data.data //It gives uri of the image

            try {
                Glide.with(this)
                        .load(mSelectedImageFileUri)
                        .centerCrop()
                        .placeholder(R.drawable.ic_user_place_holder)
                        .into(iv_profile_user_image);
            }catch(e: IOException){
                e.printStackTrace()
            }

        }

    }

    private fun setupActionBar(){
        setSupportActionBar(toolbar_my_profile_activity)

        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_whiteback)
            actionBar.title = "My Profile"
        }
        toolbar_my_profile_activity.setNavigationOnClickListener {
            onBackPressed()
        }

    }
    fun setUserDataInUI(user: User){

        mUserDetails = user
        Glide.with(this)
                .load(user.image)
                .centerCrop()
                .placeholder(R.drawable.ic_user_place_holder)
                .into(iv_profile_user_image);

        et_name.setText(user.name)
        et_email.setText(user.email)
        if(user.mobile != 0L){
            et_mobile.setText(user.mobile.toString())
        }
    }

    private  fun updateUserProfileData(){
        val userHashMap = HashMap<String,Any>()
        var anyChangeMade = false

        if(mProfileImageURL.isNotEmpty() && mProfileImageURL != mUserDetails.image){
            userHashMap[Constants.IMAGE] = mProfileImageURL
            anyChangeMade = true
        }

        if(et_name.text.toString() != mUserDetails.name){
            userHashMap[Constants.NAME] = et_name.text.toString()
            anyChangeMade = true
        }

        if(et_mobile.text.toString() != mUserDetails.mobile.toString()){
            userHashMap[Constants.MOBILE] = et_mobile.text.toString().toLong()
            anyChangeMade = true
        }
        if (anyChangeMade)
            FireStoreClass().updateUserProfileData(this,userHashMap)
    }
    private fun uploadUserImage(){
        showProgressDialog("Please wait")
        if(mSelectedImageFileUri != null){

            //Where we want to store the image in firebase
            val sRef: StorageReference =
                    FirebaseStorage.getInstance().reference.child(
                            "USER_IMAGE" + System.currentTimeMillis()
                                    + "." + getFileExtension(mSelectedImageFileUri))

            sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener{
                taskSnapshot ->
                Log.e(
                        "Firebase Image URL",taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                )
                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                    uri ->
                    Log.i("Download" , uri.toString())
                    mProfileImageURL = uri.toString()

                    updateUserProfileData()
                }
            }.addOnFailureListener {

                Toast.makeText(this,"Error",Toast.LENGTH_LONG).show()
                hideProgressDialog()
            }
        }
    }
    //It shows the uri is png,jpeg etc.
    private fun getFileExtension(uri: Uri?): String?{
        return MimeTypeMap.getSingleton()
                .getExtensionFromMimeType(contentResolver.getType(uri!!))
    }

    fun profileUpdateSuccess(){
        hideProgressDialog()

        setResult(Activity.RESULT_OK)
        finish()
    }

}