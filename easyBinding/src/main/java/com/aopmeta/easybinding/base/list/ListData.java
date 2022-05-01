package com.aopmeta.easybinding.base.list;

import java.util.List;

/**
 * 更新列表数据时需要用到的一些对象
 */
public class ListData {
    public enum ListChangedType {
        INSERT, REMOVE, CHANGE
    }
    public static class ListChangedAction {
        public final ListChangedType type;
        public final int positionStart;
        public final int itemCount;

        public ListChangedAction(ListChangedType type, int positionStart, int itemCount) {
            this.type = type;
            this.positionStart = positionStart;
            this.itemCount = itemCount;
        }
    }

    public final List<?> listResource;
    public final OnBindAdapterCallback bindAdapterCallback;
    public final int bindResourceId;
    public final OnItemClickListener listener;
    public final FootViewModel footViewModel;
    public final ListChangedAction listChangedAction;

    public ListData(OnBindAdapterCallback bindAdapterCallback, int bindResourceId, List<?> listResource, FootViewModel footViewModel, OnItemClickListener listener, ListChangedAction listChangedAction) {
        this.bindAdapterCallback = bindAdapterCallback;
        this.bindResourceId = bindResourceId;
        this.listResource = listResource;
        this.footViewModel = footViewModel;
        this.listener = listener;
        this.listChangedAction = listChangedAction;
    }
}
