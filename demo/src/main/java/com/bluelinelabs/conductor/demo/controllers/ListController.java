package com.bluelinelabs.conductor.demo.controllers;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler;
import com.bluelinelabs.conductor.demo.R;
import com.bluelinelabs.conductor.demo.changehandler.listeners.BackstackChangeListener;
import com.bluelinelabs.conductor.demo.controllers.base.BaseController;
import com.bluelinelabs.conductor.demo.util.BundleBuilder;
import com.bluelinelabs.conductor.demo.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ListController extends BaseController {

    private static final String KEY_INDEX = "ItemListController.index";

    private static final int ITEMS_COUNT = 6;

    private int index;

    @Nullable
    private Integer selectedIndex;

    @Nullable
    private ItemAdapter adapter;

    @BindView(R.id.text_view)
    TextView textView;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private BackstackChangeListener changeListener = new BackstackChangeListener(this::checkSelectedIndex);

    public ListController(int index) {
        this(new BundleBuilder(new Bundle())
                .putInt(KEY_INDEX, index)
                .build());

    }

    public ListController(Bundle args) {
        super(args);
        index = getArgs().getInt(KEY_INDEX);
    }

    @Override
    protected String getTitle() {
        return "Master/Detail Flow";
    }

    @Override
    protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        return inflater.inflate(R.layout.controller_list, container, false);
    }

    @Override
    protected void onViewBound(@NonNull View view) {
        super.onViewBound(view);

        adapter = new ItemAdapter();
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        recyclerView.setAdapter(adapter);
        textView.setVisibility(isTwoPanesMode() ? View.GONE : View.VISIBLE);
        if (isTwoPanesMode()) {
            getMasterDetailController().getDetailRouter().addChangeListener(changeListener);
            checkSelectedIndex();
        }
    }

    @Override
    protected void onDestroyView(@NonNull View view) {
        if (isTwoPanesMode()) {
            getMasterDetailController().getDetailRouter().removeChangeListener(changeListener);
        }
        recyclerView.setAdapter(null);
        super.onDestroyView(view);
    }

    private void onItemSelected(ItemAdapter.Item item) {
        if (item.disclosable) {
            getRouter().pushController(RouterTransaction.with(new ListController(item.index + ITEMS_COUNT))
                    .pushChangeHandler(new HorizontalChangeHandler())
                    .popChangeHandler(new HorizontalChangeHandler()));
        } else {
            Controller controller = new NavigationDemoController(item.index, NavigationDemoController.DisplayUpMode.HIDE);
            if (getMasterDetailController() != null) {
                getMasterDetailController().getDetailRouter().setRoot(RouterTransaction.with(controller));
            } else {
                // Won't happen in this example.
                // However, in real scenario same controller can be used
                // either in master/detail (tablet) or in regular (phone) parent controller
                getRouter().pushController(RouterTransaction.with(controller)
                        .pushChangeHandler(new HorizontalChangeHandler())
                        .popChangeHandler(new HorizontalChangeHandler()));
            }
        }
    }

    private boolean isTwoPanesMode() {
        return getMasterDetailController() != null && getMasterDetailController().isTwoPanesMode();
    }

    private void checkSelectedIndex() {
        if (isTwoPanesMode()) {
            List<RouterTransaction> transactions = getMasterDetailController().getDetailRouter().getBackstack();
            if (transactions.isEmpty()) {
                setSelectedIndex(null);
            } else {
                Controller controller = transactions.get(0).controller();
                if (controller instanceof NavigationDemoController) {
                    setSelectedIndex(((NavigationDemoController) controller).getIndex());
                }
            }
        }
    }

    private void setSelectedIndex(@Nullable Integer index) {
        if (!ObjectUtils.equals(selectedIndex, index)) {
            selectedIndex = index;
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }
    }

    class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {

        class Item {
            int index;
            public String name;
            boolean disclosable;
        }

        private final List<Item> items;

        ItemAdapter() {
            this.items = new ArrayList<>(ITEMS_COUNT);
            for (int i = 0; i < ITEMS_COUNT; i++) {
                Item item = new Item();
                item.index = index + i;
                item.disclosable = i < ITEMS_COUNT / 2;
                item.name = (item.disclosable ? "Folder " : "File ") + item.index;
                items.add(item);
            }
        }

        @Override
        public ItemAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            return new ItemAdapter.ViewHolder(inflater.inflate(R.layout.row_detail_item, parent, false));
        }

        @Override
        public void onBindViewHolder(ItemAdapter.ViewHolder holder, int position) {
            holder.bind(items.get(position));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.row_root) View root;
            @BindView(R.id.tv_title) TextView tvTitle;
            @BindView(R.id.iv_chevron) ImageView ivChevron;
            private Item item;

            ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }

            void bind(Item item) {
                this.item = item;

                tvTitle.setText(item.name);
                ivChevron.setVisibility(item.disclosable ? View.VISIBLE : View.GONE);

                if (isTwoPanesMode() && ObjectUtils.equals(selectedIndex, item.index)) {
                    root.setBackgroundColor(ContextCompat.getColor(root.getContext(), R.color.grey_300));
                } else {
                    root.setBackgroundColor(ContextCompat.getColor(root.getContext(), android.R.color.transparent));
                }
            }

            @OnClick(R.id.row_root)
            void onRowClick() {
                onItemSelected(item);
            }

        }
    }
}
