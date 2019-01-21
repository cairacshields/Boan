package com.example.cairashields.boan.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.BindView
import butterknife.ButterKnife
import com.example.cairashields.boan.Objects.BorrowRequest
import com.example.cairashields.boan.Objects.TermAgreement
import com.example.cairashields.boan.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.widget.*
import butterknife.OnClick
import com.example.cairashields.boan.Messaging.NotificationManager
import com.example.cairashields.boan.Objects.Users
import com.example.cairashields.boan.charges.Charges
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_recycler.*
import java.text.SimpleDateFormat
import java.util.*


/*
    This class should only show for Borrowers
    It will display all of their TermsAgreements as a list where they can Accept/Decline them
 */
class ProfileBorrowRequests: AppCompatActivity() {

    @BindView(R.id.content)lateinit var mContent: RecyclerView
    @BindView(R.id.empty_terms_agreement_page) lateinit var mEmptyContent: RelativeLayout

    var mDatabaseReference: DatabaseReference? = null
    var mDatabaseReferenceUsers: DatabaseReference? = null
    var database: FirebaseDatabase? = null
    lateinit var auth: FirebaseAuth
    var mAdapter: TermsRecyclerAdapter? = null

    private var mArrayList = arrayListOf<TermAgreement>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycler)
        ButterKnife.bind(this)
        database = FirebaseDatabase.getInstance()
        mDatabaseReference = database!!.reference.child("termsAgreements")
        auth = FirebaseAuth.getInstance()

        mContent = findViewById(R.id.content)
        mEmptyContent = findViewById(R.id.empty_terms_agreement_page)

        val title = intent.extras.getString(EXTRA_TITLE)
        val icon = intent.extras.getString(EXTRA_ICON)

        setTitle(title)

        mDatabaseReference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if(!snapshot.hasChildren()){
                    //Nothing to show in the recycler
                    //Show empty page
                    mContent.visibility = View.GONE
                    mEmptyContent.visibility = View.VISIBLE


                }else{
                    mContent.visibility = View.VISIBLE
                    mEmptyContent.visibility = View.GONE
                    for (dataSnapshot in snapshot.children) {

                        val termsAgreement = dataSnapshot.getValue<TermAgreement>(TermAgreement::class.java)

                        // If the terms agreement is directed to the currently signed in user.. add it to the arrayList for the recycler
                        if(termsAgreement!!.borrowerUserId == auth.currentUser!!.uid){
                            //Probably check the value of 'accepted' in a termsAgreement....
                            //If it's already been accepted, we shouldn't show it... or maybe show it somewhere else?
                            mArrayList.add(termsAgreement)
                        }
                    }
                    mContent.setHasFixedSize(false)
                    mAdapter = TermsRecyclerAdapter(mArrayList)
                    mContent.adapter = mAdapter
                    mContent.layoutManager = LinearLayoutManager(this@ProfileBorrowRequests)
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    class TermsRecyclerAdapter(list: ArrayList<TermAgreement>): RecyclerView.Adapter<TermsRecyclerAdapter.ViewHolder>(){
        private val items: List<TermAgreement> = list
        private var lender: Users? = null
        var database = FirebaseDatabase.getInstance()
        var auth = FirebaseAuth.getInstance()

        var mDatabaseReferenceUsers = database.reference.child("users")
        var mDatabaseReferenceTerms = database.reference.child("termsAgreements")



        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_term_agreement, parent, false);
            return ViewHolder(v);
        }

        override fun getItemCount(): Int {
            return items.size
        }

        fun getLenderFirbaseToken(lenderUid: String): Users{
            mDatabaseReferenceUsers.child(lenderUid).addValueEventListener(object: ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    lender = dataSnapshot.getValue(Users::class.java)
                    Log.v("firebaseToken", lender!!.firebaseToken)
                }

            })
           return  lender!!
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val termAgreement = items.get(position)
            holder.mLenderName.text = termAgreement.username
            holder.mRepayAmount.text = termAgreement.repayAmount.toString()

            val myFormat = "MM/dd/yy"
            val sdf = SimpleDateFormat(myFormat, Locale.US)
            val date = sdf.format(termAgreement.repayDate)
            holder.mRepayDate.text = date

            holder.mAccept.setOnClickListener {


                val builder = AlertDialog.Builder(holder.itemView.context)
                val message = holder.itemView.resources.getString(R.string.terms_agreement_message, auth.currentUser!!.displayName,
                        termAgreement.username, termAgreement.repayAmount.toString(),date)
                builder.setTitle("Please review terms and agreements")
                builder.setMessage(message)
                builder.setPositiveButton("Accept") { dialog, which ->
                    //Borrower has accepted the terms and agreements, send payment and notification

                    mDatabaseReferenceUsers.child(termAgreement.lenderUserId).addListenerForSingleValueEvent(object: ValueEventListener{
                        override fun onCancelled(p0: DatabaseError) {
                        }

                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            lender = dataSnapshot.getValue(Users::class.java)

                            //Send the Lender a notification
                            NotificationManager.sendNotificationToUser("Congrats!",auth.currentUser!!.displayName + " has accepted your terms agreement.",
                                    lender!!.firebaseToken)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe {response ->
                                        if(response.isSuccessful){
                                            Log.v("Notification Response ","Success " + response.body())
                                            Toast.makeText(holder.itemView.context, "Response is SUCCESS", Toast.LENGTH_LONG).show()
                                        }else{
                                            Log.v("Notification Response ","FAILED " + response.body())
                                            Toast.makeText(holder.itemView.context, "Response is Failed", Toast.LENGTH_LONG).show()
                                        }
                                    }
                            //charge the lender... send the borrower the money
                            lender?.let {lender ->
                                if (lender.token != null) {
                                    val centAmount = termAgreement.borrowAmount * 100
                                    Charges.putCharge(lender.token, lender.email, centAmount.toFloat(), termAgreement.lenderUserId, termAgreement.borrowerUserId)!!
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe({ response ->

                                                if (response.isSuccessful) {
                                                    Log.d("CHARGE REQUEST", response.message() + " " + response.body())
                                                    //Update the termsAgreement 'accepted' value
                                                    mDatabaseReferenceTerms.child(termAgreement.lenderUserId).child("accepted").setValue(true)

                                                } else if (response.isRedirect) {
                                                    Log.d("CHARGE REQUEST", "is redirect?")
                                                } else {
                                                    Log.d("CHARGE REQUEST", response.body().string())
                                                }

                                            }, { e -> e.printStackTrace() }

                                            )
                                }else{
                                    //Show the alert dialog to add card info
                                    Toast.makeText(holder.itemView.context, "Unable to charge the lender at this time. Please try again later.", Toast.LENGTH_LONG).show()
                                }
                            }


                            Log.v("firebaseToken", lender!!.firebaseToken)
                        }

                    })

                    // add a one off charge to the borrower for the repay amount, scheduled on the repay date


                    //update borrowRequest 'requestClosed' value in DB

                }
                builder.setNegativeButton("Decline"){dialog, which ->
                    //Send the Lender a notification
                    NotificationManager.sendNotificationToUser("Declined!",auth.currentUser!!.displayName + " has declined your terms agreement.",
                            getLenderFirbaseToken(termAgreement.lenderUserId).firebaseToken)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe {response ->
                                if(response.isSuccessful){
                                    Log.v("Notification Response ","Success " + response.body())
                                    Toast.makeText(holder.itemView.context, "Response is SUCCESS", Toast.LENGTH_LONG).show()
                                }else{
                                    Log.v("Notification Response ","FAILED " + response.body())
                                    Toast.makeText(holder.itemView.context, "Response is Failed", Toast.LENGTH_LONG).show()
                                }
                            }
                    //Remove the terms agreement from the DB.
                    mDatabaseReferenceTerms.child(termAgreement.id+termAgreement.borrowerUserId).removeValue()
                }
                val dialog: AlertDialog = builder.create()
                dialog.show()


                /*TODO...
                 1. When this button is clicked, we need to charge the lender... send the borrower the money
                 2. remove the borrow request from the DB (or maybe add a value that prevents it from being shown in the swipe requests page... just to keep a record)
                 3. add a one off charge to the borrower for the repay amount, scheduled on the repay date
                 4. also show a modal with the serious legal agreement stuff initially before actually completing any charges
                 5.

                 */

            }

            holder.mDecline.setOnClickListener {

                //Send the Lender a notification
                NotificationManager.sendNotificationToUser("Declined!",auth.currentUser!!.displayName + " has declined your terms agreement.",
                        getLenderFirbaseToken(termAgreement.lenderUserId).firebaseToken)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {response ->
                            if(response.isSuccessful){
                                Log.v("Notification Response ","Success " + response.body())
                                Toast.makeText(holder.itemView.context, "Response is SUCCESS", Toast.LENGTH_LONG).show()
                            }else{
                                Log.v("Notification Response ","FAILED " + response.body())
                                Toast.makeText(holder.itemView.context, "Response is Failed", Toast.LENGTH_LONG).show()
                            }
                        }
                //Remove the terms agreement from the DB.
                mDatabaseReferenceTerms.child(termAgreement.id+termAgreement.borrowerUserId).removeValue()
            }
        }

        class ViewHolder: RecyclerView.ViewHolder{
            @BindView(R.id.repay_amount) var mRepayAmount: TextView
            @BindView(R.id.repay_date) var mRepayDate: TextView
            @BindView(R.id.lender_name) var mLenderName: TextView
            @BindView(R.id.decline) var mDecline: Button
            @BindView(R.id.accept) var mAccept: Button

            constructor(itemView: View):super(itemView){
                ButterKnife.bind(itemView)

                mRepayDate = itemView.findViewById(R.id.repay_date)
                mRepayAmount = itemView.findViewById(R.id.repay_amount)
                mLenderName = itemView.findViewById(R.id.lender_name)
                mDecline = itemView.findViewById(R.id.decline)
                mAccept = itemView.findViewById(R.id.accept)
            }
        }
    }

    companion object {
        const val EXTRA_TITLE = "title"
        const val EXTRA_ICON = "icon"
    }
}