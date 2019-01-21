package com.example.cairashields.boan.Objects;

import android.support.annotation.Nullable;

import java.util.Date;

public class TermAgreement {
    //Currently using the Lenders uid as the TermAgreement Id
    public String id;
    public int borrowAmount;
    public int repayAmount;
    public Date repayDate;
    public String borrowerUserId;
    public String lenderUserId;
    public String username;
    public String email;
    public String comment;
    public boolean accepted;
    public boolean borrowerRepayed;

    public TermAgreement(){

    }

    public TermAgreement(String id,int borrowAmount, int repayAmount, Date repayDate, String borrowerUserId, String lenderUserId, String username,
                         String email, String comment, boolean accepted, boolean borrowerRepayed){
        this.id = id;
        this.borrowAmount = borrowAmount;
        this.repayAmount = repayAmount;
        this.repayDate = repayDate;
        this.borrowerUserId = borrowerUserId;
        this.lenderUserId = lenderUserId;
        this.username =username;
        this.email = email;
        this.comment = comment;
        this.accepted = accepted;
        this.borrowerRepayed = borrowerRepayed;
    }
}
