package com.example.cairashields.boan.Objects;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.Date;
public class BorrowRequest implements Serializable {
    public String userId;
    public String username;
    public String email;
    public int borrowAmount;
    public Date repayDate;
    public String borrowReason;
    public boolean requestClosed;

    public BorrowRequest() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public BorrowRequest(String userId, String username, String email, int borrowAmount, Date repayDate, String borrowReason,
                         boolean requestClosed) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.borrowAmount = borrowAmount;
        this.repayDate = repayDate;
        this.borrowReason = borrowReason;
        this.requestClosed = requestClosed;
    }
}
