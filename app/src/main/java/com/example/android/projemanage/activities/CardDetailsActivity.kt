package com.example.android.projemanage.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatEditText
import androidx.recyclerview.widget.GridLayoutManager
import com.example.android.projemanage.R
import com.example.android.projemanage.adapter.CardMemberListItemsAdapter
import com.example.android.projemanage.dialog.LabelColorListDialog
import com.example.android.projemanage.dialog.MemberListDialog
import kotlinx.android.synthetic.main.activity_card_details.*
import kotlinx.android.synthetic.main.activity_card_details.view.*
import kotlinx.android.synthetic.main.activity_memebers.*
import models.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CardDetailsActivity : BaseActivity() {

    private lateinit var  mBoarDetails: Board
    private var mTaskListPosition = -1
    private var mCardPosition = -1
    private var mSelectedColor: String = " "
    private lateinit var mMembersDetailList: ArrayList<User>

    //To store Date
    private var mSelectedDueDateMilliSeconds: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_details)

        getIntentData()
      setupActionBar()

        //<-------Initializing the UI activity card Details---------->
        et_name_card_details.setText(mBoarDetails
            .taskList[mTaskListPosition]
            .cards[mCardPosition].name)

//<---Getting the color which users select------------>

        mSelectedColor = mBoarDetails.taskList[mTaskListPosition].cards[mCardPosition].labelColor
        if (mSelectedColor.isNotEmpty()){
            setColor()
        }

       btn_update_card_details.setOnClickListener {

           if (et_name_card_details.text.toString().isNotEmpty())
           updateCardDetails()
           else {
               Toast.makeText(this,"Enter a card name.",Toast.LENGTH_SHORT).show()
           }
       }
        tv_select_label_color.setOnClickListener {
            labelColorsListDialog()
        }

        tv_select_members.setOnClickListener {
            membersListDialog()
        }

        setupSelectedMembersList()

        //<-----It shows the date to the card if already exist-------------->
        mSelectedDueDateMilliSeconds = mBoarDetails.taskList[mTaskListPosition].cards[mCardPosition].dueDate

        if (mSelectedDueDateMilliSeconds > 0){ // Greater than zero means it contains date
            val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
            val selectedDate = simpleDateFormat.format(Date(mSelectedDueDateMilliSeconds)) //Passed the date which format the date of Type into Simple date format and pass to the date function which returns the date
           tv_select_due_date.text = selectedDate
        }

        tv_select_due_date.setOnClickListener {
            showDatePicker() //Show Date PickerDialog
        }

    }//OnCreate

    fun addUpdateTaskListSuccess(){
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun setupActionBar(){
        setSupportActionBar(toolbar_card_details_activity)

        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_whiteback)
            actionBar.title = mBoarDetails.taskList[mTaskListPosition].cards[mCardPosition].name
        }
        toolbar_card_details_activity.setNavigationOnClickListener {
            onBackPressed()
        }

    }
    //<--------Getting details through Intent from TaskListActivity------>
    private fun getIntentData(){
        if(intent.hasExtra(Constants.BOARD_DETAIL)){
            mBoarDetails = intent.getParcelableExtra(Constants.BOARD_DETAIL)!!
        }
        if(intent.hasExtra(Constants.TASK_LIST_ITEM_POSITION)){
            mTaskListPosition = intent.getIntExtra(Constants.TASK_LIST_ITEM_POSITION,-1)
        }
        if(intent.hasExtra(Constants.CARD_LIST_ITEM_POSITION)){
            mCardPosition = intent.getIntExtra(Constants.CARD_LIST_ITEM_POSITION,-1)
        }
        if(intent.hasExtra(Constants.BOARD_MEMBERS_LIST)){
            mMembersDetailList = intent.getParcelableArrayListExtra(Constants.BOARD_MEMBERS_LIST)!!
        }
    }
//<--------------Added color to the ArrayList------------>
    private fun colorsList(): ArrayList<String>{
          val colorList: ArrayList<String> = ArrayList()
            colorList.add("#43C86F")
            colorList.add("#0C90F1")
            colorList.add("#F72400")
            colorList.add("#D57C1D")
            colorList.add("#770000")
            colorList.add("#0022F8")
            return colorList
    }
//<----------Setting color and text-------------->
    private fun setColor(){
        tv_select_label_color.text = " "
        tv_select_label_color.setBackgroundColor(Color.parseColor(mSelectedColor))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_delete_card,menu) //Adding menu UI
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_delete_card -> {
           alertDialogForDeleteList(mBoarDetails.taskList[mTaskListPosition].cards[mCardPosition].name)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

  //  <-----------Updating the Card Using CardObject--------------->
    private fun  updateCardDetails(){
        val card = Card(
                et_name_card_details.text.toString(),
               mBoarDetails.taskList[mTaskListPosition].cards[mCardPosition].createdBy,
                mBoarDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo,
                mSelectedColor,mSelectedDueDateMilliSeconds
        )

      val taskList: ArrayList<Task> = mBoarDetails.taskList
      taskList.removeAt(taskList.size-1)
       //<-----Passed Updated card ----------------->
      mBoarDetails.taskList[mTaskListPosition].cards[mCardPosition] = card

      showProgressDialog("Please wait..")
      //mBoardDetails contains the updated card
     FireStoreClass().addUpdateTaskList(this,mBoarDetails)


    }
    //<------------For deleting the Card------------->
  private fun deleteCard(){

        // Adding all the cards for a task which user chooses
      val cardsList: ArrayList<Card> = mBoarDetails.taskList[mTaskListPosition].cards

      cardsList.removeAt(mCardPosition)

        //Updating tasklist  with deleted card
        val taskList: ArrayList<Task> = mBoarDetails.taskList
        //This removes ADD CARD text
        taskList.removeAt(taskList.size-1)

        //<---Passed updated cards to CardsArrayList------------->
        taskList[mTaskListPosition].cards = cardsList

         showProgressDialog("Please wait..")
        FireStoreClass().addUpdateTaskList(this,mBoarDetails)
  }

    private fun alertDialogForDeleteList(cardName: String){  // Created Alter Dialog

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Alert")

        builder.setMessage("Are you sure you want to delete $cardName.")
        builder.setIcon(R.drawable.ic_warning)

        builder.setPositiveButton("Yes"){ dialogInterface, which->
            dialogInterface.dismiss()
            deleteCard()

        }
        builder.setNegativeButton("No"){dialogInterface, which->
            dialogInterface.dismiss()
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()

    }
    //<-------Passing values to abstract class and displaying dialog--------->
          private fun labelColorsListDialog(){
            val colorsList: ArrayList<String> = colorsList()

              val listDialog = object : LabelColorListDialog(
                      this,
                      colorsList,
                      "Select label color",
                      mSelectedColor){
                  override fun onItemSelected(color: String) {
                      mSelectedColor = color
                      setColor()
                  }

              }
              listDialog.show()
          }
    //<-----Adding functionality to MemberListDialog----------->
      private fun membersListDialog(){
          var cardAssignedMembersList = mBoarDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo

          if(cardAssignedMembersList.size>0){ //Check do we have any member in the list
             for(i in  mMembersDetailList.indices) {  //If have member in the list Go through all the member in the list
                 for(j in cardAssignedMembersList){  //Checks every member id to the whom the card is assigned
                     if(mMembersDetailList[i].id == j){
                         mMembersDetailList[i].selected = true// If member assigned to the card == to the member assigned to the board
                     }                                       // Then makes that item selectable selectable
                 }
             }
          }else{
              for(i in  mMembersDetailList.indices) {
                  mMembersDetailList[i].selected = false
              }
          }
        //<------To display the dialog---------->
        val listDialog = object : MemberListDialog(this,mMembersDetailList,"Select Members"){
            override fun onItemSelected(user: User, action: String) {
                if (action == Constants.SELECT){//To add tick feature
                    //Check whether user exists or not,If user not exists then we will add that user
                  if(!mBoarDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo.contains(user.id)){
                      mBoarDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo.add(user.id)
                  }
                }else{
                    mBoarDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo.remove(user.id)

                    for(i in mMembersDetailList.indices){
                        if(mMembersDetailList[i].id == user.id){
                            mMembersDetailList[i].selected = false
                        }
                    }
                }
                setupSelectedMembersList()
            }

        }
        listDialog.show()

      }

  //  <-----Adding Selected member details and passing to the CardMemberListItemAdapter---->
    private fun setupSelectedMembersList(){
        val cardAssignedMembersList = mBoarDetails
                .taskList[mTaskListPosition]
                .cards[mCardPosition].assignedTo

      val selectedMembersList: ArrayList<SelectedMembers> = ArrayList()

      for(i in  mMembersDetailList.indices) {  //If have member in the list Go through all the member in the list
          for(j in cardAssignedMembersList){  //Checks every member id to the whom the card is assigned
              if(mMembersDetailList[i].id == j){
                 val selectedMember = SelectedMembers(
                         mMembersDetailList[i].id,
                         mMembersDetailList[i].image
                 )
                  selectedMembersList.add(selectedMember)
              }
          }
      }
      if(selectedMembersList.size > 0){
          selectedMembersList.add(SelectedMembers("",""))
          tv_select_members.visibility = View.GONE

          rv_selected_members_list.visibility = View.VISIBLE

          rv_selected_members_list.layoutManager = GridLayoutManager(this,6)

          val adapter = CardMemberListItemsAdapter(this,selectedMembersList,true)

          rv_selected_members_list.adapter = adapter

          adapter.setOnClickListener(object : CardMemberListItemsAdapter.OnClickListener{
              override fun onClick() {
                 membersListDialog()
              }

          })

      }else{
          tv_select_members.visibility = View.VISIBLE
          rv_selected_members_list.visibility = View.GONE
      }

    }
    //<----------------function for datePicker-------------->
    

     private fun showDatePicker(){
         val c = Calendar.getInstance() //Instance returns all the object of the class
        val year = 
                c.get(Calendar.YEAR) //Returns the value of year from the given calendar
        val month = c.get(Calendar.MONTH)//This indicates the month
        val day = c.get(Calendar.DAY_OF_MONTH)//This indices the day
       
        val  dpd = DatePickerDialog(
                this,
                DatePickerDialog.OnDateSetListener{view, year, monthOfYear , dayOfMonth  ->
            val sDayOfMonth = if (dayOfMonth<10) "0$dayOfMonth" else "$dayOfMonth"
            val sMonthOfYear = if((monthOfYear + 1)<10) "0${monthOfYear + 1}"  else "${monthOfYear}"

                    val selectedDate = "$sDayOfMonth/$sMonthOfYear/$year"
                    tv_select_due_date.text = selectedDate

                    val sdf = SimpleDateFormat("dd/MM/yyyy",Locale.ENGLISH)
                    val theDate = sdf.parse(selectedDate) // Parse is basically a convert here it convert a string into date
                    mSelectedDueDateMilliSeconds = theDate!!.time //Parse into time

        }, year, month, day //Passing year month and ady
        )
         dpd.show() //To show the datePickerDialog
     }

   

    

}//Main



