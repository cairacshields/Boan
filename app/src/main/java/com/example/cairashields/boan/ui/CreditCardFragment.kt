package com.example.cairashields.boan.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
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
import com.example.cairashields.boan.stripe.FetchConnectedUserId
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.stripe.android.Stripe
import com.stripe.android.TokenCallback
import com.stripe.android.model.Customer
import com.stripe.android.model.Token
import com.stripe.android.view.CardInputWidget
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.security.auth.callback.Callback

class CreditCardFragment : AppCompatActivity() {

    @BindView(R.id.lend)
    lateinit var mLender: CardView
    @BindView(R.id.borrow)
    lateinit var mBorrower: CardView
    @BindView(R.id.complete_card_info)
    lateinit var mDone: Button


    var mDatabaseReference: DatabaseReference? = null
    private lateinit var auth: FirebaseAuth

    var isLender = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ButterKnife.bind(this)
        setContentView(R.layout.credit_card_fragment)
        val database = FirebaseDatabase.getInstance()
        mDatabaseReference = database.reference
        auth = FirebaseAuth.getInstance()

        mLender = findViewById(R.id.lend)
        mBorrower = findViewById(R.id.borrow)
        mDone = findViewById(R.id.complete_card_info)

        //Used for detecting deep links
        val action: String? = intent?.action
        val data: Uri? = intent?.data

        val code = data!!.getQueryParameter("code")

        mLender.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mLender.setBackgroundColor(this.getColor(R.color.colorPrimaryDark))
                mBorrower.setBackgroundColor(this.getColor(R.color.white))

            }
            isLender = true
            mDone.text = "Next"
        }

        mBorrower.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mBorrower.setBackgroundColor(this.getColor(R.color.colorPrimaryDark))
                mLender.setBackgroundColor(this.getColor(R.color.white))
            }
            isLender = false
            mDone.text = "Done"
        }

        mDone.setOnClickListener {
            if(isLender){
                // Bring to category selection page
                val intent = Intent(this, CategorySelectionPage::class.java)
                intent.putExtra("code", code)
                startActivity(intent)
            }else{
                //Just create the user
                createUser()
                FetchConnectedUserId.getId(code, auth.currentUser!!.uid)!!
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe( { response ->
                            if(response.isSuccessful){
                                Log.d("Connected user", "Creation SUCCESS " + response.body().string())
                                Toast.makeText(this, "Creation SUCCESS", Toast.LENGTH_LONG).show()
                            }else{
                                Log.d("Connected user", "Creation ERROR " + response.body())
                                Toast.makeText(this, "Creation ERROR", Toast.LENGTH_LONG).show()
                            }
                        }, { e -> e.printStackTrace() })
            }
        }
    }

    fun createUser(){
        //using this createUser method... users will need to add card info later on while using the app
        val name = auth.currentUser!!.displayName
        val email = auth.currentUser!!.email
        val firebaseToken: String?
        auth.currentUser!!.getIdToken(true)

        firebaseToken = FirebaseInstanceIdService.getRefreshedToken()
        if (firebaseToken != null) {
            val user = Users(name, email, null, isLender, firebaseToken, null, null, null, null, null, false, null, null, null)
            mDatabaseReference!!.child("users").child(auth.currentUser!!.uid).setValue(user).addOnCompleteListener {
                Log.v("USER ADDED: ", "check database now.")
                val intent = Intent(this, BorrowForm::class.java)
                intent.putExtra("isLender", isLender)
                startActivity(intent)

            }
        } else {
            Log.v("BAD TOKEN", "firebase token is NULL")
            Toast.makeText(this, "FIREBASE TOKEN NULL", Toast.LENGTH_LONG).show()
        }
    }
}