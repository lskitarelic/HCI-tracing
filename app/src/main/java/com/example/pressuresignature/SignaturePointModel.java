package com.example.pressuresignature;


public class SignaturePointModel {
    float x;
    float y;


    SignaturePointModel(float x, float y) {
        this.x = x;
        this.y = y;

    }

    @Override
    public String toString() {
        return "SignaturePointModel{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
