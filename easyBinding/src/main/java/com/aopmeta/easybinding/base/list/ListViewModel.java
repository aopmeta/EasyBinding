package com.aopmeta.easybinding.base.list;

import android.app.Application;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aopmeta.easybinding.Resource;
import com.aopmeta.easybinding.ResourceStatus;
import com.aopmeta.easybinding.Status;
import com.aopmeta.easybinding.BDR;
import com.aopmeta.easybinding.base.BaseViewModel;
import com.aopmeta.easybinding.BindingConstants;
import com.aopmeta.easybinding.CustomDividerItemDecoration;
import com.aopmeta.easybinding.R;
import com.aopmeta.easybinding.base.OnBindingLoadMoreListener;
import com.aopmeta.easybinding.base.OnBindingRefreshListener;
import com.aopmeta.easybinding.processor.Binding;

import java.util.List;

/**
 * 列表数据的VM基类，已实现了下拉刷新、加载更多、分割线设置和加载状态更新等等
 */
public class ListViewModel extends BaseViewModel implements BindingConstants.OnLoadMoreListener {
    //列表数据
    protected LiveData<List<?>> liveList;
    //当前状态
    protected LiveData<ResourceStatus> liveResourceStatus;
    //更新数据的方式
    private ListData.ListChangedAction listChangedAction;
    //精准滚动的位置
    private int scrollPosition = -1;

    protected OnItemClickListener itemClickListener;
    private OnBindingRefreshListener onRefreshListener;
    private OnBindingLoadMoreListener onLoadMoreListener;
    private RecyclerView.LayoutManager layoutManager;
    //下拉刷新的状态
    private int refreshStatus;

    //列表通用的resourceId，当BaseViewMode的getRecyclerViewItemResId返回值不为空时
    //优先使用getRecyclerViewItemResId的返回值
    private int resourceId;
    //分割线资源id
    private int dividerResId;
    //底部加载更多用的viewModel
    private FootViewModel footViewModel;
    //分割线的类型，支持固定的几种和定制类型
    private DividerType dividerType = DividerType.DEFAULT;
    private RecyclerView.ItemDecoration itemDecoration;
    //当数据为空时是否隐藏“暂无数据”视图的展示
    private boolean hideEmptyViewWhenSizeNull;

    private String emptyListText;
    private final String loadingText;
    private final String refreshText;
    private int emptyImageResId = R.drawable.img_empty_list;

    private OnBindAdapterCallback bindAdapterCallback;
    private Drawable background;

    public enum DividerType {
        DEFAULT, WITHOUT_FOOTER, WITHOUT_HEADER_FOOTER, CUSTOM
    }

    public ListViewModel(@NonNull Application application) {
        super(application);
        dividerResId = R.drawable.divider_list_2px;
        background = new ColorDrawable(ContextCompat.getColor(application, R.color.colorWhite));
        emptyListText = application.getString(R.string.msg_no_data);
        loadingText = application.getString(R.string.msg_loading);
        refreshText = application.getString(R.string.msg_append_click_to_refresh);
    }

    public void setHideEmptyViewWhenSizeNull(boolean hideEmptyViewWhenSizeNull) {
        this.hideEmptyViewWhenSizeNull = hideEmptyViewWhenSizeNull;
    }

    OnBindAdapterCallback getBindAdapterCallback() {
        return bindAdapterCallback;
    }

    public RecyclerView.LayoutManager getLayoutManager() {
        return layoutManager;
    }

    public void setLayoutManager(RecyclerView.LayoutManager layoutManager) {
        this.layoutManager = layoutManager;
    }

    public void setEmptyListText(String emptyListText) {
        this.emptyListText = emptyListText;
    }

    public void setEmptyImageResId(int emptyImageResId) {
        this.emptyImageResId = emptyImageResId;
    }

    /**
     * 设置自带状态的数据
     */
    public void setResourceIdAndResource(OnBindAdapterCallback bindAdapterCallback, int resourceId, LiveData<?> liveList) {
        //noinspection unchecked
        LiveData<Resource<List<?>>> resourceLiveData = (LiveData<Resource<List<?>>>) liveList;
        MediatorLiveData<List<?>> listLive = new MediatorLiveData<>();
        MediatorLiveData<ResourceStatus> statusLive = new MediatorLiveData<>();
        listLive.addSource(resourceLiveData, listResource -> {
            if (listResource != null) {
                listLive.setValue(listResource.getData());
                statusLive.setValue(ResourceStatus.parse(listResource));
            }
        });

        setResourceIdAndList(bindAdapterCallback, resourceId, listLive, statusLive);
    }

    /**
     * 单独设置数据和状态
     */
    public void setResourceIdAndList(OnBindAdapterCallback bindAdapterCallback, int resourceId, LiveData<?> liveList, LiveData<ResourceStatus> liveResourceStatus) {
        this.bindAdapterCallback = bindAdapterCallback;
        this.resourceId = resourceId;

        //noinspection unchecked
        this.liveList = (LiveData<List<?>>) liveList;

        if (liveResourceStatus == null) {
            MediatorLiveData<ResourceStatus> liveStatus = new MediatorLiveData<>();
            liveStatus.setValue(ResourceStatus.loading());
            liveStatus.addSource(this.liveList, objects -> {
                if (objects != null) {
                    liveStatus.removeSource(ListViewModel.this.liveList);
                    liveStatus.setValue(ResourceStatus.finish());
                }
            });
            this.liveResourceStatus = liveStatus;
        } else {
            this.liveResourceStatus = liveResourceStatus;
        }

        putNotifyLiveData(this.liveResourceStatus, BDR.emptyText, BDR.emptyViewVisible, BDR.emptyImageResId);
        putNotifyLiveData(this.liveList, BDR.liveData, BDR.emptyText, BDR.emptyViewVisible, BDR.emptyImageResId);
    }

    public void refreshList() {
        notifyPropertyChanged(BDR.liveData);
    }

    public <T> T getItemAt(int position) {
        List<?> list = liveList.getValue();
        if (list != null && list.size() > position) {
            //noinspection unchecked
            return (T) list.get(position);
        }
        return null;
    }

    /**
     * 需要以特殊的方式更新数据时，先调用该方法再为status或data赋值，否则无效
     */
    public void setListChangedAction(ListData.ListChangedAction listChangedAction) {
        this.listChangedAction = listChangedAction;
    }

    public Drawable getBackground() {
        return background;
    }

    public void setBackground(Drawable background) {
        this.background = background;
    }

    public void setDividerType(DividerType dividerType) {
        setDividerType(dividerType, null);
    }

    public void setDividerType(DividerType dividerType, RecyclerView.ItemDecoration itemDecoration) {
        this.dividerType = dividerType;
        this.itemDecoration = itemDecoration;
    }

    public void setLoadMoreEnable() {
        this.footViewModel = new FootViewModel(getApplication());
        if (dividerType == DividerType.DEFAULT) {
            setDividerType(DividerType.WITHOUT_FOOTER);
        }
    }

    public void setOnLoadMoreListener(OnBindingLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    public FootViewModel getFootViewModel() {
        return footViewModel;
    }

    public void setOnRefreshListener(OnBindingRefreshListener onRefreshListener) {
        this.onRefreshListener = onRefreshListener;
    }

    public void setDividerResId(int dividerResId) {
        this.dividerResId = dividerResId;
    }

    public Drawable getDivider() {
        return ContextCompat.getDrawable(getApplication(), dividerResId);
    }

    public RecyclerView.ItemDecoration getDecoration() {
        Application application = getApplication();
        RecyclerView.ItemDecoration decoration;
        if (dividerType == DividerType.WITHOUT_FOOTER) {
            CustomDividerItemDecoration dividerItemDecoration = new CustomDividerItemDecoration(application, DividerItemDecoration.VERTICAL, 0);
            dividerItemDecoration.setDrawable(getDivider());
            decoration = dividerItemDecoration;
        } else if (dividerType == DividerType.WITHOUT_HEADER_FOOTER) {
            CustomDividerItemDecoration dividerItemDecoration = new CustomDividerItemDecoration(application, DividerItemDecoration.VERTICAL, 1);
            dividerItemDecoration.setDrawable(getDivider());
            decoration = dividerItemDecoration;
        } else if (dividerType == DividerType.CUSTOM) {
            decoration = itemDecoration;
        } else {
            if (layoutManager instanceof GridLayoutManager) {
                return null;
            }
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(application, DividerItemDecoration.VERTICAL);
            dividerItemDecoration.setDrawable(getDivider());
            decoration = dividerItemDecoration;
        }
        return decoration;
    }

    public void setScrollPosition(int scrollPosition) {
        this.scrollPosition = scrollPosition;
        notifyPropertyChanged(BDR.scrollPosition);
    }

    @Binding
    public String getEmptyText() {
        ResourceStatus resourceStatus = liveResourceStatus.getValue();
        if (resourceStatus != null) {
            List<?> list = this.liveList.getValue();
            if (list == null || list.size() <= 0) {
                if (resourceStatus.status == Status.LOADING) {
                    return loadingText;
                } else if (resourceStatus.status == Status.ERROR) {
                    if (onRefreshListener != null) {
                        return resourceStatus.message + refreshText;
                    } else {
                        return resourceStatus.message;
                    }
                } else if (!hideEmptyViewWhenSizeNull) {
                    if (onRefreshListener != null) {
                        return emptyListText + refreshText;
                    } else {
                        return emptyListText;
                    }
                } else {
                    return null;
                }
            } else {
                if (footViewModel != null) {
                    footViewModel.setStatus(resourceStatus.status, list.size() < BindingConstants.PAGE_SIZE);
                }
            }
        }
        return null;
    }

    @Binding
    public int getEmptyViewVisible() {
        boolean visible;
        if (hideEmptyViewWhenSizeNull) {
            visible = false;
        } else {
            if (this.liveList.getValue() != null && this.liveList.getValue().size() > 0) {
                visible = false;
            } else {
                visible = true;
            }
        }
        return visible ? View.VISIBLE : View.GONE;
    }

    @Binding
    public Drawable getEmptyImageResId() {
        ResourceStatus resourceStatus = liveResourceStatus.getValue();
        if (resourceStatus != null) {
            List<?> list = this.liveList.getValue();
            if (list == null || list.size() <= 0) {
                if (resourceStatus.isOk() && !hideEmptyViewWhenSizeNull) {
                    return ContextCompat.getDrawable(getActivity(), emptyImageResId);
                }
            }
        }
        return null;
    }

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Binding
    public int getScrollPosition() {
        return scrollPosition;
    }

    @Binding
    public ListData getLiveData() {
        List<?> list = liveList.getValue();
        ListData liveDataHolder = new ListData(bindAdapterCallback, this.resourceId, list, footViewModel, itemClickListener, listChangedAction);
        listChangedAction = null;
        return liveDataHolder;
    }

    public void emptyTextClicked() {
        ResourceStatus resource = liveResourceStatus.getValue();
        if (resource != null && resource.isEnd()) {
            if (onRefreshListener != null) {
                onRefreshListener.onRefresh();
            }
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        this.itemClickListener = null;
    }

    public OnBindingRefreshListener getOnRefreshListener() {
        return onRefreshListener;
    }

    @Override
    public void onLoadMore() {
        ResourceStatus resource = liveResourceStatus.getValue();
        if (resource != null) {
            if (resource.status != Status.FINISH) {
                List<?> list = liveList.getValue();
                int size;
                if (list != null && (size = list.size()) > 0) {
                    onLoadMore(list.get(size - 1));
                    int pageSize = size / BindingConstants.PAGE_SIZE;
                    onLoadNextPage(size % BindingConstants.PAGE_SIZE == 0 ? pageSize + 1 : pageSize + 2);
                }
            }
        }
    }

    protected void onLoadMore(Object lastItem) {
        if (onLoadMoreListener != null) {
            onLoadMoreListener.onLoadMore(lastItem);
        }
    }

    protected void onLoadNextPage(int nextPageIndex) {
    }

    @Binding
    public int getRefreshStatus() {
        return refreshStatus;
    }

    public void setRefreshStatus(int refreshStatus) {
        this.refreshStatus = refreshStatus;
        notifyPropertyChanged(BDR.refreshStatus);
    }
}
