package com.aopmeta.easybinding.base.list;

import android.app.Application;
import android.view.View;

import androidx.annotation.NonNull;

import com.aopmeta.easybinding.Status;
import com.aopmeta.easybinding.base.BaseViewModel;
import com.aopmeta.easybinding.processor.Binding;

/**
 * 加载更多时展示状态为加载中的viewModel类
 */
public class FootViewModel extends BaseViewModel {
    private Status status = Status.PREPARE;
    private boolean lessThanOnePage;

    public FootViewModel(@NonNull Application application) {
        super(application);
    }

    public void setStatus(Status status, boolean lessThanOnePage) {
        this.status = status;
        this.lessThanOnePage = lessThanOnePage;
        notifyChange();
    }

    @Binding
    public int getProgressVisible() {
        if (status == Status.FINISH || lessThanOnePage) {
            return View.GONE;
        } else {
            return View.VISIBLE;
        }
    }
}
