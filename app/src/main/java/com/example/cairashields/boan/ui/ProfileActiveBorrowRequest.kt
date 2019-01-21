package com.example.cairashields.boan.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.example.cairashields.boan.Objects.BorrowRequest
import com.example.cairashields.boan.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*


/*
    This class is for Borrowers to see their active borrow request
    for v1 they will not be able to edit it
 */

class ProfileActiveBorrowRequest: AppCompatActivity(){
    @BindView(R.id.username) lateinit var mUsername: TextView
    @BindView(R.id.amount) lateinit var mAmount: TextView
    @BindView(R.id.borrow_reason) lateinit var mBorrowReason: TextView
    @BindView(R.id.repay_date) lateinit var mRepayDate: TextView

    var mDatabaseReference: DatabaseReference? = null
    var mDatabaseReferenceUsers: DatabaseReference? = null
    var database: FirebaseDatabase? = null
    lateinit var auth: FirebaseAuth
    private var borrowRequest: BorrowRequest? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_active_borrow_request)
        ButterKnife.bind(this)

        database = FirebaseDatabase.getInstance()
        mDatabaseReference = database!!.reference.child("borrowRequests")
        auth = FirebaseAuth.getInstance()

        mUsername = findViewById(R.id.username)
        mAmount = findViewById(R.id.amount)
        mBorrowReason = findViewById(R.id.borrow_reason)
        mRepayDate = findViewById(R.id.repay_date)

        val title = intent.extras.getString(EXTRA_TITLE)
        val icon = intent.extras.getString(EXTRA_ICON)

        setTitle(title)

        //Start by grabbing the borrow request using the userId

        mDatabaseReference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                    for (dataSnapshot in snapshot.children) {

                        val innerBorrowRequest = dataSnapshot.getValue<BorrowRequest>(BorrowRequest::class.java)

                        // If the terms agreement is directed to the currently signed in user.. add it to the arrayList for the recycler
                        if(innerBorrowRequest!!.userId == auth.currentUser!!.uid){
                            borrowRequest = innerBorrowRequest

                            mUsername.text = innerBorrowRequest.username
                            mAmount.text = "$" + innerBorrowRequest.borrowAmount.toString()
                            if(innerBorrowRequest.borrowReason.isNotEmpty()){
                                mBorrowReason.text = innerBorrowRequest.borrowReason
                            }else{
                                mBorrowReason.text = "No borrow reason added."
                            }
                            val myFormat = "MM/dd/yy" //In which you need put here
                            val sdf = SimpleDateFormat(myFormat, Locale.US)
                            mRepayDate.text = "${sdf.format(innerBorrowRequest.repayDate)}"

                            break
                        }
                    }
                }

            override fun onCancelled(databaseError: DatabaseError) {}
            })

    }


    companion object {
        const val EXTRA_TITLE = "title"
        const val EXTRA_ICON = "icon"
    }
}