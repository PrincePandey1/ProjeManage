package com.example.android.projemanage.Bcm


import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.android.projemanage.R
import com.example.android.projemanage.activities.Constants
import com.example.android.projemanage.activities.FireStoreClass
import com.example.android.projemanage.activities.MainActivity
import com.example.android.projemanage.activities.SignIn
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService: FirebaseMessagingService() {

    companion object{
        private const val TAG = "MyFirebaseMsgService"
    }

 //  <==============Implementing firebase messaging Service class======>

    //<====For notification we use this service for receiving the message==========>
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d(TAG,"FROM: ${remoteMessage.from}") //From Where the message come from

        remoteMessage.data.isNotEmpty().let {       //Checks if message data is not empty
            Log.d(TAG,"Message data Payload: ${remoteMessage.data}") //Display the message data

            //<========Receiving the remote Message==========>
            val title = remoteMessage.data[Constants.FCM_KEY_TITLE]!!
            val message = remoteMessage.data[Constants.FCM_KEY_MESSAGE]!!

            //<======Passing Notification to the User====>
            sendNotification(title,message) //Passing parameter to the sendNotification Function
        }
        remoteMessage.notification?.let{
            Log.d(TAG,"Message Notification Body: ${it.body}") //Gives the Notification of the message
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.e(TAG,"Refreshed token: $token")

        sendRegistrationToServer(token)
    }
    private fun sendNotification(title: String, message: String){
        //Checks if the user is registered then only it sends to the MAinActivity
        val intent = if (FireStoreClass().getCurrentUserId().isNotEmpty()){
            Intent(this,MainActivity::class.java)
        }else{
            //<====If user is not registered send it to the SignInActivity================>
            Intent(this,SignIn::class.java)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT)
        val channelId = this.resources.getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        //Building Notification
        val notificationBuilder = NotificationCompat.Builder(
              this,channelId
        ).setSmallIcon(R.drawable.ic_stat_ic_notification)
                .setContentTitle(title)   //Passing values which will be shown in Notification i.e title
                .setContentText(message)  //Passing message which is passed to the function
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent) // Take the user to the intialize activity through intent but once

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(channelId,
                    "Channel Projemanage title",
                    NotificationManager.IMPORTANCE_DEFAULT )
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(0,notificationBuilder.build())

    }

    private fun sendRegistrationToServer(token: String?){

    }
}