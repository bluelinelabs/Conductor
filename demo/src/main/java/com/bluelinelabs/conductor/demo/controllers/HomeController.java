package com.bluelinelabs.conductor.demo.controllers;

import android.content.Intent;
import android.graphics.PorterDuff.Mode;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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
import com.bluelinelabs.conductor.changehandler.TransitionChangeHandlerCompat;
import com.bluelinelabs.conductor.demo.R;
import com.bluelinelabs.conductor.demo.changehandler.ArcFadeMoveChangeHandler;
import com.bluelinelabs.conductor.demo.changehandler.FabToDialogTransitionChangeHandler;
import com.bluelinelabs.conductor.demo.controllers.NavigationDemoController.DisplayUpMode;
import com.bluelinelabs.conductor.demo.controllers.base.BaseController;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class HomeController extends BaseController {

    private enum HomeDemoModel {
        NAVIGATION("Navigation Demos", R.color.red_300),
        TRANSITIONS("Transition Demos", R.color.blue_grey_300),
        SHARED_ELEMENT_TRANSITIONS("Shared Element Demos", R.color.purple_300),
        CHILD_CONTROLLERS("Child Controllers", R.color.orange_300),
        VIEW_PAGER("ViewPager", R.color.green_300),
        TARGET_CONTROLLER("Target Controller", R.color.pink_300),
        MULTIPLE_CHILD_ROUTERS("Multiple Child Routers", R.color.deep_orange_300),
        MASTER_DETAIL("Master Detail", R.color.grey_300),
        DRAG_DISMISS("Drag Dismiss", R.color.lime_300),
        RX_LIFECYCLE("Rx Lifecycle", R.color.teal_300),
        RX_LIFECYCLE_2("Rx Lifecycle 2", R.color.brown_300);

        String title;
        @ColorRes int color;

        HomeDemoModel(String title, @ColorRes int color) {
            this.title = title;
            this.color = color;
        }
    }

    private static final String KEY_FAB_VISIBILITY = "HomeController.fabVisibility";

    @BindView(R.id.recycler_view) RecyclerView recyclerView;
    @BindView(R.id.fab) View fab;

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
    protected void onSaveViewState(@NonNull View view, @NonNull Bundle outState) {
        super.onSaveViewState(view, outState);
        outState.putInt(KEY_FAB_VISIBILITY, fab.getVisibility());
    }

    @Override
    protected void onRestoreViewState(@NonNull View view, @NonNull Bundle savedViewState) {
        super.onRestoreViewState(view, savedViewState);

        //noinspection WrongConstant
        fab.setVisibility(savedViewState.getInt(KEY_FAB_VISIBILITY));
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.about) {
            onFabClicked(false);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected String getTitle() {
        return "Conductor Demos";
    }

    @OnClick(R.id.fab)
    public void onFabClicked() {
        onFabClicked(true);
    }

    private void onFabClicked(boolean fromFab) {
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

        SpannableStringBuilder description = new SpannableStringBuilder();
        description.append(details);
        description.append("\n\n");
        description.append(link);

        ControllerChangeHandler pushHandler = fromFab ? new TransitionChangeHandlerCompat(new FabToDialogTransitionChangeHandler(), new FadeChangeHandler(false)) : new FadeChangeHandler(false);
        ControllerChangeHandler popHandler = fromFab ? new TransitionChangeHandlerCompat(new FabToDialogTransitionChangeHandler(), new FadeChangeHandler()) : new FadeChangeHandler();

        if (fromFab) {
            getRouter()
                    .pushController(RouterTransaction.with(new DialogController("Conductor", description))
                            .pushChangeHandler(pushHandler)
                            .popChangeHandler(popHandler));
        } else {
            new AlertDialogController("Conductor", description)
                    .showDialog(getRouter(), "about_dialog_tag");
        }

    }

    void onModelRowClick(HomeDemoModel model, int position) {
        switch (model) {
            case NAVIGATION:
                getRouter().pushController(RouterTransaction.with(new NavigationDemoController(0, DisplayUpMode.SHOW_FOR_CHILDREN_ONLY))
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
            case SHARED_ELEMENT_TRANSITIONS:
                getRouter().pushController(RouterTransaction.with(new CityGridController(model.title, model.color, position))
                        .pushChangeHandler(new TransitionChangeHandlerCompat(new ArcFadeMoveChangeHandler(), new FadeChangeHandler()))
                        .popChangeHandler(new TransitionChangeHandlerCompat(new ArcFadeMoveChangeHandler(), new FadeChangeHandler())));
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
            case RX_LIFECYCLE_2:
                getRouter().pushController(RouterTransaction.with(new RxLifecycle2Controller())
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
            holder.bind(position, items[position]);
        }

        @Override
        public int getItemCount() {
            return items.length;
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.tv_title) TextView tvTitle;
            @BindView(R.id.img_dot) ImageView imgDot;
            private HomeDemoModel model;
            private int position;

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }

            void bind(int position, HomeDemoModel item) {
                model = item;
                tvTitle.setText(item.title);
                imgDot.getDrawable().setColorFilter(ContextCompat.getColor(getActivity(), item.color), Mode.SRC_ATOP);
                this.position = position;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    tvTitle.setTransitionName(getResources().getString(R.string.transition_tag_title_indexed, position));
                    imgDot.setTransitionName(getResources().getString(R.string.transition_tag_dot_indexed, position));
                }
            }

            @OnClick(R.id.row_root)
            void onRowClick() {
                onModelRowClick(model, position);
            }

        }
    }

}
