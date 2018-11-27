package com.example.cairashields.boan.ui

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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.stripe.android.Stripe
import com.stripe.android.TokenCallback
import com.stripe.android.model.Token
import com.stripe.android.view.CardInputWidget
import javax.security.auth.callback.Callback

class CreditCardFragment : Fragment(){

    @BindView(R.id.card_input_widget) lateinit var mCardInputWidget: CardInputWidget
    @BindView(R.id.lend)lateinit var mLender: LinearLayout
    @BindView(R.id.borrow)lateinit var mBorrower: LinearLayout
    @BindView(R.id.complete_card_info)lateinit var mDone: Button


    var mDatabaseReference: DatabaseReference? = null
    private lateinit var auth: FirebaseAuth
    fun newInstance(): CreditCardFragment{
        return CreditCardFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.credit_card_fragment, container, false)
        ButterKnife.bind(this, root)
        mDatabaseReference = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()
        return root
    }

    override fun onStart() {
        super.onStart()
        mDone.setOnClickListener {
            val cardToSave = mCardInputWidget.card
            if (cardToSave == null) {
                //Card not good
               // mErrorDialogHandler.showError("Invalid Card Data")
            }else{
                //Card info is good
                var stripe =  Stripe(context!!, "pk_test_DkwOovybuSQN6dDkVLOODzn1");
                stripe.createToken(
                        cardToSave,
                        object : TokenCallback {
                            override fun onSuccess(token: Token?) {
                                Log.v("Token!","Token Created!!"+ token!!.getId())
                                createUserWithCard(token.getId()); // Pass that token to your Server for further processing
                            }

                            override fun onError(error: Exception?) {
                                Log.v("Token!","Token Not Created!!")
                                error!!.printStackTrace()
                            }

                        })

            }
        }
    }

    fun createUserWithCard(token: String){
        val name = auth.currentUser!!.displayName
        val email = auth.currentUser!!.email
        val user = Users(name, email, token)

        mDatabaseReference!!.child("users").child(auth.currentUser!!.uid).setValue(user)
    }
}