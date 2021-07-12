package com.example.android.projemanage.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.projemanage.adapter.TaskListItemAdapter
import com.example.android.projemanage.R
import kotlinx.android.synthetic.main.activity_task_list.*
import models.Board
import models.Card
import models.Task
import models.User

class TaskListActivity : BaseActivity() {

    private lateinit var mBoardDetails: Board
    private lateinit var mBoardDocumentID: String
    lateinit var mAssignedMemberDetailList: ArrayList<User>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_list)

        window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN, // Used to hide status bar
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
        )


        if(intent.hasExtra(Constants.DOCUMENT_ID)){ // Check whether Constant var contains data or not
          mBoardDocumentID = intent.getStringExtra(Constants.DOCUMENT_ID).toString()  // If contains data then Using "getStringExtra" we get that data and stores in variable
        }
        showProgressDialog("Please wait")
        FireStoreClass().getBoardDetails(this, mBoardDocumentID)  //We get details from fireStore w.r.t the userid

    }
//<--------Here we check if any changes made in member activity, If changes are made Reload the TaskListActivity---->

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == MEMBERS_REQUEST_CODE || requestCode == CARD_DETAILS_REQUEST_CODE){
           showProgressDialog("Please wait..")
            FireStoreClass().getBoardDetails(this, mBoardDocumentID)
        }else{
            Log.e("Cancelled" , "Cancelled")
        }
    }
//<------Adding menu to TAskListActivity----------->
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_members , menu)
        return super.onCreateOptionsMenu(menu)
    }
    //<-------Adding clickable function to the menu item------->
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_members ->{
             val intent =  Intent(this,MembersActivity::class.java)
                intent.putExtra(Constants.BOARD_DETAIL, mBoardDetails)
                startActivityForResult(intent, MEMBERS_REQUEST_CODE)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

  //  <-------Passing data to the taskListItemAdapter and adapter to the recycler view----->

    fun boardDetails(board: Board){
        mBoardDetails = board

        hideProgressDialog()
        setupActionBar()



        showProgressDialog("Please wait..")
        FireStoreClass().getAssignedMembersListDetails(this,
                mBoardDetails.assignedTo)
    }

    private fun setupActionBar(){
        setSupportActionBar(toolbar_task_list_activity)

        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_whiteback)
            actionBar.title = mBoardDetails.name
        }
        toolbar_task_list_activity.setNavigationOnClickListener {
            onBackPressed()
        }

    }
    fun addUpdateTaskListSuccess(){ // Getting data from the fireStore from Board Collection
        hideProgressDialog()  //It will get closed when taskList updated successfully

        showProgressDialog("Please wait.") // It will Show after the hideprogress dialog is closed and,
        // showProgressDialog will start while data is loading from fireStore
        FireStoreClass().getBoardDetails(this, mBoardDetails.documentId)

    }

    fun createTaskList(taskListName: String){  // Adding the functionality to UI of the TaskListActivity
        val task = Task(taskListName,FireStoreClass().getCurrentUserId())
        mBoardDetails.taskList.add(0,task)  //Updated the board with the task which User assigned in Task List Activity
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1)

        showProgressDialog("Please wait")
        FireStoreClass().addUpdateTaskList(this,mBoardDetails)
    }
    //<----------Updating the task To the Firestore with the given Parameter--------->

    fun updateTaskList(position: Int, listName: String , model: Task){  //Updating taskList
        val task = Task(listName, model.createdBy)

        mBoardDetails.taskList[position] = task
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1)

        showProgressDialog("Please wait")

        FireStoreClass().addUpdateTaskList(this,mBoardDetails)
    }

    fun deleteTaskList(position:Int ){ //For deleting the task and updating the board in fireStore
        mBoardDetails.taskList.removeAt(position)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1 )

        showProgressDialog("Please wait")

        FireStoreClass().addUpdateTaskList(this,mBoardDetails)
    }

    //<---------Creating card and updating data in the firestore of type Card Object---->
    fun addCardToTaskList(position: Int, cardName: String){
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1)

        var cardAssignedUserList: ArrayList<String> = ArrayList() //Creating ArrayList for Assigned User

        cardAssignedUserList.add(FireStoreClass().getCurrentUserId())   //Gets Assigned Value

        val card = Card(cardName , FireStoreClass().getCurrentUserId(),cardAssignedUserList)

        val cardList = mBoardDetails.taskList[position].cards //We gets the old card from taskList which is assigned to the user of given position
        cardList.add(card) //We add the new card to cardList or Updating the TaskLIst with new Cards

        val task = Task(  //And Created New Task Using New Card
                mBoardDetails.taskList[position].title,
                mBoardDetails.taskList[position].createdBy,
                cardList
        )

        mBoardDetails.taskList[position] = task //Updating the taskList With Cureent Card
        showProgressDialog("Please wait")

        FireStoreClass().addUpdateTaskList(this,mBoardDetails)

    }

    fun boardMembersDetailsList(list: ArrayList<User>){
        mAssignedMemberDetailList = list
        hideProgressDialog()

        val addTaskList = Task(resources.getString(R.string.add_list))
        mBoardDetails.taskList.add(addTaskList)

        rv_task_list.layoutManager = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
        rv_task_list.setHasFixedSize(true)

        val adapter  = TaskListItemAdapter(this,mBoardDetails.taskList) //Passing to the data the adapter
        rv_task_list.adapter = adapter // passing to the recyclerView
    }
//<==============This function takes care of item that move in card Using drag and drop=======>
    fun updateCardsInTaskList(taskListPosition: Int, cards: ArrayList<Card>){
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

    mBoardDetails.taskList[taskListPosition].cards = cards //This swap the cards from the draggedFrom position card
    showProgressDialog("Please wait..")
    FireStoreClass().addUpdateTaskList(this,mBoardDetails)
    }

    companion object{
        const val MEMBERS_REQUEST_CODE: Int = 13
        const val CARD_DETAILS_REQUEST_CODE: Int = 14
    }
   //<------Moving to the CardDetailsActivity------------->
    fun cardDetails(taskListPosition: Int , cardPosition: Int){
       val intent =  Intent(this,CardDetailsActivity::class.java)
       intent.putExtra(Constants.BOARD_DETAIL,mBoardDetails)
       intent.putExtra(Constants.TASK_LIST_ITEM_POSITION,taskListPosition)
       intent.putExtra(Constants.CARD_LIST_ITEM_POSITION,cardPosition)
       intent.putExtra(Constants.BOARD_MEMBERS_LIST,mAssignedMemberDetailList)
        startActivityForResult(intent, CARD_DETAILS_REQUEST_CODE)
    }
}