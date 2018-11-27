package com.example.cairashields.boan

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
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
import android.support.v4.view.ViewPager
import android.util.Log
import com.example.cairashields.boan.adapters.FragmentViewPagerAdapter
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.UserProfileChangeRequest
import com.wajahatkarim3.easyvalidation.core.view_ktx.textEqualTo
import com.wajahatkarim3.easyvalidation.core.view_ktx.validEmail
import com.wajahatkarim3.easyvalidation.core.view_ktx.validator


class MainActivity : AppCompatActivity() {

    @Nullable @BindView(R.id.pager) lateinit var mPager: ViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)

        mPager = findViewById(R.id.pager)
        mPager.adapter = FragmentViewPagerAdapter(supportFragmentManager)
    }


}

