package com.bluelinelabs.conductor.demo.controllers;

import android.content.Intent;
import android.graphics.PorterDuff.Mode;
import android.net.Uri;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bluelinelabs.conductor.ControllerChangeHandler;
import com.bluelinelabs.conductor.ControllerChangeType;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler;
import com.bluelinelabs.conductor.demo.R;
import com.bluelinelabs.conductor.demo.controllers.base.BaseController;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class HomeController extends BaseController {

    public enum HomeDemoModel {
        NAVIGATION("Navigation Demos", R.color.red_300),
        TRANSITIONS("Transition Demos", R.color.blue_grey_300),
        CHILD_CONTROLLERS("Child Controllers", R.color.orange_300),
        VIEW_PAGER("ViewPager", R.color.green_300),
        TARGET_CONTROLLER("Target Controller", R.color.pink_300),
        MULTIPLE_CHILD_ROUTERS("Multiple Child Routers", R.color.deep_orange_300),
        MASTER_DETAIL("Master Detail", R.color.grey_300),
        DRAG_DISMISS("Drag Dismiss", R.color.lime_300),
        RX_LIFECYCLE("Rx Lifecycle", R.color.teal_300),
        OVERLAY("Overlay Controller", R.color.purple_300);

        String title;
        @ColorRes int color;

        HomeDemoModel(String title, @ColorRes int color) {
            this.title = title;
            this.color = color;
        }
    }

    @BindView(R.id.recycler_view) RecyclerView recyclerView;
    @BindView(R.id.overlay_root) ViewGroup overlayRoot;

    public HomeController() {
        setHasOptionsMenu(true);
    }

    @NonNull
    @Override
    protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        return inflater.inflate(R.layout.controller_home, container, false);
    }

    @Override
    protected void onViewBound(@NonNull View view) {
        super.onViewBound(view);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        recyclerView.setAdapter(new HomeAdapter(LayoutInflater.from(view.getContext()), HomeDemoModel.values()));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.home, menu);
    }

    @Override
    protected void onChangeStarted(@NonNull ControllerChangeHandler changeHandler, @NonNull ControllerChangeType changeType) {
        setOptionsMenuHidden(!changeType.isEnter);

        if (changeType.isEnter) {
            setTitle();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.about) {
            SpannableString details = new SpannableString("A small, yet full-featured framework that allows building View-based Android applications");
            details.setSpan(new AbsoluteSizeSpan(16, true), 0, details.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

            final String url = "https://github.com/bluelinelabs/Conductor";
            SpannableString link = new SpannableString(url);
            link.setSpan(new URLSpan(url) {
                @Override
                public void onClick(View widget) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                }
            }, 0, link.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

            SpannableStringBuilder content = new SpannableStringBuilder();
            content.append("Conductor");
            content.append("\n\n");
            content.append(details);
            content.append("\n\n");
            content.append(link);

            getChildRouter(overlayRoot, null)
                    .setPopsLastView(true)
                    .setRoot(RouterTransaction.with(new OverlayController(content))
                            .pushChangeHandler(new FadeChangeHandler())
                            .popChangeHandler(new FadeChangeHandler()));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected String getTitle() {
        return "Conductor Demos";
    }

    void onModelRowClick(HomeDemoModel model) {
        switch (model) {
            case NAVIGATION:
                getRouter().pushController(RouterTransaction.with(new NavigationDemoController(0, true))
                        .pushChangeHandler(new FadeChangeHandler())
                        .popChangeHandler(new FadeChangeHandler())
                        .tag(NavigationDemoController.TAG_UP_TRANSACTION)
                );
                break;
            case TRANSITIONS:
                getRouter().pushController(TransitionDemoController.getRouterTransaction(0, this));
                break;
            case TARGET_CONTROLLER:
                getRouter().pushController(
                        RouterTransaction.with(new TargetDisplayController())
                                .pushChangeHandler(new FadeChangeHandler())
                                .popChangeHandler(new FadeChangeHandler()));
                break;
            case VIEW_PAGER:
                getRouter().pushController(RouterTransaction.with(new PagerController())
                        .pushChangeHandler(new FadeChangeHandler())
                        .popChangeHandler(new FadeChangeHandler()));
                break;
            case CHILD_CONTROLLERS:
                getRouter().pushController(RouterTransaction.with(new ParentController())
                        .pushChangeHandler(new FadeChangeHandler())
                        .popChangeHandler(new FadeChangeHandler()));
                break;
            case OVERLAY:
                getChildRouter(overlayRoot, null)
                        .setPopsLastView(true)
                        .setRoot(RouterTransaction.with(new OverlayController("I'm an overlay!"))
                                .pushChangeHandler(new FadeChangeHandler())
                                .popChangeHandler(new FadeChangeHandler()));
                break;
            case DRAG_DISMISS:
                getRouter().pushController(RouterTransaction.with(new DragDismissController())
                        .pushChangeHandler(new FadeChangeHandler(false))
                        .popChangeHandler(new FadeChangeHandler()));
                break;
            case RX_LIFECYCLE:
                getRouter().pushController(RouterTransaction.with(new RxLifecycleController())
                        .pushChangeHandler(new FadeChangeHandler())
                        .popChangeHandler(new FadeChangeHandler()));
                break;
            case MULTIPLE_CHILD_ROUTERS:
                getRouter().pushController(RouterTransaction.with(new MultipleChildRouterController())
                        .pushChangeHandler(new FadeChangeHandler())
                        .popChangeHandler(new FadeChangeHandler()));
                break;
            case MASTER_DETAIL:
                getRouter().pushController(RouterTransaction.with(new MasterDetailListController())
                        .pushChangeHandler(new FadeChangeHandler())
                        .popChangeHandler(new FadeChangeHandler()));
                break;
        }
    }

    class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ViewHolder> {

        private final LayoutInflater inflater;
        private final HomeDemoModel[] items;

        public HomeAdapter(LayoutInflater inflater, HomeDemoModel[] items) {
            this.inflater = inflater;
            this.items = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(inflater.inflate(R.layout.row_home, parent, false));
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

            @BindView(R.id.tv_title) TextView tvTitle;
            @BindView(R.id.img_dot) ImageView imgDot;
            private HomeDemoModel model;

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }

            void bind(HomeDemoModel item) {
                model = item;
                tvTitle.setText(item.title);
                imgDot.getDrawable().setColorFilter(ContextCompat.getColor(getActivity(), item.color), Mode.SRC_ATOP);
            }

            @OnClick(R.id.row_root)
            void onRowClick() {
                onModelRowClick(model);
            }

        }
    }

}
