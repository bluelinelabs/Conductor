package com.bluelinelabs.conductor.demo.controllers;

import android.graphics.PorterDuff.Mode;
import android.os.Bundle;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler;
import com.bluelinelabs.conductor.changehandler.TransitionChangeHandlerCompat;
import com.bluelinelabs.conductor.demo.R;
import com.bluelinelabs.conductor.demo.changehandler.CityGridSharedElementTransitionChangeHandler;
import com.bluelinelabs.conductor.demo.controllers.base.BaseController;
import com.bluelinelabs.conductor.demo.util.BundleBuilder;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CityGridController extends BaseController {

    private static final String KEY_TITLE = "CityGridController.title";
    private static final String KEY_DOT_COLOR = "CityGridController.dotColor";
    private static final String KEY_FROM_POSITION = "CityGridController.position";

    private static final CityModel[] CITY_MODELS = new CityModel[] {
            new CityModel(R.drawable.chicago, "Chicago"),
            new CityModel(R.drawable.jakarta, "Jakarta"),
            new CityModel(R.drawable.london, "London"),
            new CityModel(R.drawable.sao_paulo, "Sao Paulo"),
            new CityModel(R.drawable.tokyo, "Tokyo")
    };

    @BindView(R.id.tv_title) TextView tvTitle;
    @BindView(R.id.img_dot) ImageView imgDot;
    @BindView(R.id.recycler_view) RecyclerView recyclerView;

    private String title;
    private int dotColor;
    private int fromPosition;

    public CityGridController(String title, int dotColor, int fromPosition) {
        this(new BundleBuilder(new Bundle())
                .putString(KEY_TITLE, title)
                .putInt(KEY_DOT_COLOR, dotColor)
                .putInt(KEY_FROM_POSITION, fromPosition)
                .build());
    }

    public CityGridController(Bundle args) {
        super(args);
        title = getArgs().getString(KEY_TITLE);
        dotColor = getArgs().getInt(KEY_DOT_COLOR);
        fromPosition = getArgs().getInt(KEY_FROM_POSITION);
    }

    @NonNull
    @Override
    protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        return inflater.inflate(R.layout.controller_city_grid, container, false);
    }

    @Override
    protected void onViewBound(@NonNull View view) {
        super.onViewBound(view);

        tvTitle.setText(title);
        imgDot.getDrawable().setColorFilter(ContextCompat.getColor(getActivity(), dotColor), Mode.SRC_ATOP);

        ViewCompat.setTransitionName(tvTitle, getResources().getString(R.string.transition_tag_title_indexed, fromPosition));
        ViewCompat.setTransitionName(imgDot, getResources().getString(R.string.transition_tag_dot_indexed, fromPosition));

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(view.getContext(), 2));
        recyclerView.setAdapter(new CityGridAdapter(LayoutInflater.from(view.getContext()), CITY_MODELS));
    }

    @Override
    protected String getTitle() {
        return "Shared Element Demos";
    }

    void onModelRowClick(CityModel model) {
        String imageTransitionName = getResources().getString(R.string.transition_tag_image_named, model.title);
        String titleTransitionName = getResources().getString(R.string.transition_tag_title_named, model.title);

        List<String> names = new ArrayList<>();
        names.add(imageTransitionName);
        names.add(titleTransitionName);

        getRouter().pushController(RouterTransaction.with(new CityDetailController(model.drawableRes, model.title))
                .pushChangeHandler(new TransitionChangeHandlerCompat(new CityGridSharedElementTransitionChangeHandler(names), new FadeChangeHandler()))
                .popChangeHandler(new TransitionChangeHandlerCompat(new CityGridSharedElementTransitionChangeHandler(names), new FadeChangeHandler())));
    }

    class CityGridAdapter extends RecyclerView.Adapter<CityGridAdapter.ViewHolder> {

        private final LayoutInflater inflater;
        private final CityModel[] items;

        public CityGridAdapter(LayoutInflater inflater, CityModel[] items) {
            this.inflater = inflater;
            this.items = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(inflater.inflate(R.layout.row_city_grid, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.bind(items[position]);
        }

        @Override
        public int getItemCount() {
            return items.length;
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.tv_title) TextView textView;
            @BindView(R.id.img_city) ImageView imageView;
            private CityModel model;

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }

            void bind(CityModel item) {
                model = item;
                imageView.setImageResource(item.drawableRes);
                textView.setText(item.title);

                ViewCompat.setTransitionName(textView, getResources().getString(R.string.transition_tag_title_named, model.title));
                ViewCompat.setTransitionName(imageView, getResources().getString(R.string.transition_tag_image_named, model.title));
            }

            @OnClick(R.id.row_root)
            void onRowClick() {
                onModelRowClick(model);
            }

        }
    }

    private static class CityModel {
        @DrawableRes int drawableRes;
        String title;

        public CityModel(@DrawableRes int drawableRes, String title) {
            this.drawableRes = drawableRes;
            this.title = title;
        }
    }
}
