package com.aopmeta.sample.viewmodel;

import android.app.Application;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import com.aopmeta.sample.model.UserProfile;
import com.aopmeta.easybinding.base.BaseViewModel;
import com.aopmeta.sample.BDR;
import com.aopmeta.sample.R;
import com.aopmeta.sample.model.GroupProfile;
import com.aopmeta.easybinding.processor.Binding;

import java.util.List;


public class TreeItemViewModel extends BaseViewModel {
    public final GroupProfile groupProfile;
    public final UserProfile userProfile;
    public final int level;

    private boolean mExpand;
    private boolean mChildLoad;
    private boolean mLoading;
    private List<TreeItemViewModel> mChildItemList;

    @Override
    public int getRecyclerViewItemResId() {
        return isGroup() ? R.layout.list_item_tree_group : R.layout.list_item_tree_user;
    }

    public TreeItemViewModel(Application application, int level, GroupProfile groupProfile) {
        super(application);
        this.groupProfile = groupProfile;
        this.userProfile = null;
        this.level = level;
    }

    public TreeItemViewModel(Application application, int level, UserProfile userProfile) {
        super(application);
        this.groupProfile = null;
        this.userProfile = userProfile;
        this.level = level;
    }

    public boolean isGroup() {
        return this.groupProfile != null;
    }

    public void setChildItemList(List<TreeItemViewModel> childItemList) {
        mChildItemList = childItemList;
    }

    public List<TreeItemViewModel> getChildItemList() {
        return mChildItemList;
    }

    public int getPaddingLeft() {
        return getApplication().getResources().getDimensionPixelOffset(R.dimen.offset) * level;
    }

    public void setExpand(boolean expand) {
        mExpand = expand;
        notifyPropertyChanged(BDR.expandDrawable);
    }

    /**
     * 收集所有已经展开的节点
     */
    public void shrink(List<TreeItemViewModel> collectList) {
        mExpand = false;
        if (mChildItemList != null) {
            for (TreeItemViewModel item : mChildItemList) {
                collectList.add(item);
                if (item.isExpand()) {
                    item.shrink(collectList);
                }
            }
        }
    }

    public void setLoading(boolean loading) {
        mLoading = loading;
        notifyPropertyChanged(BDR.loading);
    }

    @Binding
    public boolean isLoading() {
        return mLoading;
    }

    public boolean isExpand() {
        return mExpand;
    }

    @Binding
    public Drawable getExpandDrawable() {
        return ContextCompat.getDrawable(getApplication(), mExpand ? R.drawable.ic_arrow_down_gray : R.drawable.ic_arrow_right_gray);
    }

    public void setChildLoad(boolean childLoad) {
        mChildLoad = childLoad;
    }

    public boolean isChildLoad() {
        return mChildLoad;
    }
}
