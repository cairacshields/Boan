package com.example.cairashields.boan

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
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
import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.UserProfileChangeRequest
import com.wajahatkarim3.easyvalidation.core.view_ktx.textEqualTo
import com.wajahatkarim3.easyvalidation.core.view_ktx.validEmail
import com.wajahatkarim3.easyvalidation.core.view_ktx.validator


class MainActivity : AppCompatActivity() {
    @Nullable @BindView(R.id.sign_in) lateinit var mSignIn: Button
    @Nullable @BindView(R.id.sign_up) lateinit var mSignUp: Button
    @Nullable @BindView(R.id.email) lateinit var mEmail: EditText
    @Nullable @BindView(R.id.password) lateinit var mPassword: EditText
    @Nullable @BindView(R.id.password_verify) lateinit var mPasswordVerify: EditText
    @Nullable @BindView(R.id.user_name) lateinit var mUserName: EditText

    val RC_SIGN_IN = 100
    val TAG = "SIGN IN/ SIGN UP"
    private lateinit var auth: FirebaseAuth

    val providers = arrayListOf(AuthUI.IdpConfig.EmailBuilder().build())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)

        mSignIn = findViewById(R.id.sign_in)
        mSignUp = findViewById(R.id.sign_up)
        mEmail = findViewById(R.id.email)
        mUserName = findViewById(R.id.user_name)
        mPassword = findViewById(R.id.password)
        mPasswordVerify = findViewById(R.id.password_verify)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

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
        mSignIn.setOnClickListener {
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
                auth.signInWithEmailAndPassword(email!!, password!!)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInWithEmail:success")
                                val user = auth.currentUser
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInWithEmail:failure", task.exception)
                                Toast.makeText(baseContext, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show()
                            }

                            // ...
                        }
            }else Log.e(TAG, "Password valid? $password and email valid? $emailValid")
        }

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

