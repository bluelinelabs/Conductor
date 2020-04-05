package com.bluelinelabs.conductor.util;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bluelinelabs.conductor.ControllerChangeHandler;
import com.bluelinelabs.conductor.ControllerChangeType;
import com.bluelinelabs.conductor.MasterDetailController;

public class TestMasterDetailController extends MasterDetailController implements CallStateOwner, ChangeHandlerHistoryOwner {

    @IdRes public static final int VIEW_ID = 2442;
    @IdRes public static final int CHILD_VIEW_ID_1 = 2443;
    @IdRes public static final int CHILD_VIEW_ID_2 = 2444;

    private static final String KEY_CALL_STATE = "TestController.currentCallState";

    private CallState currentCallState = new CallState(false);
    private ChangeHandlerHistory changeHandlerHistory = new ChangeHandlerHistory();

    @NonNull
    @Override
    protected View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedViewState) {
        currentCallState.createViewCalls++;
        FrameLayout view = new AttachFakingFrameLayout(inflater.getContext());
        view.setId(VIEW_ID);

        FrameLayout childContainer1 = new AttachFakingFrameLayout(inflater.getContext());
        childContainer1.setId(CHILD_VIEW_ID_1);
        view.addView(childContainer1);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            FrameLayout childContainer2 = new AttachFakingFrameLayout(inflater.getContext());
            childContainer2.setId(CHILD_VIEW_ID_2);
            view.addView(childContainer2);
        }

        initRouters(view, CHILD_VIEW_ID_1, CHILD_VIEW_ID_2);
        return view;
    }

    @Override
    protected void onChangeStarted(@NonNull ControllerChangeHandler changeHandler, @NonNull ControllerChangeType changeType) {
        super.onChangeStarted(changeHandler, changeType);
        currentCallState.changeStartCalls++;
    }

    @Override
    protected void onChangeEnded(@NonNull ControllerChangeHandler changeHandler, @NonNull ControllerChangeType changeType) {
        super.onChangeEnded(changeHandler, changeType);
        currentCallState.changeEndCalls++;

        if (changeHandler instanceof MockChangeHandler) {
            MockChangeHandler mockHandler = (MockChangeHandler)changeHandler;
            changeHandlerHistory.addEntry(mockHandler.from, mockHandler.to, changeType.isPush, mockHandler);
        } else {
            changeHandlerHistory.isValidHistory = false;
        }
    }

    @Override
    protected void onContextAvailable(@NonNull Context context) {
        super.onContextAvailable(context);
        currentCallState.contextAvailableCalls++;
    }

    @Override
    protected void onContextUnavailable() {
        super.onContextUnavailable();
        currentCallState.contextUnavailableCalls++;
    }

    @Override
    protected void onAttach(@NonNull View view) {
        super.onAttach(view);
        currentCallState.attachCalls++;
    }

    @Override
    protected void onDetach(@NonNull View view) {
        super.onDetach(view);
        currentCallState.detachCalls++;
    }

    @Override
    protected void onDestroyView(@NonNull View view) {
        super.onDestroyView(view);
        currentCallState.destroyViewCalls++;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        currentCallState.destroyCalls++;
    }

    @Override
    protected void onSaveViewState(@NonNull View view, @NonNull Bundle outState) {
        super.onSaveViewState(view, outState);
        currentCallState.saveViewStateCalls++;
    }

    @Override
    protected void onRestoreViewState(@NonNull View view, @NonNull Bundle savedViewState) {
        super.onRestoreViewState(view, savedViewState);
        currentCallState.restoreViewStateCalls++;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        currentCallState.saveInstanceStateCalls++;

        outState.putParcelable(KEY_CALL_STATE, currentCallState);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        currentCallState = savedInstanceState.getParcelable(KEY_CALL_STATE);

        currentCallState.restoreInstanceStateCalls++;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        currentCallState.onActivityResultCalls++;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        currentCallState.onRequestPermissionsResultCalls++;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        currentCallState.createOptionsMenuCalls++;
    }

    @Override
    public ControllerChangeHandler getRootDetailPushHandler() {
        return MockChangeHandler.taggedHandler("push", true);
    }

    @Override
    public ControllerChangeHandler getRootDetailPopHandler() {
        return MockChangeHandler.taggedHandler("pop", true);
    }

    @Override
    public CallState currentCallState() {
        return currentCallState;
    }

    @Override
    public ChangeHandlerHistory changeHandlerHistory() {
        return changeHandlerHistory;
    }
}
