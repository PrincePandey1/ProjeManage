package com.example.android.projemanage.activities

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import models.Board
import models.User

class FireStoreClass {

    private val mFireStore = FirebaseFirestore.getInstance()

    //<-----Registering User To the The Firestore--->

    fun  registerUser(activity: SignUp , userInfo: User){
        mFireStore.collection(Constants.USERS)
                .document(getCurrentUserId())
                .set(userInfo , SetOptions.merge())
                .addOnSuccessListener {
                    activity.userRegisteredSuccess()
                }.addOnFailureListener {
                    e->
                    Log.e(activity.javaClass.simpleName,"Something went Wrong")
                }
    }

    //<------Creating collection of board to the fireStore--->
    fun createBoard(activity: CreateBoard , board: Board){
        mFireStore.collection(Constants.BOARDS)
                .document()
                .set(board, SetOptions.merge())
                .addOnSuccessListener {
                    Log.e(activity.javaClass.simpleName,"Board Created Successfully")
                    Toast.makeText(activity,"Board created successfully",Toast.LENGTH_LONG).show()
                    activity.boardCreatedSuccessfully()
                }.addOnFailureListener {
                    exception ->
                    activity.hideProgressDialog()
                    Log.e(activity.javaClass.simpleName,
                            "Error while creating a board.",
                            exception
                    )
                }

    }

 //   <---------Getting User deatils for TaskListActivity------>

    fun getBoardDetails(activity: TaskListActivity, documentId: String){                      //Getting Board data For TaskActivity from FireStore
        mFireStore.collection(Constants.BOARDS)                                // we get the data using userId i.e(DocumentID)
                .document(documentId)
                .get()
                .addOnSuccessListener {
                    document ->
                    Log.i(activity.javaClass.simpleName , document.toString())
                    val board = document.toObject(Board::class.java)!!
                    board.documentId = document.id
                    activity.boardDetails(board)

                }.addOnFailureListener { e ->
                    activity.hideProgressDialog()
                    Log.e(activity.javaClass.simpleName , "Error while creating a board")
                }
    }

  //  <---Updating taskList to the FireStore---->

    fun addUpdateTaskList(activity: Activity, board: Board){                    // Updating data to the task list
        val taskListHashMap = HashMap<String , Any>()
        taskListHashMap[Constants.TASK_LIST] = board.taskList                  //  In constants.TAsk_List position we pass the value string

        mFireStore.collection(Constants.BOARDS)                           //Updating the data(taskListHashMap) in board collection in FireStore
                .document(board.documentId)
                .update(taskListHashMap)
                .addOnSuccessListener {
                    Log.e(activity.javaClass.simpleName, "TaskList updated successfully.")
                    if(activity is TaskListActivity) {
                        activity.addUpdateTaskListSuccess()
                    }else if(activity is CardDetailsActivity) {
                        activity.addUpdateTaskListSuccess()
                    }// Calling addUpdateTaskListSuccess() for CardListActivity
                }.addOnFailureListener {
                    exception->
                    if (activity is TaskListActivity)
                    activity.hideProgressDialog()
                    else if (activity is CardDetailsActivity)
                        activity.hideProgressDialog()
                    Log.e(activity.javaClass.simpleName, "Error while creating the board.",exception)
                }

    }


//<-----Updating data from MyProfileActivity To the User Collection---->

    fun updateUserProfileData(activity: Activity,userHashMap:  HashMap<String, Any>) {
        mFireStore.collection(Constants.USERS)
                .document(getCurrentUserId())
                .update(userHashMap)
                .addOnSuccessListener {
                    Log.e(activity.javaClass.simpleName,"Profile Data updated")
                    Toast.makeText(activity,"Profile updated successfully",Toast.LENGTH_LONG).show()
                    when(activity){
                        is MainActivity ->{
                            activity.tokenUpdateSuccess()
                        }
                        is MyProfileActivity ->{
                            activity.profileUpdateSuccess()
                        }
                    }

                }.addOnFailureListener {
                    e->
                    when(activity){
                        is MainActivity ->{
                            activity.hideProgressDialog()
                        }
                        is MyProfileActivity ->{
                            activity.hideProgressDialog()
                        }
                    }


                    Log.e(activity.javaClass.simpleName,"Error while creating a board.")
                    Toast.makeText(activity,"Something went wrong!",Toast.LENGTH_LONG).show()

                }
    }

// <-------Loading User Data  from FireStore from UserCollection ----->

    fun loadUserData(activity: Activity, readBoardsList: Boolean = false){

        mFireStore.collection(Constants.USERS)
                .document(getCurrentUserId())
                .get()
                .addOnSuccessListener { document ->
                    val loggedInUser = document.toObject(User::class.java)

                    when(activity){
                        is SignIn ->{
                            activity.signInSuccess(loggedInUser)
                        }
                        is MainActivity ->{
                            if (loggedInUser != null) {
                                activity.updateNavigationUserDetails(loggedInUser, readBoardsList)
                            }
                        }
                        is MyProfileActivity ->{
                            if (loggedInUser != null) {
                                activity.setUserDataInUI(loggedInUser)
                            }
                        }
                    }

                }.addOnFailureListener {
                    e->
                    when(activity){
                        is SignIn ->{
                            activity.hideProgressDialog()
                        }
                        is MainActivity ->{
                            activity.hideProgressDialog()
                        }

                    }

                    Log.e("SignInSuccess","Something went Wrong")
                }
    }

  //  <--- Getting User Object from FireStorage Using CurrentUserID------>

    fun getBoardsList(activity: MainActivity){  // Passing the data from fireStore collection(board) and assigned to the board class
        //For BoardActivity
        mFireStore.collection(Constants.BOARDS)
                .whereArrayContains(Constants.ASSIGNED_TO , getCurrentUserId())//Query where we check does this firestore contains assigned value or not
                .get()
                .addOnSuccessListener {
                    document ->
                    Log.i(activity.javaClass.simpleName , document.documents.toString())
                    val boardList: ArrayList<Board> = ArrayList()
                    for (i in document.documents){
                        val board = i.toObject(Board::class.java)!! //whatever object Board class contain we get tha t object and stored it in board variable
                        board.documentId = i.id
                        boardList.add(board)
                    }
                    activity.populateBoardsListToUI(boardList)

                }.addOnFailureListener { e ->
                    activity.hideProgressDialog()
                    Log.e(activity.javaClass.simpleName , "Error while creating a board")
                }
    }

  //  <----- Getting Current UserId from the FireStore----->

    fun getCurrentUserId(): String {
        //  return FirebaseAuth.getInstance().currentUser!!.uid
        // In Alternative we check whether the Current User is empty or not
        val currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserID = " "
        if(currentUser != null){
            currentUserID = currentUser.uid
        }
        return currentUserID
    }
//<-----Getting data from FireStore to assign in MembersActivity--->
    fun getAssignedMembersListDetails(activity: Activity, assignedTo: ArrayList<String>){
       mFireStore.collection(Constants.USERS)
               .whereIn(Constants.ID,assignedTo) //  Using Query,here we match each id with  the id which is passes to the key "Constants.id" , and if any id matches then it return s the documents of the that user id in  string
               .get()
               .addOnSuccessListener {
                   document ->
                   Log.e(activity.javaClass.simpleName,document.documents.toString())

                   val userList: ArrayList<User> = ArrayList()

                   for(i in document.documents){ //traversing through each documents and stores to the the i
                       val user = i.toObject(User::class.java) //Converting documents to Object of type USer class
                       if (user != null) {
                           userList.add(user)
                       }
                       if (activity is MembersActivity)
                       activity.setupMembersList(userList) //Function Present oin MemberActivity
                       else if (activity is TaskListActivity)
                           activity.boardMembersDetailsList(userList)
                   }
               }.addOnFailureListener {
                   if(activity is MembersActivity)
                   activity.hideProgressDialog()
                   else if(activity is TaskListActivity)
                       activity.hideProgressDialog()
                   Log.e(
                           activity.javaClass.simpleName,
                   "Error while creating board.")
               }
    }

  //  <-------Getting members Details-------->
    fun getMemberDetails(activity: MembersActivity, email: String){
        mFireStore.collection(Constants.USERS)
                .whereEqualTo(Constants.EMAIL,email) // It checks if the email which User enters matches to any Users Email then it return the data of that User
                .get()
                .addOnSuccessListener {
                    document ->
                    if(document.documents.size > 0){
                        val user = document.documents[0].toObject(User::class.java) //Getting first data from document i.e on 0 position and making the object of it Of type User
                        if (user != null) {
                            activity.memberDetails(user)
                        }
                    }else{
                        activity.hideProgressDialog()
                        activity.showErrorSnackBar("No such member found")
                    }
                }.addOnFailureListener {
                    activity.hideProgressDialog()
                    Log.e(
                            activity.javaClass.simpleName,
                            "Error while creating user details")
                }
    }
    //<-----------------Updating the memberUser to the FireStore------------>

    fun assignedMemberToBoard(activity: MembersActivity,board: Board, user: User){

        val assignedToHashMap = HashMap<String , Any>()
        assignedToHashMap[Constants.ASSIGNED_TO] = board.assignedTo

        mFireStore.collection(Constants.BOARDS)
                .document(board.documentId)
                .update(assignedToHashMap)
                .addOnSuccessListener {
                    activity.memberAssignedSuccess(user) //Passing User collection Object with updated data
                }.addOnFailureListener {
                    activity.hideProgressDialog()
                    Log.e(activity.javaClass.simpleName,"Error while creating Board.")
                }

    }
}