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
import android.widget.*
import butterknife.BindView
import butterknife.ButterKnife
import com.example.cairashields.boan.R
import com.example.cairashields.boan.Services.FirebaseInstanceIdService
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.wajahatkarim3.easyvalidation.core.view_ktx.validEmail
import com.wajahatkarim3.easyvalidation.core.view_ktx.validator
import org.jetbrains.annotations.Nullable
import org.junit.experimental.results.ResultMatchers.isSuccessful
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.OnCompleteListener



class SignInFragment: AppCompatActivity() {

    @Nullable @BindView(R.id.sign_in_container)lateinit var mSignInContainer: LinearLayout
    @Nullable @BindView(R.id.sign_in_forgot_container)lateinit var mSignInForgotEmpty: LinearLayout
    @Nullable @BindView(R.id.forgot_password_email)lateinit var mForgotPasswordEmail: EditText
    @Nullable @BindView(R.id.forgot_password_button) lateinit var mForgotPasswordButton: Button
    @Nullable @BindView(R.id.email) lateinit var mEmail: EditText
    @Nullable @BindView(R.id.password) lateinit var mPassword: EditText
    @Nullable @BindView(R.id.login)lateinit var mLogin: Button
    @Nullable @BindView(R.id.forgot_password)lateinit var mForgotPassword: TextView

    val RC_SIGN_IN = 100
    val TAG = "SIGN IN"
    private lateinit var auth: FirebaseAuth
    var mDatabaseReference: DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_sign_in)
        ButterKnife.bind(this)

        mSignInContainer = findViewById(R.id.sign_in_container)
        mSignInForgotEmpty = findViewById(R.id.sign_in_forgot_container)
        mForgotPasswordButton = findViewById(R.id.forgot_password_button)
        mForgotPasswordEmail = findViewById(R.id.forgot_password_email)
        mEmail = findViewById(R.id.email)
        mPassword = findViewById(R.id.password)
        mLogin = findViewById(R.id.login)
        mForgotPassword = findViewById(R.id.forgot_password)

        mSignInForgotEmpty.visibility = View.GONE
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        val database = FirebaseDatabase.getInstance()
        mDatabaseReference = database.reference.child("users")

        mForgotPassword.setOnClickListener {
            mSignInContainer.visibility = View.GONE
            mSignInForgotEmpty.visibility = View.VISIBLE
        }
        mForgotPasswordButton.setOnClickListener {
            //TODO Add checks for email edit text... just to make sure it's not empty and is an actual email
            FirebaseAuth.getInstance().sendPasswordResetEmail(mForgotPasswordEmail.text.toString())
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Password reset email sent", Toast.LENGTH_LONG).show()
                            Log.d("RESET EMAIL SENT", "Email sent.")

                        }else{
                            Toast.makeText(this, "Password reset email wasn't sent", Toast.LENGTH_LONG).show()
                            Log.d("RESET EMAIL NOT SENT", "Email Not sent.")
                        }
                        mSignInContainer.visibility = View.VISIBLE
                        mSignInForgotEmpty.visibility = View.GONE
                    }
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
        mLogin.setOnClickListener {
            email = mEmail.text.toString()
            password = mPassword.text.toString()

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
            if(emailValid && passwordValid) {
                auth.signInWithEmailAndPassword(email!!, password!!)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInWithEmail:success")
                                val user = auth.currentUser

                                //update the firebaseToken on each signIn
                                mDatabaseReference!!.setValue(FirebaseInstanceIdService.getRefreshedToken())

                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInWithEmail:failure", task.exception)
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