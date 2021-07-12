package com.example.android.projemanage.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.projemanage.R
import com.example.android.projemanage.adapter.MemberListItemAdapter
import kotlinx.android.synthetic.main.activity_memebers.*
import kotlinx.android.synthetic.main.dialog_search_member.*
import models.Board
import models.User
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.lang.Exception
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

class MembersActivity : BaseActivity() {

    private lateinit var mBoardsDetails: Board
    private lateinit var mAssignedMembersList: ArrayList<User>
    private var anyChangeMade: Boolean = false //If any changes made in the members activity then boolean value changes to true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memebers)

// <----------The data came from TaskListListActivity by Intent Through function MenuItem----->
        if(intent.hasExtra(Constants.BOARD_DETAIL)) {
            mBoardsDetails = intent.getParcelableExtra(Constants.BOARD_DETAIL)!! //Error
        }
        setupActionBar()
        showProgressDialog("Please wait..")
        //<----Getting From FireStore----->
        FireStoreClass().getAssignedMembersListDetails(this,mBoardsDetails.assignedTo)


    }//On Create


    fun setupMembersList(list: ArrayList<User>){

        mAssignedMembersList = list

        hideProgressDialog()

        rv_members_list.layoutManager = LinearLayoutManager(this)
        rv_members_list.setHasFixedSize(true)

        val adapter = MemberListItemAdapter(this,list)

        rv_members_list.adapter = adapter
    }

  //<------Getting member Details------------------->
  fun memberDetails(user: User)  {
          mBoardsDetails.assignedTo.add(user.id)// The task is assigned to more user of give id
          FireStoreClass().assignedMemberToBoard(this,mBoardsDetails,user)
  }
// <-----------Setting up ActionBar----------------->
    private fun setupActionBar(){
        setSupportActionBar(toolbar_members_activity)

        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_whiteback)
            actionBar.title = "Members"
        }
        toolbar_members_activity.setNavigationOnClickListener {
            onBackPressed()
        }

    }
    //<-------Adding menu members------->
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_member,menu) //Adding menu UI
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_add_member -> {
                dialogSearchMember()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

 //   <---------Adding functionality to the button of Dialog---------->

    private fun dialogSearchMember(){
        var dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_search_member)
        dialog.tv_add.setOnClickListener {
                val email = dialog.et_email_search_member.text.toString()
            if(email.isNotEmpty()){
                dialog.dismiss()
                showProgressDialog("Please wait...")
                FireStoreClass().getMemberDetails(this,email)
            }else{
                Toast.makeText(this,"Please enter members email address.",Toast.LENGTH_SHORT).show()
            }
        }
        dialog.tv_cancel.setOnClickListener{
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun onBackPressed() {
        if (anyChangeMade){
            setResult(Activity.RESULT_OK)
        }
        super.onBackPressed()

    }
    //<-------Update the List with the current assigned user---->
     fun memberAssignedSuccess(user: User){
         hideProgressDialog()
        mAssignedMembersList.add(user) // Added the User object of assigned member
        anyChangeMade = true // As the member is added it means changes has made & value changes to true and which reload the tasklistActivity
        setupMembersList(mAssignedMembersList)
//<==================Passed value to the SendNotification function============>
        SendNotificationToUserTask(mBoardsDetails.name,user.fcmToken)
     }

    //<===============Adding the Member to thom the Notification would be Send=================>
    private inner class SendNotificationToUserTask(val boardName: String, val token: String)
        : AsyncTask<Any, Void, String>(){

        override fun onPreExecute() {
            super.onPreExecute()
            showProgressDialog("Please wait..")
        }
        override fun doInBackground(vararg params: Any?): String {
                 var result: String

                 var connection: HttpURLConnection? = null
            try{
                val url = URL(Constants.FCM_BASE_URL)
                //<====To open the url we use the connection
                connection = url.openConnection() as HttpURLConnection
                connection.doOutput = true
                connection.doInput = true
                connection.instanceFollowRedirects = false
                connection.requestMethod = "POST"

                //<======Setting property of Connection===========>
                connection.setRequestProperty("Content-type","application/json")
                connection.setRequestProperty("charset","utf-8")
                connection.setRequestProperty("Accept","application/json")

                //<===== A setRequestProperty always contains Key and value pair=====>

                connection.setRequestProperty(
                        Constants.FCM_AUTHORIZATION,"${Constants.FCM_KEY} = ${Constants.FCM_SERVER_KEY}")

                connection.useCaches = false

                //<=====As we sending the Data therefore we use outputStream========>
                val wr = DataOutputStream(connection.outputStream)
                val jsonRequest = JSONObject()
                val dataObject = JSONObject()
         //<====Adding data to dataObject Using putFunction================>
                dataObject.put(Constants.FCM_KEY_TITLE,"Assigned to the board $boardName") // Adding title to the Notification
                dataObject.put(Constants.FCM_KEY_MESSAGE,"You have been assigned to the Board"+"by ${mAssignedMembersList[0].name}")

                //<====Adding dataObject to the Json Request
                jsonRequest.put(Constants.FCM_KEY_DATA,dataObject)
                //=====Passing the token
                jsonRequest.put(Constants.FCM_KEY_TO,token)

                wr.writeBytes(jsonRequest.toString())
                wr.flush()
                wr.close()

                //<===If the RequestFails to the Server then whatever the response is given by the Server is Stored in var hhtp====>
                //If response is 200 then request is accepted
                // If response is 400 then there is Error
             val  httpResult: Int = connection.responseCode
                if (httpResult == HttpURLConnection.HTTP_OK){
                    val inputStream = connection.inputStream

                    val reader = BufferedReader(
                            InputStreamReader(inputStream))

                    val sb = StringBuilder()
                    var line: String?
                   try {
                       while (reader.readLine().also { line=it }!= null){
                           sb.append(line+"\n")
                       }
                   }catch(e: IOException){
                       e.printStackTrace()
                   }finally {
                       try {
                           inputStream.close()
                       }catch (e: IOException){
                           e.printStackTrace()
                       }
                   }
                    result = sb.toString()
                }else{
                    result = connection.responseMessage
                }

            }catch (e: SocketTimeoutException){
                result = "Connection Timeout"
            }catch (e: Exception){
                result = "Error : " + e.message
            }finally {
                connection?.disconnect()
            }
            return result
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            hideProgressDialog()
            if (result != null) {
                Log.e("JSON Response Result" , result)
            }
        }

    }
}