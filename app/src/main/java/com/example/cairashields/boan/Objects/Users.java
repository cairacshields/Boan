package com.example.cairashields.boan.Objects;


import android.support.annotation.Nullable;

import com.stripe.android.model.Token;

public class Users {
    public String username;
    public String email;
    public String token;
    public String firebaseToken;
    public boolean isLender;
    public String profileImage;
    //Customer Id, will get set on the server side
    public String customerId;


    public Users() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Users(String username, String email, String token, boolean isLender, String firebaseToken, @Nullable  String profileImage, String customerId) {
        this.username = username;
        this.email = email;
        this.token = token;
        this.isLender = isLender;
        this.firebaseToken = firebaseToken;
        this.profileImage = profileImage;
        this.customerId = customerId;
    }
}
