package com.bluelinelabs.conductor.demo.controllers.bottomnavigation.children;

import android.os.Bundle;

import android.annotation.SuppressLint;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.demo.R;
import com.bluelinelabs.conductor.demo.controllers.base.BaseController;
import com.bluelinelabs.conductor.demo.util.BundleBuilder;

import butterknife.BindView;
import butterknife.OnClick;
/**
 * The {@link com.bluelinelabs.conductor.Controller} for showing the depth and instance
 * @author chris6647@gmail.com
 */
public class DepthChildController extends BaseController {

    public static final String TAG = "DepthChildController";

    @BindView(R.id.message)
    TextView textMessage;

    @OnClick(R.id.deeper)
    void onClicked(){
        getRouter().pushController(RouterTransaction.with(new DepthChildController(getColor(), getDepth()+1)));
    }

    public DepthChildController(@ColorInt int color, int depth) {
        this(new BundleBuilder(new Bundle()).putInt("color", color).putInt("depth", depth).build());
    }

    public DepthChildController(Bundle args) {
        super(args);
    }

    @ColorInt int getColor(){
        return getArgs().getInt("color");
    }

    int getDepth(){
        return getArgs().getInt("depth");
    }

    @NonNull
    @Override
    protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        return inflater.inflate(R.layout.controller_depth_child, container, false);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onViewBound(@NonNull View view) {
        super.onViewBound(view);
        view.setBackgroundColor(getColor());
        textMessage.setText("depth="+getDepth() + " | " +this.toString());
    }
}
