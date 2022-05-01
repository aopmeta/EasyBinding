package com.aopmeta.sample.viewmodel;

import android.app.Application;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.MutableLiveData;

import com.aopmeta.sample.BDR;
import com.aopmeta.sample.R;
import com.aopmeta.easybinding.Resource;
import com.aopmeta.easybinding.base.ViewBinding;
import com.aopmeta.easybinding.base.list.ListData;
import com.aopmeta.easybinding.base.list.ListViewModel;
import com.aopmeta.easybinding.base.list.OnBindAdapterCallback;
import com.aopmeta.easybinding.base.list.OnItemClickListener;
import com.aopmeta.sample.model.GroupProfile;
import com.aopmeta.sample.model.UserProfile;
import com.aopmeta.easybinding.value.ImageValueBinder;
import com.aopmeta.easybinding.value.ValueBinder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class TreeViewModel extends ListViewModel implements OnItemClickListener {
    private final ExecutorService executorService = Executors.newFixedThreadPool(1);

    private final MutableLiveData<Resource<List<TreeItemViewModel>>> listLiveData =
            new MutableLiveData<>();

    public TreeViewModel(@NonNull Application application) {
        super(application);

        //绑定数据和视图
        setResourceIdAndResource(
                new OnBindAdapterCallback() {
                    @Override
                    public void bind(ViewBinding binding, ViewGroup root, int viewType) {
                        if (viewType == R.layout.list_item_tree_group) {
                            binding.bind(R.id.iv_tree_item_expand_group, new ImageValueBinder(), BDR.expandDrawable);
                            binding.bind(R.id.switcher_tree_item_group, (ValueBinder<ViewSwitcher, Boolean>) (view, value) -> view.setDisplayedChild(value ? 1 : 0), BDR.loading);
                        }
                    }

                    @Override
                    public void setValue(ViewBinding binding, Object value) {
                        super.setValue(binding, value);

                        TreeItemViewModel itemViewModel = (TreeItemViewModel) value;
                        View rootView = binding.getRootView();

                        rootView.setPadding(itemViewModel.getPaddingLeft(), rootView.getPaddingTop(), rootView.getPaddingEnd(), rootView.getPaddingBottom());
                        if (itemViewModel.isGroup()) {
                            TextView groupNameView = binding.findView(R.id.tv_tree_item_group_name);
                            groupNameView.setText(itemViewModel.groupProfile.name);
                        } else {
                            TextView userNameView = binding.findView(R.id.tv_tree_item_user_name);
                            userNameView.setText(itemViewModel.userProfile.name);
                            ImageView userPhotoView = binding.findView(R.id.iv_tree_item_user);
                            userPhotoView.setImageResource(itemViewModel.userProfile.avatar);
                        }
                    }
                }, 0,
                listLiveData);
        setItemClickListener(this);
    }

    @Override
    public void setActivity(FragmentActivity activity) {
        super.setActivity(activity);
        loadTreeData();
    }

    /**
     * 设置模拟数据，以如下树型机构展示：
     * ˇ小组1
     *   ˇ小组1-1
     * ˇ小组2
     *   ˇ小组2-1
     *     小明
     *   ˇ小组2-2
     *     ˇ小组2-2-1
     *       小华
     *   小明
     *   小华
     *   小方
     * ˇ小组3
     */
    public void loadTreeData() {
        listLiveData.setValue(Resource.loading(listLiveData.getValue() != null ? listLiveData.getValue().getData() : null));
        executorService.submit(() -> {
            List<GroupProfile> groupList = Arrays.asList(
                    new GroupProfile("group1", "小组1", null),
                    new GroupProfile("group2", "小组2", null),
                    new GroupProfile("group3", "小组3", null),
                    new GroupProfile("group1-1", "小组1-1", "group1"),
                    new GroupProfile("group2-1", "小组2-1", "group2"),
                    new GroupProfile("group2-2", "小组2-2", "group2"),
                    new GroupProfile("group2-2-1", "小组2-2-1", "group2-2")
            );
            List<UserProfile> userList = Arrays.asList(
                    new UserProfile("user1", "小明", R.drawable.ic_user, Arrays.asList("group2", "group2-1")),
                    new UserProfile("user2", "小华", R.drawable.ic_user, Arrays.asList("group2", "group2-2-1")),
                    new UserProfile("user3", "小方", R.drawable.ic_user, Collections.singletonList("group2"))
            );

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            listLiveData.postValue(
                    Resource.success(getNodeChildren(null, 1, groupList, userList)));
            setRefreshStatus(-1);
        });
    }

    /**
     * 将分组和人员整合成树型结构
     */
    private List<TreeItemViewModel> getNodeChildren(String nodeId, int level, List<GroupProfile> groupList, List<UserProfile> userList) {
        List<TreeItemViewModel> nodeChildren = new ArrayList<>();

        if (groupList != null) {
            for (int i = 0; i < groupList.size(); i++) {
                GroupProfile responseData = groupList.get(i);
                if (TextUtils.equals(responseData.parentId, nodeId)) {
                    nodeChildren.add(new TreeItemViewModel(getApplication(), level, responseData));
                }
            }
        }
        if (userList != null) {
            for (int i = 0; i < userList.size(); i++) {
                UserProfile userProfile = userList.get(i);
                if (userProfile.groupIds.contains(nodeId)) {
                    nodeChildren.add(new TreeItemViewModel(getApplication(), level, userProfile));
                }
            }
        }

        for (TreeItemViewModel itemViewModel : nodeChildren) {
            if (itemViewModel.groupProfile != null) {
                List<TreeItemViewModel> children = getNodeChildren(itemViewModel.groupProfile.id, itemViewModel.level + 1, groupList, userList);
                itemViewModel.setChildItemList(children);
                itemViewModel.setChildLoad(true);
            }
        }

        return nodeChildren;
    }

    @Override
    public void onItemClick(int position) {
        TreeItemViewModel viewModel = get(position);
        if (viewModel != null) {
            onItemClick(viewModel, position);
        }
    }

    private void onItemClick(TreeItemViewModel viewModel, int position) {
        if (viewModel.isGroup()) {
            if (viewModel.isChildLoad()) {
                boolean expand = viewModel.isExpand();
                Resource<List<TreeItemViewModel>> resource = listLiveData.getValue();
                if (resource != null) {
                    List<TreeItemViewModel> viewModelList = resource.getData();
                    if (viewModelList != null) {
                        List<TreeItemViewModel> children = viewModel.getChildItemList();
                        if (children != null && children.size() > 0) {
                            if (expand) {
                                //收集所有已经展开的节点进行移除
                                List<TreeItemViewModel> collectList = new ArrayList<>();
                                viewModel.shrink(collectList);
                                setListChangedAction(new ListData.ListChangedAction(
                                        ListData.ListChangedType.REMOVE, position + 1,
                                        collectList.size()));
                                viewModelList.removeAll(collectList);
                            } else {
                                //展开子节点
                                setListChangedAction(new ListData.ListChangedAction(
                                        ListData.ListChangedType.INSERT, position + 1,
                                        children.size()));
                                viewModelList.addAll(position + 1, children);
                            }
                            listLiveData.setValue(listLiveData.getValue());
                        }
                    }
                }
                viewModel.setExpand(!expand);
            }
        }
    }

    @Override
    public void onItemLongClick(int position) {

    }

    private TreeItemViewModel get(int position) {
        Resource<List<TreeItemViewModel>> resource = listLiveData.getValue();
        if (resource != null && resource.getData() != null) {
            return resource.getData().get(position);
        }
        return null;
    }
}
