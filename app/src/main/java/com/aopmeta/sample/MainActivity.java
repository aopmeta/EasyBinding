package com.aopmeta.sample;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.aopmeta.easybinding.base.ViewBinding;
import com.aopmeta.easybinding.value.TextValueBinder;
import com.aopmeta.sample.BDR;
import com.aopmeta.sample.viewmodel.MainViewModel;

public class MainActivity extends AppCompatActivity {
    private MainViewModel mainViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewBinding viewBinding = new ViewBinding(this, MainViewModel.class);
        mainViewModel = viewBinding.getViewModel();
        viewBinding.twoWayBind(R.id.et_sample_two_way_bind, BDR.inputText, charSequence ->
                mainViewModel.setInputText(charSequence)
        );
        viewBinding.bind(R.id.tv_sample_two_way_bind, new TextValueBinder(), BDR.computedInputText);

        viewBinding.findView(R.id.btn_goto_tree).setOnClickListener(v ->
                mainViewModel.gotoSampleActivity(TreeActivity.class)
        );
        viewBinding.findView(R.id.btn_goto_list).setOnClickListener(v ->
                mainViewModel.gotoSampleActivity(SampleListActivity.class)
        );

    }
}