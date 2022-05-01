package com.aopmeta.easybinding.base;

import java.util.HashMap;
import java.util.Map;

/**
 * 存储可被绑定的所有方法，即加了@Binding的方法
 * 在编译阶段apt将生成与viewModel同包，名称为${viewModel名}+$BindingCallbacks的类
 * 如ListViewModel将生成
 *   public class ListViewModel$BindingCallbacks extends BindingCallbacks {
 *     public ListViewModel$BindingCallbacks(ListViewModel model) {
 *       callbacks.put(BDR.emptyText, new OnBindingCallback(){
 *         public String get(){
 *           return model.getEmptyText();
 *         }
 *       });
 *       ...
 *     }
 *   }
 */
public class BindingCallbacks {
    protected Map<Enum<?>, OnBindingCallback<?>> callbacks = new HashMap<>();

    /**
     * 获取实际方法的OnBindingCallback代理对象
     */
    public OnBindingCallback<?> getCallback(Enum<?> type) {
        return callbacks.get(type);
    }
}
