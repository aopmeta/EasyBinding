package com.aopmeta.sample.viewmodel;

import android.app.Application;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.aopmeta.easybinding.BindingConstants;
import com.aopmeta.easybinding.Resource;
import com.aopmeta.easybinding.base.OnBindingLoadMoreListener;
import com.aopmeta.easybinding.base.OnBindingRefreshListener;
import com.aopmeta.easybinding.base.ViewBinding;
import com.aopmeta.easybinding.base.list.ListViewModel;
import com.aopmeta.easybinding.base.list.OnBindAdapterCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SampleListViewModel extends ListViewModel implements OnBindingRefreshListener, OnBindingLoadMoreListener {
    private final ExecutorService executorService = Executors.newFixedThreadPool(1);
    private final MutableLiveData<Resource<List<String>>> listLiveData = new MutableLiveData<>();

    public SampleListViewModel(@NonNull Application application) {
        super(application);

        setResourceIdAndResource(
                new OnBindAdapterCallback() {
                    @Override
                    public void bind(ViewBinding binding, ViewGroup root, int viewType) {
                    }

                    @Override
                    public void setValue(ViewBinding binding, Object value) {
                        TextView groupNameView = binding.findView(android.R.id.text1);
                        groupNameView.setText((String) value);
                    }
                }, android.R.layout.simple_list_item_1,
                listLiveData);
        setLoadMoreEnable();
        setOnRefreshListener(this);
        setOnLoadMoreListener(this);

        onRefresh();
    }

    @Override
    public void onRefresh() {
        if (setLoadingStatus()) {
            executorService.submit(() -> {
                List<String> dataList = new ArrayList<>();
                for (int i = 0; i < BindingConstants.PAGE_SIZE; i++) {
                    dataList.add("列表数据" + (i + 1));
                }

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                listLiveData.postValue(Resource.success(dataList));
                setRefreshStatus(-1);
            });
        }
    }

    @Override
    public void onLoadMore(Object lastItem) {
        if (setLoadingStatus()) {
            executorService.submit(() -> {
                Resource<List<String>> resource = listLiveData.getValue();
                if (resource == null || resource.getData() == null) {
                    return;
                }
                for (int i = 0; i < 10; i++) {
                    resource.getData().add("其他" + (resource.getData().size() + 1));
                }

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (resource.getData().size() > 30) {
                    listLiveData.postValue(Resource.finish(resource.getData()));
                } else {
                    listLiveData.postValue(Resource.success(resource.getData()));
                }
            });
        }
    }

    private boolean setLoadingStatus() {
        Resource<List<String>> dataResource = listLiveData.getValue();
        if (dataResource != null) {
            if (!dataResource.isLoading()) {
                listLiveData.setValue(Resource.loading(dataResource.getData()));
            } else {
                return false;
            }
        } else {
            listLiveData.setValue(Resource.loading(null));
        }
        return true;
    }
}
