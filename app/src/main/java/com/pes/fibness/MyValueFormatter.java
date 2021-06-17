package com.pes.fibness;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

class MyValueFormatter implements IAxisValueFormatter {
    private String[] mValues;

    public MyValueFormatter(String[] values) {
        mValues = values;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        int val = (int) (value);
        System.out.println("my value is: " + val);
        String label = "";
        if (val >= 0 && val <= (mValues.length-1)*2) {
            label = mValues[val/2];
        } else {
            label = "";
        }
        return label;
    }

}
