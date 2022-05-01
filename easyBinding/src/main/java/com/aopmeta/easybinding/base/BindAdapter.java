package com.aopmeta.easybinding.base;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.aopmeta.easybinding.BDR;
import com.aopmeta.easybinding.R;
import com.aopmeta.easybinding.base.list.FootViewModel;
import com.aopmeta.easybinding.base.list.OnBindAdapterCallback;
import com.aopmeta.easybinding.base.list.OnItemClickListener;
import com.aopmeta.easybinding.value.VisibleValueBinder;

import java.util.ArrayList;
import java.util.List;

/**
 * 列表数据绑定使用的适配器
 */
public class BindAdapter extends RecyclerView.Adapter<BindAdapter.BaseViewHolder> implements ItemTouchHelperAdapter {
    private final Context mContext;
    private final OnBindAdapterCallback mBindAdapterCallback;
    private final int mResourceId;
    private final List<Object> mList;
    private final FootViewModel mFooterViewModel;

    private OnItemClickListener mItemClickListener;

    public BindAdapter(Context context, OnBindAdapterCallback bindAdapterCallback, int resourceId, List<?> list, FootViewModel footViewModel) {
        mContext = context;
        mBindAdapterCallback = bindAdapterCallback;
        mResourceId = resourceId;

        if (list != null) {
            //noinspection unchecked
            mList = (List<Object>) list;
        } else {
            mList = new ArrayList<>();
        }
        mFooterViewModel = footViewModel;
    }

    public void resetList(List<?> list) {
        if (mList != list) {
            mList.clear();
            if (list != null) {
                mList.addAll(list);
            }
        }
    }

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        mItemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        int resourceId;
        if (viewType == 0) {
            resourceId = R.layout.layout_recycler_foot;
        } else {
            resourceId = viewType;
        }
        View view = layoutInflater.inflate(resourceId, viewGroup, false);
        ViewBinding viewBinding = new ViewBinding(view);
        if (viewType == 0) {
            viewBinding.bind(R.id.progress_footer, new VisibleValueBinder(), BDR.progressVisible);
        } else {
            if (mBindAdapterCallback != null) {
                mBindAdapterCallback.bind(viewBinding, viewGroup, viewType);
            }
        }
        return new BaseViewHolder(viewBinding);
    }

    @Override
    public int getItemViewType(int position) {
        if (mFooterViewModel != null && mList.size() > 0 && position == mList.size()) {
            return 0;
        } else {
            //用resourceId作为viewType构建千变万化的item
            Object obj = mList.get(position);
            if (obj instanceof BaseViewModel) {
                BaseViewModel baseViewModel = (BaseViewModel) obj;
                int itemResId = baseViewModel.getRecyclerViewItemResId();
                if (itemResId > 0) {
                    return itemResId;
                }
            }
            return mResourceId;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder baseViewHolder, int position) {
        if (getItemViewType(position) == 0) {
            if (mContext instanceof FragmentActivity) {
                mFooterViewModel.setActivity((FragmentActivity) mContext);
            }
            baseViewHolder.binding.setViewModel(mFooterViewModel);
        } else {
            Object obj = mList.get(position);

            if (mContext instanceof FragmentActivity && obj instanceof BaseViewModel) {
                BaseViewModel viewModelObservable = (BaseViewModel) obj;
                viewModelObservable.setActivity((FragmentActivity) mContext);
            }

            View view = baseViewHolder.binding.getRootView();
            if (obj instanceof BaseViewModel) {
                baseViewHolder.binding.setViewModel((BaseViewModel) obj);
            }
            baseViewHolder.binding.resetAllValues();
            if (mBindAdapterCallback != null) {
                mBindAdapterCallback.setValue(baseViewHolder.binding, obj);
            }
            if (mItemClickListener != null) {
                view.setOnClickListener(v -> mItemClickListener.onItemClick(mList.indexOf(obj)));
                view.setOnLongClickListener(v -> {
                    mItemClickListener.onItemLongClick(mList.indexOf(obj));
                    return true;
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        int size = mList.size();
        if (size > 0) {
            if (mFooterViewModel != null) {
                return size + 1;
            } else {
                return size;
            }
        } else {
            return 0;
        }
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        Object obj = mList.get(fromPosition);
        mList.set(fromPosition, mList.get(toPosition));
        mList.set(toPosition, obj);
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onItemDismiss(int position) {
        mList.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public boolean isItemViewSwipeEnabled(int position) {
        return false;
    }

    @Override
    public boolean isLastItemDisable() {
        return false;
    }

    static class BaseViewHolder extends RecyclerView.ViewHolder {
        final ViewBinding binding;

        BaseViewHolder(@NonNull ViewBinding binding) {
            super(binding.getRootView());
            this.binding = binding;
        }
    }
}
