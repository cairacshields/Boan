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
    public String giftCategory;
    public String address;
    public String stripe_user_id;
    public boolean hasActiveBorrowRequest;
    public String summary;
    public String facebookLink;
    public String instagramLink;

    public Users() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Users(String username, String email, String token, boolean isLender, String firebaseToken, @Nullable  String profileImage, String customerId, String giftCategory,
                 String address, String stripe_user_id, boolean hasActiveBorrowRequest, String summary, String facebookLink, String instagramLink) {
        this.username = username;
        this.email = email;
        this.token = token;
        this.isLender = isLender;
        this.firebaseToken = firebaseToken;
        this.profileImage = profileImage;
        this.customerId = customerId;
        this.giftCategory = giftCategory;
        this.address = address;
        this.stripe_user_id = stripe_user_id;
        this.hasActiveBorrowRequest = hasActiveBorrowRequest;
        this.summary = summary;
        this.facebookLink = facebookLink;
        this.instagramLink = instagramLink;
    }
}
