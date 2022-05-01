package com.aopmeta.easybinding.base;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseArray;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;

import com.aopmeta.easybinding.OnTwoWayValueChanged;
import com.aopmeta.easybinding.value.TextValueBinder;
import com.aopmeta.easybinding.value.ValueBinder;

import java.util.HashMap;
import java.util.Map;

/**
 * 视图绑定管理类，用于存储已绑定的视图、监听来自VM层的数据变化以及提供视图绑定方法
 */
public class ViewBinding extends PropertiesObservable.OnPropertyChangedCallback {
    private BaseViewModel viewModel;
    private final View rootView;

    private static class BindHolder {
        final View view;
        final ValueBinder valueBinder;

        BindHolder(View view, ValueBinder valueBinder) {
            this.view = view;
            this.valueBinder = valueBinder;
        }
    }

    private final Map<Enum<?>, BindHolder> bindingView = new HashMap<>();
    private final SparseArray<View> cacheViews = new SparseArray<>();

    ViewBinding(View view) {
        this.rootView = view;
    }

    public ViewBinding(FragmentActivity activity, Class<? extends BaseViewModel> viewModelClass) {
        this.rootView = activity.getWindow().getDecorView();
        BaseViewModel viewModel = ViewModelProviders.of(activity).get(viewModelClass);
        viewModel.setActivity(activity);
        setViewModel(viewModel);
    }

    public ViewBinding(Fragment fragment, View view, Class<? extends BaseViewModel> viewModelClass) {
        this.rootView = view;
        BaseViewModel viewModel = ViewModelProviders.of(fragment).get(viewModelClass);
        viewModel.setActivity(fragment.getActivity());
        setViewModel(viewModel);
    }

    /**
     * 设置viewModel并注册监听viewModel的数据变化
     */
    protected void setViewModel(BaseViewModel viewModel) {
        if (this.viewModel != null) {
            this.viewModel.removeOnPropertyChangedCallback(this);
        }
        this.viewModel = viewModel;
        this.viewModel.addOnPropertyChangedCallback(this);

        if (bindingView.size() > 0) {
            this.viewModel.notifyChange();
        }
    }

    public <T extends BaseViewModel> T getViewModel() {
        //noinspection unchecked
        return (T) viewModel;
    }

    /**
     * 在findViewById的基础上加上缓存
     */
    public <T extends View> T findView(int viewId) {
        View view = cacheViews.get(viewId);
        if (view == null) {
            view = rootView.findViewById(viewId);
            cacheViews.put(viewId, view);
        }
        //noinspection unchecked
        return (T) view;
    }

    public View getRootView() {
        return rootView;
    }

    /**
     * 通过id进行绑定
     */
    public void bind(int viewId, ValueBinder<? extends View, ?> valueBinder, Enum<?> type) {
        View view = rootView.findViewById(viewId);
        bind(view, valueBinder, type);
    }

    /**
     * 通过view进行绑定
     */
    public void bind(View view, ValueBinder<? extends View, ?> valueBinder, Enum<?> type) {
        if (view != null) {
            putAndSet(new BindHolder(view, valueBinder), type);
        }
    }

    private void putAndSet(BindHolder bindHolder, Enum<?> type) {
        //dui对绑定关系进行保存
        bindingView.put(type, bindHolder);
        if (viewModel != null) {
            //获取被绑定方法的包装类
            OnBindingCallback<?> onBindingCallback = viewModel.getCallback(type);
            if (onBindingCallback != null) {
                //调用get方法就是调用的被绑定的方法，拿到返回值后交给valueBinder进行赋值
                //noinspection unchecked
                bindHolder.valueBinder.setValue(bindHolder.view, onBindingCallback.get());
            }
        }
    }

    /**
     * 为已绑定的所有view重新赋值
     */
    public void resetAllValues() {
        for (Enum<?> type : bindingView.keySet()) {
            BindHolder bindHolder = bindingView.get(type);
            if (viewModel != null && bindHolder != null && bindHolder.view != null) {
                OnBindingCallback<?> onBindingCallback = viewModel.getCallback(type);
                if (onBindingCallback != null) {
                    //noinspection unchecked
                    bindHolder.valueBinder.setValue(bindHolder.view, onBindingCallback.get());
                }
            }
        }
    }

    /**
     * 通过id进行双向绑定
     */
    public <T> void twoWayBind(int id, Enum<?> type, OnTwoWayValueChanged<CharSequence> onTwoWayValueChanged) {
        View view = rootView.findViewById(id);
        bind(view, new TextValueBinder(), type);

        if (view instanceof TextView) {
            TextView editText = (TextView) view;
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    onTwoWayValueChanged.onChanged(s);
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }
    }

    @Override
    public void onPropertyChanged(PropertiesObservable sender, Enum<?> propertyId) {
        //如果propertyId为BDR.all则更新所有已绑定的view
        if (propertyId == null || propertyId.ordinal() == 0) {
            resetAllValues();
            return;
        }

        if (bindingView.containsKey(propertyId)) {
            //根据枚举key找到绑定的view
            BindHolder bindHolder = bindingView.get(propertyId);
            if (bindHolder != null && bindHolder.view != null) {
                //根据枚举key找到绑定的代理类
                OnBindingCallback<?> onBindingCallback = viewModel.getCallback(propertyId);
                if (onBindingCallback != null) {
                    //调用代理类的get方法获取最新的值后赋值给view
                    //noinspection unchecked
                    bindHolder.valueBinder.setValue(bindHolder.view, onBindingCallback.get());
                }
                return;
            }
            bindingView.remove(propertyId);
        }
    }
}
