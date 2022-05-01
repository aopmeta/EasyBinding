package com.aopmeta.easybinding.base;

/**
 * 可被观察的包含binding属性的数据模型，viewModel进行实现
 */
public interface PropertiesObservable {
    void addOnPropertyChangedCallback(OnPropertyChangedCallback callback);

    void removeOnPropertyChangedCallback(OnPropertyChangedCallback callback);

    /**
     * 实际的观察者，viewBinding进行实现
     */
    abstract class OnPropertyChangedCallback {
        public abstract void onPropertyChanged(PropertiesObservable sender, Enum<?> propertyId);
    }
}
