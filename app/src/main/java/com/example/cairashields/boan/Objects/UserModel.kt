package com.example.cairashields.boan.Objects

import com.google.firebase.database.*

object UserModel {

    var database = FirebaseDatabase.getInstance()
    var mDatabaseReferenceUsers = database.reference.child("users")


    fun getUser(userId: String): Users{
        var user : Users? = null
        mDatabaseReferenceUsers.child(userId).addValueEventListener(object : ValueEventListener {
           override fun onCancelled(p0: DatabaseError) {
           }

           override fun onDataChange(p0: DataSnapshot) {
                user = p0.getValue<Users>(Users::class.java)!!

           }
       })
      return user!!
    }
}