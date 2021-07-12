package com.example.android.projemanage.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
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
import kotlinx.android.synthetic.main.activity_create_board.*
import models.Board
import java.io.IOException

class CreateBoard : BaseActivity() {

    companion object{
        const val READ_STORAGE_PERMISSION_CODE = 1
        const  val PICK_IMAGE_REQUEST_CODE = 2
    }

    private var mSelectedImageFileUrii: Uri? = null

    private lateinit var mUserName: String
    private  var mBoardImageURL : String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_board)

        window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
        )

        setUpActionBar()

        if (intent.hasExtra(Constants.NAME)){
            mUserName = intent.getStringExtra(Constants.NAME).toString()
        }

        iv_profile_card_user_image.setOnClickListener {
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)
                    ==PackageManager.PERMISSION_GRANTED){                     //To get the image from external storage
                showImageChooser()
            }else{
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        MyProfileActivity.READ_STORAGE_PERMISSION_CODE
                )
            }
        }
        btn_create_user.setOnClickListener {
            if(mSelectedImageFileUrii!=null){
                uploadBoardUserImage()
            }else{
                showProgressDialog("Please wait")
                createBoard()
            }
        }





    }//OnCreate




    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == READ_STORAGE_PERMISSION_CODE){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                showImageChooser()
            }
        }
    }//ovv1

    private fun showImageChooser(){
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)   //Permission to excess image from external Storage
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == MyProfileActivity.PICK_IMAGE_REQUEST_CODE && data!!.data != null){
            mSelectedImageFileUrii = data.data //It gives uri of the image

            try {
                Glide.with(this)
                        .load(mSelectedImageFileUrii)
                        .centerCrop()
                        .placeholder(R.drawable.ic_board_place_holder)
                        .into(iv_profile_card_user_image);
            }catch(e: IOException){
                e.printStackTrace()
            }
        }
    }


    fun boardCreatedSuccessfully(){

        setResult(Activity.RESULT_OK)
        hideProgressDialog()


    }


    private fun setUpActionBar() {
        setSupportActionBar(toolbar_card_board_activity)
        val actionbar = supportActionBar
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true)
            actionbar.setHomeAsUpIndicator(R.drawable.ic_white_arrow_back_24)
        }
        toolbar_card_board_activity.setNavigationOnClickListener {
            onBackPressed()
        }
    }


    private fun uploadBoardUserImage(){
        showProgressDialog("Please wait")
        if(mSelectedImageFileUrii != null){

            //Where we want to store the image in firebase
            val sRef: StorageReference =
                    FirebaseStorage.getInstance().reference.child(
                            "BOARD_IMAGE" + System.currentTimeMillis()
                                    + "." + getFileExtension(mSelectedImageFileUrii))

            sRef.putFile(mSelectedImageFileUrii!!).addOnSuccessListener{
                taskSnapshot ->
                Log.i(
                        "Board Image URL",taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                )
                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                    uri ->
                    Log.i("Download" , uri.toString())
                    mBoardImageURL = uri.toString()

                    createBoard()
                }
            }.addOnFailureListener {

                Toast.makeText(this,"Error",Toast.LENGTH_LONG).show()
                hideProgressDialog()
            }
        }
    }

    private fun createBoard() {
        val assignedUserArrayList: ArrayList<String> = ArrayList()
        assignedUserArrayList.add(getCurrentUserID())

        var board = Board(
                et_board_name.text.toString(),
                mBoardImageURL,
                mUserName,
                assignedUserArrayList
        )
        FireStoreClass().createBoard(this,board)
    }

    private fun getFileExtension(uri: Uri?): String?{
        return MimeTypeMap.getSingleton()
                .getExtensionFromMimeType(contentResolver.getType(uri!!))
    }

}//MainActivity