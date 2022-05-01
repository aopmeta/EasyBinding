package com.aopmeta.sample;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.aopmeta.easybinding.base.list.ListViewBinding;
import com.aopmeta.sample.viewmodel.SampleListViewModel;
import com.aopmeta.easybinding.R;

public class SampleListActivity extends AppCompatActivity {
    private ListViewBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_list_binding_swipe);

        setTitle("列表");

        binding = new ListViewBinding(this, SampleListViewModel.class);
    }
}