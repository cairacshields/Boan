package com.example.cairashields.boan.Objects;

import java.util.Date;

public class BorrowRequest {
    public String username;
    public String email;
    public String collateralPic;
    public String collateralDescription;
    public String borrowAmount;
    public Date repayDate;
    public String borrowReason;

    public BorrowRequest() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public BorrowRequest(String username, String email, String collateralPic, String collateralDescription, String borrowAmount, Date repayDate, String borrowReason) {
        this.username = username;
        this.email = email;
        this.collateralPic = collateralPic;
        this.collateralDescription = collateralDescription;
        this.borrowAmount = borrowAmount;
        this.repayDate = repayDate;
        this.borrowReason = borrowReason;
    }
}
