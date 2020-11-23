package com.hh.contractiontimer.common;

import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.databinding.BindingAdapter;
import androidx.lifecycle.ViewModel;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hh.contractiontimer.ui.measure.MeasurementViewModel;

public class CustomBinding {

    @BindingAdapter("app:gone")
    public static void setGone1(LinearLayout view, Boolean gone) {
        if (gone) view.setVisibility(View.GONE);
        else view.setVisibility(View.VISIBLE);
    }

    @BindingAdapter("app:gone")
    public static void setGone(Button view, Boolean gone) {
        if (gone) view.setVisibility(View.GONE);
        else view.setVisibility(View.VISIBLE);
    }

    @BindingAdapter("app:gone")
    public static void setGoneFloatButton(FloatingActionButton view, Boolean gone) {
        if (gone) view.setVisibility(View.GONE);
        else view.setVisibility(View.VISIBLE);
    }

    @BindingAdapter("app:text")
    public static void setText(TextView view, String text) {
        view.setText(text);
    }

}
