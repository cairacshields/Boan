package com.example.cairashields.boan.ui

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
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
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


class CategorySelectionPage :AppCompatActivity() {

    @BindView(R.id.lender_address_container)lateinit var mAddressContainer :LinearLayout
    @BindView(R.id.address_1)lateinit var mAddress1 : EditText
    @BindView(R.id.address_2)lateinit var mAddress2 : EditText
    @BindView(R.id.state)lateinit var mState: EditText
    @BindView(R.id.city)lateinit var mCity: EditText
    @BindView(R.id.zip)lateinit var mZip: EditText
    @BindView(R.id.done)lateinit var mDone: Button


    @BindView(R.id.category_container)lateinit var mCategoryContainer: LinearLayout
    @BindView(R.id.Sports) lateinit var mSportsButton: Button
    @BindView(R.id.makeup)lateinit var mMakeup: Button
    @BindView(R.id.fashion)lateinit var mFashion: Button
    @BindView(R.id.food)lateinit var mFood: Button
    @BindView(R.id.movies)lateinit var mMovies : Button
    @BindView(R.id.tech)lateinit var mTech : Button

    var mDatabaseReference: DatabaseReference? = null
    private lateinit var auth: FirebaseAuth

    var giftCategory: String? = null
    var address: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gift_category)
        ButterKnife.bind(this)

        val database = FirebaseDatabase.getInstance()
        mDatabaseReference = database.reference
        auth = FirebaseAuth.getInstance()

        mAddressContainer = findViewById(R.id.lender_address_container)
        mAddress1 = findViewById(R.id.address_1)
        mAddress2 = findViewById(R.id.address_2)
        mState = findViewById(R.id.state)
        mCity = findViewById(R.id.city)
        mZip = findViewById(R.id.zip)
        mDone = findViewById(R.id.done)



        mCategoryContainer = findViewById(R.id.category_container)
        mSportsButton = findViewById(R.id.Sports)
        mMakeup = findViewById(R.id.makeup)
        mFashion = findViewById(R.id.fashion)
        mFood = findViewById(R.id.food)
        mMovies = findViewById(R.id.movies)
        mTech = findViewById(R.id.tech)

        val intent = getIntent()
        val code = intent.getStringExtra("code")

        mAddressContainer.visibility = View.GONE

        mSportsButton.setOnClickListener {
            giftCategory = "sports"
            mCategoryContainer.visibility = View.GONE
            mAddressContainer.visibility = View.VISIBLE

        }

        mTech.setOnClickListener {
            giftCategory = "tech"
            mCategoryContainer.visibility = View.GONE
            mAddressContainer.visibility = View.VISIBLE
        }

        mFashion.setOnClickListener {
            giftCategory = "fashion"
            mCategoryContainer.visibility = View.GONE
            mAddressContainer.visibility = View.VISIBLE
        }

        mFood.setOnClickListener {
            giftCategory = "food"
            mCategoryContainer.visibility = View.GONE
            mAddressContainer.visibility = View.VISIBLE
        }
        mMovies.setOnClickListener {
            giftCategory = "movies"
            mCategoryContainer.visibility = View.GONE
            mAddressContainer.visibility = View.VISIBLE
        }

        mMakeup.setOnClickListener {
            giftCategory = "makeup"
            mCategoryContainer.visibility = View.GONE
            mAddressContainer.visibility = View.VISIBLE
        }

        mDone.setOnClickListener {
            //Make sure values are not empty
            if(!mAddress1.text.isNullOrEmpty() && !mState.text.isNullOrEmpty() && !mCity.text.isNullOrEmpty() && !mZip.text.isNullOrEmpty()){

                if(!mAddress2.text.isEmpty()){
                    address = "${mAddress1.text} ${mAddress2.text} ${mCity.text} , ${mState.text} ${mZip.text}"
                }else address = "${mAddress1.text} ${mCity.text} , ${mState.text} ${mZip.text}"

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
            }else{
                Toast.makeText(this, "Values cannot be empty", Toast.LENGTH_LONG).show()
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
            val user = Users(name, email, null, true, firebaseToken, null, null, giftCategory, address, null, false, null, null, null)
            mDatabaseReference!!.child("users").child(auth.currentUser!!.uid).setValue(user).addOnCompleteListener {
                Log.v("USER ADDED: ", "check database now.")
                val intent = Intent(this, SwipeBorrowRequests::class.java)
                intent.putExtra("isLender", true)
                startActivity(intent)
            }
        } else {
            Log.v("BAD TOKEN", "firebase token is NULL")
            Toast.makeText(this, "FIREBASE TOKEN NULL", Toast.LENGTH_LONG).show()
        }
    }

}