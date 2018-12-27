package com.example.cairashields.boan.Services;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;


public class FirebaseInstanceIdService extends com.google.firebase.iid.FirebaseInstanceIdService {

    static String refreshedToken;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        refreshedToken = FirebaseInstanceId.getInstance().getToken();
        //update the firebaseToken on each token refresh call
        if(auth.getCurrentUser() != null) {
            DatabaseReference mDatabaseReference = database.getReference().child("users").child(auth.getUid()).child("firebaseToken");
            mDatabaseReference.setValue(refreshedToken);
        }
        Log.v("*****FirebaseInstanceId", "Refreshed token: " + refreshedToken);
    }


    public static String getRefreshedToken() {
        return refreshedToken;
    }
}
