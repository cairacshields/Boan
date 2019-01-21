package com.example.cairashields.boan.stripe

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ImageButton
import butterknife.BindView
import com.example.cairashields.boan.R
import android.content.Intent
import com.google.firebase.auth.FirebaseAuth


/*
    This is used to create a Connect Express Account for all users

    The redirect URI should be http://www.example.com/boanConnectExpress
    Using that URI we will be deep linked into the credit card activity
 */

class ConnectExpress : AppCompatActivity(){
    @BindView(R.id.connect_stripe) lateinit var mConnectStripe: ImageButton

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_boan_connect_express)

        mConnectStripe = findViewById(R.id.connect_stripe)

        auth = FirebaseAuth.getInstance()

        mConnectStripe.setOnClickListener {
           val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(this.resources.getString(R.string.stripe_connect_url, 1 ,auth.currentUser!!.email )))
           startActivity(browserIntent)
       }
    }

}