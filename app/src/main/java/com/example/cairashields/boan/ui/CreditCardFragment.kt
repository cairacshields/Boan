package com.example.cairashields.boan.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import com.example.cairashields.boan.Objects.Users
import com.example.cairashields.boan.R
import com.example.cairashields.boan.Services.FirebaseInstanceIdService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.stripe.android.Stripe
import com.stripe.android.TokenCallback
import com.stripe.android.model.Customer
import com.stripe.android.model.Token
import com.stripe.android.view.CardInputWidget
import javax.security.auth.callback.Callback

class CreditCardFragment : Fragment() {

    @BindView(R.id.card_input_widget)
    lateinit var mCardInputWidget: CardInputWidget
    @BindView(R.id.lend)
    lateinit var mLender: LinearLayout
    @BindView(R.id.borrow)
    lateinit var mBorrower: LinearLayout
    @BindView(R.id.complete_card_info)
    lateinit var mDone: Button


    var mDatabaseReference: DatabaseReference? = null
    private lateinit var auth: FirebaseAuth

    var isLender = false
    fun newInstance(): CreditCardFragment {
        return CreditCardFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.credit_card_fragment, container, false)
        ButterKnife.bind(this, root)
        val database = FirebaseDatabase.getInstance()
        mDatabaseReference = database.reference
        auth = FirebaseAuth.getInstance()

        mLender = root.findViewById(R.id.lend)
        mBorrower = root.findViewById(R.id.borrow)
        mDone = root.findViewById(R.id.complete_card_info)
        mCardInputWidget = root.findViewById(R.id.card_input_widget)

        mLender.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mLender.setBackgroundColor(context!!.getColor(R.color.colorPrimary))
                mBorrower.setBackgroundColor(context!!.getColor(R.color.white))

            }
            isLender = true
        }
        mBorrower.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mBorrower.setBackgroundColor(context!!.getColor(R.color.colorPrimary))
                mLender.setBackgroundColor(context!!.getColor(R.color.white))
            }
            isLender = false
        }
        mDone.setOnClickListener {
            val cardToSave = mCardInputWidget.card

            if (cardToSave == null) {
                //Card not good
                // mErrorDialogHandle r.showError("Invalid Card Data")
            } else {
                //Card info is good
                val stripe = Stripe(context!!, "pk_test_DkwOovybuSQN6dDkVLOODzn1");
                stripe.createToken(
                        cardToSave,
                        object : TokenCallback {
                            override fun onSuccess(token: Token?) {
                                Log.v("Token!", "Token Created!!" + token!!.getId())
                                createUserWithCard(token.getId()); // Pass that token to your Server for further processing
                            }

                            override fun onError(error: Exception?) {
                                Log.v("Token!", "Token Not Created!!")
                                error!!.printStackTrace()
                                //TODO Handle what happens when card is invalid... currently app crashes *face palm*
                            }

                        })

            }
        }
        return root
    }

    fun createUserWithCard(token: String) {
        val name = auth.currentUser!!.displayName
        val email = auth.currentUser!!.email
        val firebaseToken: String?
        auth.currentUser!!.getIdToken(true)

        firebaseToken = FirebaseInstanceIdService.getRefreshedToken()
        if (firebaseToken != null) {
            val user = Users(name, email, token, isLender, firebaseToken, null, null)
            mDatabaseReference!!.child("users").child(auth.currentUser!!.uid).setValue(user).addOnCompleteListener {
                Log.v("USER ADDED: ", "check database now.")
                //TODO CHECK IF LENDER OR BORROWER AND SEND TO CORRECT MAIN PAGE
                val intent = Intent(context, SwipeBorrowRequests::class.java)
                startActivity(intent)
            }
        } else {
            Log.v("BAD TOKEN", "firebase token is NULL")
            Toast.makeText(activity, "FIREBASE TOKEN NULL", Toast.LENGTH_LONG).show()
        }
    }
}