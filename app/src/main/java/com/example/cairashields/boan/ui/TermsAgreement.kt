package com.example.cairashields.boan.ui

import android.content.Intent
import android.graphics.Point
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import butterknife.BindView
import butterknife.ButterKnife
import com.example.cairashields.boan.Objects.BorrowRequest
import com.example.cairashields.boan.Objects.Users
import com.example.cairashields.boan.R
import com.firebase.ui.auth.data.model.User
import com.google.firebase.database.*
import java.io.Serializable
import java.text.DecimalFormat
import android.opengl.ETC1.getWidth
import android.util.Log
import android.widget.*
import com.example.cairashields.boan.Messaging.NotificationManager
import com.example.cairashields.boan.Objects.TermAgreement
import com.google.firebase.auth.FirebaseAuth
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.Response
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import org.w3c.dom.Text
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt


class TermsAgreement :AppCompatActivity(), Serializable{


    @BindView(R.id.seekbar)lateinit var mSeekBar: SeekBar
    @BindView(R.id.repay_amount)lateinit var mRepayAmount: TextView
    @BindView(R.id.borrow_amount) lateinit var mBorrowAmount: TextView
    @BindView(R.id.borrow_repay_date)lateinit var mRepayDate: TextView
    @BindView(R.id.username) lateinit var mUsername: TextView
    @BindView(R.id.user_image)lateinit var mUserImage: ImageView
    @BindView(R.id.interest)lateinit var mInterest: TextView
    @BindView(R.id.add_comment)lateinit var mComment: EditText
    @BindView(R.id.submit)lateinit var mSubmitAgreement: Button

    private var mBorrowRequest: BorrowRequest? = null
    var mDatabaseReferenceUsers: DatabaseReference? = null
    var mDatabaseReference: DatabaseReference? = null
    var database: FirebaseDatabase? = null
    var mBorrowingUser : Users? = null
    var newRepayAmount: Int? = null
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms_agreement)
        ButterKnife.bind(this)

        database = FirebaseDatabase.getInstance()
        mDatabaseReferenceUsers = database!!.reference.child("users")
        mDatabaseReference = database!!.reference.child("termsAgreements")
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()


        mSeekBar = findViewById(R.id.seekbar)
        mRepayAmount = findViewById(R.id.repay_amount)
        mBorrowAmount = findViewById(R.id.borrow_amount)
        mRepayDate = findViewById(R.id.borrow_repay_date)
        mUsername = findViewById(R.id.username)
        mUserImage = findViewById(R.id.user_image)
        mInterest = findViewById(R.id.interest)
        mComment = findViewById(R.id.add_comment)
        mSubmitAgreement = findViewById(R.id.submit)

        val intent = this.intent
        mBorrowRequest = intent.getSerializableExtra("borrow_request") as BorrowRequest

        mDatabaseReferenceUsers!!.child(mBorrowRequest!!.userId).addValueEventListener(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                mBorrowingUser = dataSnapshot.getValue(Users::class.java)
            }

        })

        mBorrowRequest?.let {
            mUsername.text = it.username
            mBorrowAmount.text = "$" + it.borrowAmount.toString()
            val myFormat = "MM/dd/yy" //In which you need put here
            val sdf = SimpleDateFormat(myFormat, Locale.US)
            mRepayDate.text = "What you'll get on ${sdf.format(it.repayDate)}"
            mRepayAmount.text = "$" + it.borrowAmount.toString()

        }


        mSeekBar.max = 50
        var maxSizePoint = Point()
        getWindowManager().getDefaultDisplay().getSize(maxSizePoint);
        var maxX = maxSizePoint.x
        mSeekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val secondaryProgress = progress - 85
                mSeekBar.secondaryProgress = secondaryProgress

                val decimalFormat = DecimalFormat("0.0%")
                val decimalFromat2 = DecimalFormat("0.0")
                val progressPercentageFloat = progress.toFloat() / seekBar!!.getMax().toFloat()
                val progressPercentage = decimalFormat.format(progressPercentageFloat);
                val secondaryProgressPercentageFloat = secondaryProgress.toFloat() / seekBar.max.toFloat()
                val secondaryProgressPercentage = decimalFormat.format(secondaryProgressPercentageFloat);

                mInterest.text = progressPercentage
                val originalRepayAmount = mBorrowRequest!!.borrowAmount
                val progressPercentage2 = (decimalFromat2.format(progressPercentageFloat).toFloat()).times(100)
                val floatedProgress = originalRepayAmount.times(progressPercentage2)
                newRepayAmount = (floatedProgress / 100).roundToInt() + mBorrowRequest!!.borrowAmount
                mRepayAmount.text =("$$newRepayAmount").toString()

                val value = progress * (seekBar.width - 2 * seekBar.thumbOffset) / seekBar.max
                val textViewX = value - (mInterest.width / 2);
                val finalX =if(mInterest.width + textViewX > maxX) {
                    (maxX - mInterest.width - 8)
                } else textViewX + 8
                val endFinalValue = if(finalX < 0){
                    8f
                }else (finalX).toFloat()
                mInterest.setX(endFinalValue)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })

        mSubmitAgreement.setOnClickListener {
            //Post the new terms agreement
            //Send the Borrower a notification
            //Take Lender back to borrow requests activity
//            mDatabaseReference!!.addValueEventListener(object: ValueEventListener{
//                override fun onCancelled(p0: DatabaseError) {
//
//                }
//
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    var alreadySentAgreement = false
//                    var notificationResponse: com.squareup.okhttp.Response?
//                    for (dataSnapshot in snapshot.children) {
//                        val termsAgreement = dataSnapshot.getValue<TermAgreement>(TermAgreement::class.java)
//                        if(termsAgreement!!.lenderUserId == auth.currentUser!!.uid && termsAgreement.borrowerUserId == mBorrowRequest!!.userId){
//                            Toast.makeText(this@TermsAgreement, "Cannot send multiple agreements", Toast.LENGTH_LONG).show()
//                            alreadySentAgreement = true
//                        }
//                    }
//                    //Only post an agreement if one hasn't already been posted
//                    if(alreadySentAgreement == false){
                        val comment = if(mComment.text != null) mComment.text.toString() else ""
                        // Note ** the id of a term agreement ftm is the (lender uid + borrower uid)
                        mDatabaseReference!!.child(auth.currentUser!!.uid+mBorrowRequest!!.userId).setValue(TermAgreement(auth.currentUser!!.uid,mBorrowRequest!!.borrowAmount, newRepayAmount!!,  mBorrowRequest!!.repayDate,
                                mBorrowRequest!!.userId, auth.currentUser!!.uid, auth.currentUser!!.displayName, auth.currentUser!!.email, comment, false))
                                .addOnCompleteListener{ it ->
                                    when {
                                        it.isSuccessful -> {
                                            Toast.makeText(this@TermsAgreement, "Terms Agreement published!", Toast.LENGTH_LONG).show()
                                            Log.v("Terms Agreement status:", " Success")
                                            //Take to main profile?
                                            val intent = Intent(this@TermsAgreement, SwipeBorrowRequests::class.java)
                                            startActivity(intent)

                                            //Send the Borrower a notification
                                          NotificationManager.sendNotificationToUser("Congrats!",auth.currentUser!!.displayName + " has sent you a terms agreement.",
                                                    mBorrowingUser!!.firebaseToken)
                                                  .subscribeOn(Schedulers.io())
                                                  .observeOn(AndroidSchedulers.mainThread())
                                                  .subscribe {response ->
                                                      if(response.isSuccessful){
                                                          Log.v("Notification Response ","Success " + response.body())
                                                          Toast.makeText(this@TermsAgreement, "Response is SUCCESS", Toast.LENGTH_LONG).show()
                                                      }else{
                                                          Log.v("Notification Response ","FAILED " + response.body())
                                                          Toast.makeText(this@TermsAgreement, "Response is Failed", Toast.LENGTH_LONG).show()
                                                      }
                                                  }

                                        }
                                        it.isCanceled -> Log.v("Terms agreement status ","Cancled")
                                        else -> Log.v("Terms agreement status ", "Something went wrong")
                                    }
                                }
//                    }else{
//                        //An agreement from this lender to this borrower has already been posted
//                        //Toast.makeText(this@TermsAgreement, "An agreement already exists", Toast.LENGTH_LONG).show()
//                        Log.v("Terms agreement status ", "Already sent agreement")
//                    }
//                }
//
//            })
        }

    }
}