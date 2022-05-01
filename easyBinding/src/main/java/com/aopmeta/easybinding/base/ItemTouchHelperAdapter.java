package com.aopmeta.easybinding.base;

public interface ItemTouchHelperAdapter {

    boolean onItemMove(int fromPosition, int toPosition);
    void onItemDismiss(int position);
    boolean isItemViewSwipeEnabled(int position);
    boolean isLastItemDisable();
}
