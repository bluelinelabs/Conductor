package com.bluelinelabs.conductor.demo.controllers;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler;
import com.bluelinelabs.conductor.demo.R;
import com.bluelinelabs.conductor.demo.controllers.base.BaseController;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MasterDetailListController extends BaseController {

    private static final String KEY_SELECTED_INDEX = "MasterDetailListController.selectedIndex";

    public enum DetailItemModel {
        ONE("Item 1", "This is a quick demo of master/detail flow using Conductor. In portrait mode you'll see a standard list. In landscape, you'll see a two-pane layout.", R.color.green_300),
        TWO("Item 2", "This is another item.", R.color.cyan_300),
        THREE("Item 3", "Wow, a 3rd item!", R.color.deep_purple_300);

        String title;
        String detail;
        int backgroundColor;

        DetailItemModel(String title, String detail, int backgroundColor) {
            this.title = title;
            this.detail = detail;
            this.backgroundColor = backgroundColor;
        }
    }

    @BindView(R.id.recycler_view) RecyclerView recyclerView;
    @Nullable @BindView(R.id.detail_container) ViewGroup detailContainer;

    private int selectedIndex;
    private boolean twoPaneView;

    @Override
    protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        return inflater.inflate(R.layout.controller_master_detail_list, container, false);
    }

    @Override
    protected void onViewBound(@NonNull View view) {
        super.onViewBound(view);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        recyclerView.setAdapter(new DetailItemAdapter(LayoutInflater.from(view.getContext()), DetailItemModel.values()));

        twoPaneView = (detailContainer != null);
        if (twoPaneView) {
            onRowSelected(selectedIndex);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(KEY_SELECTED_INDEX, selectedIndex);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        selectedIndex = savedInstanceState.getInt(KEY_SELECTED_INDEX);
    }

    @Override
    protected String getTitle() {
        return "Master/Detail Flow";
    }

    void onRowSelected(int index) {
        selectedIndex = index;

        DetailItemModel model = DetailItemModel.values()[index];
        ChildController controller = new ChildController(model.detail, model.backgroundColor, true);

        if (twoPaneView) {
            getChildRouter(detailContainer).setRoot(RouterTransaction.with(controller));
        } else {
            getRouter().pushController(RouterTransaction.with(controller)
                    .pushChangeHandler(new HorizontalChangeHandler())
                    .popChangeHandler(new HorizontalChangeHandler()));
        }
    }

    class DetailItemAdapter extends RecyclerView.Adapter<DetailItemAdapter.ViewHolder> {

        private final LayoutInflater inflater;
        private final DetailItemModel[] items;

        public DetailItemAdapter(LayoutInflater inflater, DetailItemModel[] items) {
            this.inflater = inflater;
            this.items = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(inflater.inflate(R.layout.row_detail_item, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.bind(items[position], position);
        }

        @Override
        public int getItemCount() {
            return items.length;
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.row_root) View root;
            @BindView(R.id.tv_title) TextView tvTitle;
            private int position;

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }

            void bind(DetailItemModel item, int position) {
                tvTitle.setText(item.title);
                this.position = position;

                if (twoPaneView && position == selectedIndex) {
                    root.setBackgroundColor(ContextCompat.getColor(root.getContext(), R.color.grey_400));
                } else {
                    root.setBackgroundColor(ContextCompat.getColor(root.getContext(), android.R.color.transparent));
                }
            }

            @OnClick(R.id.row_root)
            void onRowClick() {
                onRowSelected(position);
                notifyDataSetChanged();
            }

        }
    }

}
