package com.example.cairashields.boan.Helper;

import java.util.Random;

public class MathHelper {

    public static int generateRandomInt(int upperRange){
        Random random = new Random();
        return random.nextInt(upperRange);
    }
}
