package com.example.cairashields.boan.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import butterknife.BindView
import butterknife.ButterKnife
import com.example.cairashields.boan.MainActivity
import com.example.cairashields.boan.R
import com.example.cairashields.boan.Services.FirebaseInstanceIdService
import com.example.cairashields.boan.events.Events
import com.example.cairashields.boan.stripe.ConnectExpress
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.wajahatkarim3.easyvalidation.core.view_ktx.textEqualTo
import com.wajahatkarim3.easyvalidation.core.view_ktx.validEmail
import com.wajahatkarim3.easyvalidation.core.view_ktx.validator
import org.greenrobot.eventbus.EventBus
import org.jetbrains.annotations.Nullable

class SignUpFragment: AppCompatActivity(){
    @Nullable @BindView(R.id.sign_in)lateinit var mSignUp: Button
    @Nullable @BindView(R.id.email) lateinit var mEmail: EditText
    @Nullable @BindView(R.id.password) lateinit var mPassword: EditText
    @Nullable @BindView(R.id.password_verify) lateinit var mPasswordVerify: EditText
    @Nullable @BindView(R.id.user_name) lateinit var mUserName: EditText
    @Nullable @BindView(R.id.already_have_account)lateinit var mExistingAccount: TextView

    val RC_SIGN_IN = 100
    val TAG = "SIGN UP"
    private lateinit var auth: FirebaseAuth
    var mDatabaseReference: DatabaseReference? = null

    val providers = arrayListOf(AuthUI.IdpConfig.EmailBuilder().build())

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        ButterKnife.bind(this)
        setContentView(R.layout.sign_up_fragment)

        mSignUp = findViewById(R.id.sign_in)
        mEmail = findViewById(R.id.email)
        mUserName = findViewById(R.id.user_name)
        mPassword = findViewById(R.id.password)
        mPasswordVerify = findViewById(R.id.password_verify)
        mExistingAccount = findViewById(R.id.already_have_account)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        val database = FirebaseDatabase.getInstance()
        mDatabaseReference = database.reference.child("users").child("firebaseToken")

        mExistingAccount.setOnClickListener {
            val intent = Intent(this, SignInFragment::class.java)
            startActivity(intent)
        }

    }

    override fun onStart() {
        super.onStart()

        val currentUser = auth.currentUser

        if(currentUser == null){
            //User not logged in
        }else{
            //User is logged in
        }
        var email:String?
        var password: String?
        var passwordVerify: String?

        mSignUp.setOnClickListener {
            email = mEmail.text.toString()
            password = mPassword.text.toString()
            passwordVerify = mPasswordVerify.text.toString()


            val passwordsEqual = password!!.textEqualTo(passwordVerify!!)

            val emailValid = email!!.validEmail() {
                // This method will be called when myEmailStr is not a valid email.
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
            val passwordValid = password!!.validator()
                    .nonEmpty()
                    .atleastOneNumber()
                    .atleastOneSpecialCharacters()
                    .atleastOneUpperCase()
                    .addErrorCallback {
                        mPassword.error = it
                        // it will contain the right message.
                        // For example, if edit text is empty,
                        // then 'it' will show "Can't be Empty" message
                        Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                    }
                    .check()
            if(emailValid && passwordValid && passwordsEqual) {
                auth.createUserWithEmailAndPassword(email!!, password!!)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "createUserWithEmail:success")
                                val user = auth.currentUser
                                val profileUpdates = UserProfileChangeRequest.Builder()
                                        .setDisplayName(mUserName.text.toString())
                                        //.setPhotoUri(Uri.parse("https://example.com/jane-q-user/profile.jpg"))
                                        .build()

                                user?.updateProfile(profileUpdates)
                                        ?.addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                Log.d(TAG, "User profile updated.")

                                                //Redirect to the main app...
                                                val intent = Intent(this, ConnectExpress::class.java)
                                                startActivity(intent)
                                            }
                                        }

                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "createUserWithEmail:failure", task.exception)
                                Toast.makeText(this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show()

                            }

                            // ...
                        }
            }else Log.e(TAG, "Password valid? $password and email valid? $emailValid")

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                val user = FirebaseAuth.getInstance().currentUser
                // ...
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
        }
    }
}