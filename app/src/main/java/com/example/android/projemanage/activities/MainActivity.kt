package com.example.android.projemanage.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.android.projemanage.adapter.BoardItemAdapter
import com.example.android.projemanage.R
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceIdReceiver
import com.google.firebase.iid.internal.FirebaseInstanceIdInternal
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging.getInstance
import com.google.firebase.storage.FirebaseStorage.getInstance
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.nav_header_main.*
import models.Board
import models.User

class MainActivity : BaseActivity() , NavigationView.OnNavigationItemSelectedListener {

    companion object{
        const val MY_PROFILE_REQUEST_CODE: Int = 11
        const val CREATE_BOARD_REQUEST_CODE: Int = 12
    }

    private lateinit var  mUserName: String
  //<=============For Notification==========================>
    private lateinit var mSharedPreferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //<------To change the status bar color------>
        if (Build.VERSION.SDK_INT >= 21) {
            val window = this.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = this.resources.getColor(R.color.sky_blue)
        }

        setupActionBar()

        nav_view.setNavigationItemSelectedListener(this)
//<======================Getting value in SharedPreference in givenKey which store the data in App storage==============>
        mSharedPreferences = this.getSharedPreferences(
                Constants.PROJEMANAG_PREFERENCES,
                Context.MODE_PRIVATE)
//<====================Checks whether the Token is Updated is FireStore or Not=================>
        val tokenUpdated = mSharedPreferences.getBoolean(Constants.FCM_TOKEN_UPDATED,false)
        if (tokenUpdated){ //If we haven't update the toke just laod the UserData
            showProgressDialog("Please wait..")
            FireStoreClass().loadUserData(this,true)
        }else{ // Get the token For the Current User
             FirebaseInstallations.getInstance().getToken(true).addOnSuccessListener(this@MainActivity){ installationTokenResult->
                 updateFCMToken(installationTokenResult.token)
             }

            }



        FireStoreClass().loadUserData(this, true)

        fab_create_board.setOnClickListener {
            val intent = Intent(this,CreateBoard::class.java)
            intent.putExtra(Constants.NAME,mUserName)
            startActivityForResult(intent, CREATE_BOARD_REQUEST_CODE)    // For OnBoard

        }



    }//OnCreate

    private fun setupActionBar() {
        setSupportActionBar(toolbar_main_activity)
        toolbar_main_activity.setNavigationIcon(R.drawable.ic_action_navigation_menu)

        toolbar_main_activity.setNavigationOnClickListener {
            //Toggle drawer
            toggleDrawer()
        }
    }

    private fun toggleDrawer() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            drawer_layout.openDrawer(GravityCompat.START)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            doubleBackToExit()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK && requestCode == MY_PROFILE_REQUEST_CODE){
            FireStoreClass().loadUserData(this)
        }else if(resultCode == Activity.RESULT_OK                   // to display board collection data from fireStore class
                && requestCode == CREATE_BOARD_REQUEST_CODE) {
            FireStoreClass().getBoardsList(this)
        }else{
            Log.e("Cancelled" , "Cancelled" )
        }
    }
    override fun onNavigationItemSelected(item: MenuItem): Boolean {       // functionality to the item of navigation bar
        when(item.itemId){
            R.id.nav_my_profile ->{
                startActivityForResult(Intent(this,MyProfileActivity::class.java), MY_PROFILE_REQUEST_CODE)
            }
            R.id.nav_sign_out ->{
                FirebaseAuth.getInstance().signOut()
                //<--------When we SignOut our SharedPreference should be Reset
            //    mSharedPreferences.edit().clear().apply()

                val intent = Intent(this, IntroActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    fun populateBoardsListToUI(boardsList: ArrayList<Board>){  //To add add the functionality to board Activity & data to the board
        hideProgressDialog()

        if(boardsList.size > 0){
            rv_boards_list.visibility = View.VISIBLE
            tv_no_boards_available.visibility = View.GONE

            rv_boards_list.layoutManager = LinearLayoutManager(this)   // RecyclerView for CardActivity
            rv_boards_list.setHasFixedSize(true)                          //Such that our board should have fixed size

            val adapter = BoardItemAdapter(this,boardsList)
            rv_boards_list.adapter = adapter

            adapter.setOnClickListener(object : BoardItemAdapter.OnClickListener{        //Setting onClicklistner to our adapter
                override fun onClick(position: Int, model: Board) {
                    val intent = Intent(this@MainActivity,TaskListActivity::class.java)
                    intent.putExtra(Constants.DOCUMENT_ID , model.documentId)
                    startActivity(intent)
                }
            })
        }else{
            rv_boards_list.visibility = View.GONE
            tv_no_boards_available.visibility = View.VISIBLE
        }
    }

    fun updateNavigationUserDetails(user: User, readBoardsList: Boolean) {
        hideProgressDialog()
        mUserName = user.name

        Glide.with(this)
                .load(user.image)
                .centerCrop()
                .placeholder(R.drawable.ic_user_place_holder)
                .into(nav_user_image);

        tv_username.text = user.name

        if(readBoardsList){
            showProgressDialog("Please wait")
            FireStoreClass().getBoardsList(this)
        }

    }
//<===========update the the Token in SharedPrefernce =================>
    fun tokenUpdateSuccess(){
        hideProgressDialog()
        val editor: SharedPreferences.Editor = mSharedPreferences.edit()
        editor.putBoolean(Constants.FCM_TOKEN_UPDATED, true)
        editor.apply() //Apply this Changes to our sharedPreference
        showProgressDialog("Please wait..")
        FireStoreClass().loadUserData(this,true)
    }
    //<==================Store the token In firestore==============>

    private fun updateFCMToken(token: String){
       val userHashMap = HashMap<String , Any>()                      //Token of the User when the user logged In and using that Token it the User a Notification
        userHashMap[Constants.FCM_TOKEN] = token
        showProgressDialog("Please wait..")
        FireStoreClass().updateUserProfileData(this,userHashMap)
    }
}


