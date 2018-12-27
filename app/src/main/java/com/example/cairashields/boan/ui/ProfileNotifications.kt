package com.example.cairashields.boan.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.example.cairashields.boan.R

/*
    This class will display any notifications regarding borrow requests (for borrowers) or terms agreements (for lenders)
 */

class ProfileNotifications: AppCompatActivity() {
    @BindView(R.id.content)lateinit var mContent: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycler)
        ButterKnife.bind(this)

        mContent = findViewById(R.id.content)

        val title = intent.extras.getString(EXTRA_TITLE)
        val icon = intent.extras.getString(EXTRA_ICON)

        setTitle(title)

    }

    companion object {
        const val EXTRA_TITLE = "title"
        const val EXTRA_ICON = "icon"
    }
}