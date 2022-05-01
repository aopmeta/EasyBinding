package com.aopmeta.sample.viewmodel;

import android.app.Application;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.aopmeta.easybinding.base.BaseViewModel;
import com.aopmeta.easybinding.processor.Binding;
import com.aopmeta.sample.BDR;

public class MainViewModel extends BaseViewModel {

    private CharSequence inputText;

    public MainViewModel(@NonNull Application application) {
        super(application);
    }

    @Binding
    public CharSequence getInputText() {
        return inputText;
    }

    public void setInputText(CharSequence inputText) {
        if (inputText != null && inputText.length() < 10) {
            this.inputText = inputText;
        } else {
            this.inputText = null;
            notifyPropertyChanged(BDR.inputText);
        }
        notifyPropertyChanged(BDR.computedInputText);
    }

    @Binding
    public CharSequence getComputedInputText() {
        return inputText != null ? (inputText.length() + " " + inputText) : "";
    }

    public void gotoSampleActivity(Class<?> sampleActivityClass) {
        FragmentActivity activity = getActivity();
        if (activity != null) {
            activity.startActivity(new Intent(activity, sampleActivityClass));
        }
    }
}
