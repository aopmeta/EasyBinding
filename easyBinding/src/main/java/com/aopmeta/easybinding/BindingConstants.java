package com.aopmeta.easybinding;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aopmeta.easybinding.base.BindAdapter;
import com.aopmeta.easybinding.base.OnBindingRefreshListener;
import com.aopmeta.easybinding.base.list.ListData;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;

import java.util.List;

/**
 * 该类作用类似于@BindingAdapter，将一些通用方法提取到一个类中
 */
public class BindingConstants {
    public static final int PAGE_SIZE = 20;

    public interface OnLoadMoreListener {
        void onLoadMore();
    }

    /**
     * 添加自定义分割线
     */
    public static void setDecoration(RecyclerView recyclerView, RecyclerView.LayoutManager layoutManager, RecyclerView.ItemDecoration decoration) {
        Context context = recyclerView.getContext();
        if (layoutManager == null) {
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            layoutManager = linearLayoutManager;
        }
        if (recyclerView.getLayoutManager() == null) {
            recyclerView.setLayoutManager(layoutManager);

            if (decoration != null) {
                recyclerView.addItemDecoration(decoration);
            }
        }
    }

    /**
     * 为RecyclerView设置adapter或更新其现有数据
     */
    public static void bindRecyclerView(RecyclerView recyclerView, ListData dataHolder) {
        if (dataHolder == null) return;

        List<?> list = dataHolder.listResource;
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        if (adapter == null) {
            BindAdapter bindAdapter = new BindAdapter(recyclerView.getContext(), dataHolder.bindAdapterCallback, dataHolder.bindResourceId, list, dataHolder.footViewModel);
            bindAdapter.setItemClickListener(dataHolder.listener);
            recyclerView.setAdapter(bindAdapter);
        } else if (adapter instanceof BindAdapter) {
            BindAdapter bindAdapter = (BindAdapter) adapter;
            bindAdapter.setItemClickListener(dataHolder.listener);
            bindAdapter.resetList(list);
            if (dataHolder.listChangedAction != null) {
                //根据不同的类型选择不同的更新方式
                int positionStart = dataHolder.listChangedAction.positionStart;
                int itemCount = dataHolder.listChangedAction.itemCount;
                switch (dataHolder.listChangedAction.type) {
                    case INSERT:
                        if (itemCount == 1) {
                            adapter.notifyItemInserted(positionStart);
                        } else {
                            adapter.notifyItemRangeInserted(positionStart, itemCount);
                        }
                        break;
                    case REMOVE:
                        if (itemCount == 1) {
                            adapter.notifyItemRemoved(positionStart);
                        } else {
                            adapter.notifyItemRangeRemoved(positionStart, itemCount);
                        }
                        break;
                    case CHANGE:
                        if (itemCount == 1) {
                            adapter.notifyItemChanged(positionStart);
                        } else {
                            adapter.notifyItemRangeChanged(positionStart, itemCount);
                        }
                        break;
                }
            } else {
                adapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * 设置加载更多的监听
     */
    public static void setOnLoadMoreListener(RecyclerView recyclerView, OnLoadMoreListener listener) {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                    if (layoutManager instanceof LinearLayoutManager) {
                        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
                        int totalItemCount = linearLayoutManager.getItemCount();
                        int lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                        if (totalItemCount >= PAGE_SIZE && totalItemCount <= lastVisibleItem + 1) {
                            listener.onLoadMore();
                        }
                    }
                }
            }
        });
    }

    /**
     * 精准位置滚动
     */
    public static void scrollToPosition(RecyclerView recyclerView, int position) {
        if (position >= 0) {
            recyclerView.smoothScrollToPosition(position);
            LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            if (layoutManager != null) {
                int firstItem = layoutManager.findFirstVisibleItemPosition();
                int lastItem = layoutManager.findLastVisibleItemPosition();
                if (position <= firstItem) {
                    recyclerView.scrollToPosition(position);
                } else if (position <= lastItem) {
                    int top = recyclerView.getChildAt(position - firstItem).getTop();

                    recyclerView.scrollBy(0, top);
                } else {
                    recyclerView.scrollToPosition(position);

                    recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                        @Override
                        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                            super.onScrolled(recyclerView, dx, dy);
                            int n = position - layoutManager.findFirstVisibleItemPosition();
                            if (0 <= n && n < recyclerView.getChildCount()) {
                                int top = recyclerView.getChildAt(n).getTop();
                                recyclerView.scrollBy(0, top);
                            }
                            recyclerView.removeOnScrollListener(this);
                        }
                    });
                }
            }
        }
    }

    /**
     * 设置下拉刷新
     */
    public static void setRefreshListener(SmartRefreshLayout swipeRefreshLayout, OnBindingRefreshListener listener) {
        if (listener == null) {
            swipeRefreshLayout.setOnRefreshListener(null);
            swipeRefreshLayout.setEnableRefresh(false);
        } else {
            swipeRefreshLayout.setOnRefreshListener(refreshLayout -> listener.onRefresh());
        }
    }

    /**
     * 取消下拉刷新
     */
    public static void cancelRefresh(SmartRefreshLayout swipeRefreshLayout, int refreshStatus) {
        if (refreshStatus == 1) {
            swipeRefreshLayout.autoRefresh();
        } else if (refreshStatus == -1) {
            swipeRefreshLayout.finishRefresh();
        }
    }
}
