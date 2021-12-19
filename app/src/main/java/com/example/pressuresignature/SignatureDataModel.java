package com.example.pressuresignature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SignatureDataModel {
    List<SignaturePointModel> dataPoints;

    SignatureDataModel() {
        dataPoints = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "SignatureDataModel{" +
                "dataPoints=" + dataPoints +
                '}';
    }
}
