package com.example.cairashields.boan.Objects;


import com.stripe.android.model.Token;

public class Users {
    public String username;
    public String email;
    public String token;

    public Users() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Users(String username, String email, String token) {
        this.username = username;
        this.email = email;
    }
}
