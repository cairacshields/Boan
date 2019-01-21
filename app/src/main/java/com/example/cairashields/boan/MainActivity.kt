package com.example.cairashields.boan

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import butterknife.BindView
import butterknife.ButterKnife
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import org.jetbrains.annotations.Nullable
import android.widget.Toast
import com.google.firebase.auth.FirebaseUser
import org.junit.experimental.results.ResultMatchers.isSuccessful
import com.google.firebase.auth.AuthResult
import com.google.android.gms.tasks.Task
import android.support.annotation.NonNull
import android.support.v4.view.ViewPager
import android.util.Log
import com.example.cairashields.boan.Objects.Users
import com.example.cairashields.boan.adapters.FragmentViewPagerAdapter
import com.example.cairashields.boan.events.Events
import com.example.cairashields.boan.ui.BorrowForm
import com.example.cairashields.boan.ui.CreditCardFragment
import com.example.cairashields.boan.ui.SignUpFragment
import com.example.cairashields.boan.ui.SwipeBorrowRequests
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.*
import com.wajahatkarim3.easyvalidation.core.view_ktx.textEqualTo
import com.wajahatkarim3.easyvalidation.core.view_ktx.validEmail
import com.wajahatkarim3.easyvalidation.core.view_ktx.validator
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    var mDatabaseReferenceUsers: DatabaseReference? = null
    var database: FirebaseDatabase? = null
    private val CHANNEL_ID = "BOAN_CHANNEL"
    private var user: Users? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)

        //Need to create notification channel for devices running API 8.0+
        createNotificationChannel()

        // Initialize Firebase Auth and database
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        mDatabaseReferenceUsers = database!!.reference.child("users")


        if(auth.currentUser != null){
            var mIsLender: Boolean? = null
            mDatabaseReferenceUsers!!.child(auth.currentUser!!.uid).addValueEventListener(object: ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    user =  snapshot.getValue(Users::class.java)
                    user?.let {user ->
                        mIsLender = user.isLender
                        if (mIsLender!!) {
                            //They're a lender....show the borrowRequest page
                            val intent = Intent(this@MainActivity, SwipeBorrowRequests::class.java)
                            startActivity(intent)
                        } else {
                            //They're a borrower... show the borrow form
                            val intent = Intent(this@MainActivity, BorrowForm::class.java)
                            startActivity(intent)
                        }
                    }
                }

            })
        }else{
            //They aren't logged in, or they are a new user... show the sign up page
            val intent = Intent(this, SignUpFragment::class.java)
            startActivity(intent)
        }

    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this);

    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this);

    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun updatePager(){
        val intent = Intent(this, CreditCardFragment::class.java)
        startActivity(intent)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: Events.UpdateViewPager) {
       updatePager()
    };
}

