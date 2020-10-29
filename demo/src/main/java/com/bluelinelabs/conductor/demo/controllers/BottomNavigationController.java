package com.bluelinelabs.conductor.demo.controllers;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.Router;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.demo.R;
import com.bluelinelabs.conductor.demo.controllers.base.BaseController;
import com.bluelinelabs.conductor.demo.widget.NonScrollableViewPager;
import com.bluelinelabs.conductor.viewpager.RouterPagerAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import butterknife.BindView;

public class BottomNavigationController extends BaseController {

    @BindView(R.id.view_pager) NonScrollableViewPager viewPager;
    @BindView(R.id.navigation) BottomNavigationView navigation;

    private final RouterPagerAdapter pagerAdapter;

    public BottomNavigationController() {
        pagerAdapter = new RouterPagerAdapter(this) {
            @Override
            public void configureRouter(@NonNull Router router, int position) {
                if (!router.hasRootController()) {
                    Controller page = new NavigationDemoController(position * 10, NavigationDemoController.DisplayUpMode.HIDE);
                    router.setRoot(RouterTransaction.with(page));
                }
            }

            @Override
            public int getCount() {
                return 4;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return "Tab " + position;
            }
        };
    }

    @Override
    protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        return inflater.inflate(R.layout.controller_bottom_navigation, container, false);
    }

    @Override
    protected void onViewBound(@NonNull View view) {
        super.onViewBound(view);
        viewPager.setAdapter(pagerAdapter);
        navigation.setOnNavigationItemSelectedListener(menuItem -> {
            int index = 0;
            switch (menuItem.getItemId()) {
                case R.id.home:
                    index = 0;
                    break;
                case R.id.places:
                    index = 1;
                    break;
                case R.id.favorite:
                    index = 2;
                    break;
                case R.id.settings:
                    index = 3;
                    break;
            }
            viewPager.setCurrentItem(index);
            return true;
        });
    }

    @Override
    protected void onDestroyView(@NonNull View view) {
        viewPager.setAdapter(null);
        super.onDestroyView(view);
    }
}
