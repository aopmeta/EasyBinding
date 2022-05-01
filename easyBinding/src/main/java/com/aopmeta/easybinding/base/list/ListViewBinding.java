package com.aopmeta.easybinding.base.list;

import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.aopmeta.easybinding.BDR;
import com.aopmeta.easybinding.base.BaseViewModel;
import com.aopmeta.easybinding.BindingConstants;
import com.aopmeta.easybinding.R;
import com.aopmeta.easybinding.base.ViewBinding;
import com.aopmeta.easybinding.value.TextDrawableValueBinder;
import com.aopmeta.easybinding.value.TextValueBinder;
import com.aopmeta.easybinding.value.ValueBinder;
import com.aopmeta.easybinding.value.VisibleValueBinder;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;

/**
 * 列表视图绑定管理类
 */
public class ListViewBinding extends ViewBinding {
    public ListViewBinding(FragmentActivity activity, Class<? extends ListViewModel> viewModelClass) {
        super(activity, viewModelClass);
    }

    public ListViewBinding(Fragment fragment, View view, Class<? extends ListViewModel> viewModelClass) {
        super(fragment, view, viewModelClass);
    }

    public int getListWithEmptyResId() {
        return R.id.layout_list_binding;
    }

    @Override
    protected void setViewModel(BaseViewModel viewModel) {
        super.setViewModel(viewModel);
        ListViewModel listViewModel = (ListViewModel) viewModel;

        View view = findView(getListWithEmptyResId());
        view.setBackground(listViewModel.getBackground());
        View emptyView = view.findViewById(R.id.layout_list_binding_empty);
        View emptyTextView = view.findViewById(R.id.tv_list_binding);

        emptyView.setOnClickListener(v -> listViewModel.emptyTextClicked());
        //没有数据或者加载失败等状态进行绑定
        bind(emptyView, new VisibleValueBinder(), BDR.emptyViewVisible);
        bind(emptyTextView, new TextValueBinder(), BDR.emptyText);
        bind(emptyTextView, new TextDrawableValueBinder(TextDrawableValueBinder.Direction.TOP), BDR.emptyImageResId);

        RecyclerView recyclerView = view.findViewById(R.id.rv_list_binding);
        BindingConstants.setDecoration(recyclerView, listViewModel.getLayoutManager(), listViewModel.getDecoration());
        //这里才是绑定列表数据
        bind(recyclerView, (ValueBinder<RecyclerView, ListData>) BindingConstants::bindRecyclerView, BDR.liveData);
        //绑定精准滚动
        bind(recyclerView, (ValueBinder<RecyclerView, Integer>) BindingConstants::scrollToPosition, BDR.scrollPosition);
        BindingConstants.setOnLoadMoreListener(recyclerView, listViewModel);
        SmartRefreshLayout refreshLayout = view.findViewById(R.id.layout_swipe_v4);
        if (refreshLayout != null) {
            BindingConstants.setRefreshListener(refreshLayout, listViewModel.getOnRefreshListener());
            //绑定刷新状态
            bind(refreshLayout, (ValueBinder<SmartRefreshLayout, Integer>) BindingConstants::cancelRefresh, BDR.refreshStatus);
        }
    }
}
