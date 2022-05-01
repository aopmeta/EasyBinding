package com.aopmeta.easybinding.base;

import android.app.Activity;
import android.app.Application;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * viewModel基类，用于存储了所有可绑定方法的包装类对象、观察数据变化的对象等等
 * 要实现数据绑定必须要继承该类
 */
public class BaseViewModel extends AndroidViewModel implements PropertiesObservable {
    private final BindingCallbacks bindingCallbacks;
    //存储liveData与binding属性的关系，liveData更新时响应的绑定view自动更新
    private final Map<LiveData<?>, Enum<?>[]> liveDataMap = new HashMap<>();
    //子viewModel
    private final Map<String, BaseViewModel> childrenMap = new HashMap<>();
    //观察数据变化的所有对象，用弱引用进行包装防止内存泄漏
    private final Set<WeakReference<OnPropertyChangedCallback>> propertyChangedCallbacks = new HashSet<>();

    private WeakReference<? extends Activity> activityRef;

    /**
     * 当返回值不为-1时，该viewModel将不使用listViewModel中设置的通用resourceId，改为使用自己定制的resourceId
     * 该resourceId将作为一个单独的viewType让recyclerView进行缓存
     */
    public int getRecyclerViewItemResId() {
        return -1;
    }

    public BaseViewModel(@NonNull Application application) {
        super(application);

        bindingCallbacks = new BindingCallbacks();
        Class<?> thisClass = getClass();
        //查找当前对象到最底层BaseViewModel之间所有的枚举key和代理对象
        while (thisClass != null && thisClass != BaseViewModel.class) {
            BindingCallbacks callbacks = getBindingCallbacks(thisClass);
            if (callbacks != null) {
                bindingCallbacks.callbacks.putAll(callbacks.callbacks);
            }
            thisClass = thisClass.getSuperclass();
        }
    }

    /**
     * 通过反射创建viewModel对应BindingCallbacks类
     */
    private BindingCallbacks getBindingCallbacks(Class<?> clazz) {
        String classNameWithPkg = clazz.getName() + "$BindingCallbacks";
        try {
            Class<?> callbacksClass = Class.forName(classNameWithPkg);
            return (BindingCallbacks) callbacksClass.getConstructor(clazz).newInstance(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void addChildViewModel(BaseViewModel baseViewModel, String tag) {
        //用tag对相同的viewModel类进行区分
        childrenMap.put(String.format("%s::%s", baseViewModel.getClass().getName(), tag), baseViewModel);
    }

    public <T extends BaseViewModel> T getChildViewModel(Class<T> viewModelClass, String tag) {
        //noinspection unchecked
        return (T) childrenMap.get(String.format("%s::%s", viewModelClass.getName(), tag));
    }

    public <T extends Activity> T getActivity() {
        if (activityRef != null) {
            Activity activity = activityRef.get();
            if (activity != null) {
                //noinspection unchecked
                return (T) activity;
            }
        }
        return null;
    }

    public void setActivity(FragmentActivity activity) {
        if (activityRef != null) {
            activityRef.clear();
        }
        if (activity != null) {
            this.activityRef = new WeakReference<>(activity);
            for (BaseViewModel baseViewModel : childrenMap.values()) {
                baseViewModel.setActivity(activity);
            }

            synchronized (liveDataMap) {
                for (LiveData<?> liveData : liveDataMap.keySet()) {
                    Enum<?>[] fieldIds = liveDataMap.get(liveData);
                    if (fieldIds != null) {
                        //监听liveData以便及时通知到view层
                        liveData.observe(activity, o -> {
                            for (Enum<?> fieldId : fieldIds) {
                                notifyPropertyChanged(fieldId);
                            }
                        });
                    }
                }
                liveDataMap.clear();
            }
        }
    }

    /**
     * 将liveData与binding属性的关系进行绑定，liveData数据更新则与binding属性绑定的view更新
     */
    public void putNotifyLiveData(@NonNull LiveData<?> notifyLiveData, Enum<?>... fieldIds) {
        FragmentActivity activity = getActivity();
        //根据不同的生命周期分别处理，activity不为null时直接监听，为null时加入缓存待setActivity调用后再监听
        if (activity != null) {
            notifyLiveData.observe(activity, o -> {
                for (Enum<?> fieldId : fieldIds) {
                    notifyPropertyChanged(fieldId);
                }
            });
        } else {
            synchronized (liveDataMap) {
                liveDataMap.put(notifyLiveData, fieldIds);
            }
        }
    }

    OnBindingCallback<?> getCallback(Enum<?> propertyId) {
        OnBindingCallback<?> callback = bindingCallbacks.getCallback(propertyId);
        if (callback == null) {
            for (BaseViewModel childViewModel : childrenMap.values()) {
                callback = childViewModel.bindingCallbacks.getCallback(propertyId);
                if (callback != null) {
                    break;
                }
            }
        }
        return callback;
    }

    @Override
    public void addOnPropertyChangedCallback(OnPropertyChangedCallback callback) {
        propertyChangedCallbacks.add(new WeakReference<>(callback));

        for (BaseViewModel childViewModel : childrenMap.values()) {
            childViewModel.addOnPropertyChangedCallback(callback);
        }
    }

    @Override
    public void removeOnPropertyChangedCallback(OnPropertyChangedCallback callback) {
        for (WeakReference<OnPropertyChangedCallback> weakReference : propertyChangedCallbacks) {
            OnPropertyChangedCallback changedCallback = weakReference.get();
            if (callback == changedCallback) {
                propertyChangedCallbacks.remove(weakReference);
                break;
            }
        }

        for (BaseViewModel childViewModel : childrenMap.values()) {
            childViewModel.removeOnPropertyChangedCallback(callback);
        }
    }

    public void notifyChange() {
        notifyPropertyChanged(null);
    }

    public void notifyPropertyChanged(Enum<?> fieldId) {
        List<WeakReference<OnPropertyChangedCallback>> clearedList = null;
        for (WeakReference<OnPropertyChangedCallback> weakReference : propertyChangedCallbacks) {
            //更新时顺便清理一下已被回收的观察者
            OnPropertyChangedCallback changedCallback = weakReference.get();
            if (changedCallback != null) {
                changedCallback.onPropertyChanged(this, fieldId);
            } else {
                if (clearedList == null) {
                    clearedList = new ArrayList<>();
                }
                clearedList.add(weakReference);
            }
        }
        if (clearedList != null) {
            propertyChangedCallbacks.removeAll(clearedList);
        }
    }

    @Override
    protected void onCleared() {
        propertyChangedCallbacks.clear();
        super.onCleared();
    }
}
