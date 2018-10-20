package com.bluelinelabs.conductor.demo.kotlin.controllers

import android.arch.lifecycle.Lifecycle.Event
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.OnLifecycleEvent
import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.Unbinder
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.ControllerChangeHandler
import com.bluelinelabs.conductor.ControllerChangeType
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.archlifecycle.LifecycleController
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler
import com.bluelinelabs.conductor.demo.kotlin.ActionBarProvider
import com.bluelinelabs.conductor.demo.kotlin.DemoApplication
import com.bluelinelabs.conductor.demo.kotlin.R

class ArchLifecycleController : LifecycleController() {

    @BindView(R.id.tv_title)
    internal var tvTitle: TextView? = null

    private var unbinder: Unbinder? = null
    private var hasExited: Boolean = false

    init {
        Log.i(TAG, "Conductor: Constructor called")

        lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Event.ON_ANY)
            fun onLifecycleEvent(source: LifecycleOwner, event: Event) {
                Log.d(TAG, "Lifecycle: " + source.javaClass.simpleName + " emitted event " + event + " and is now in state " + source.lifecycle.currentState)
            }
        })

        Log.d(TAG, "Lifecycle: " + javaClass.simpleName + " is now in state " + lifecycle.currentState)
    }

    override fun onContextAvailable(context: Context) {
        Log.i(TAG, "Conductor: onContextAvailable() called")
        super.onContextAvailable(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        Log.i(TAG, "Conductor: onCreateView() called")

        val view = inflater.inflate(R.layout.controller_lifecycle, container, false)
        view.setBackgroundColor(ContextCompat.getColor(container.context, R.color.orange_300))
        unbinder = ButterKnife.bind(this, view)

        tvTitle!!.text = resources!!.getString(R.string.rxlifecycle_title, TAG)

        return view
    }

    override fun onAttach(view: View) {
        Log.i(TAG, "Conductor: onAttach() called")
        super.onAttach(view)

        (activity as ActionBarProvider).supportActionBar?.title = "Arch Components Lifecycle Demo"
    }

    override fun onDetach(view: View) {
        Log.i(TAG, "Conductor: onDetach() called")
        super.onDetach(view)
    }

    override fun onDestroyView(view: View) {
        Log.i(TAG, "Conductor: onDestroyView() called")
        super.onDestroyView(view)

        unbinder!!.unbind()
        unbinder = null
    }

    override fun onContextUnavailable() {
        Log.i(TAG, "Conductor: onContextUnavailable() called")
        super.onContextUnavailable()
    }

    public override fun onDestroy() {
        Log.i(TAG, "Conductor: onDestroy() called")
        super.onDestroy()

        if (hasExited) {
            DemoApplication.refWatcher.watch(this)
        }
    }

    override fun onChangeEnded(changeHandler: ControllerChangeHandler, changeType: ControllerChangeType) {
        super.onChangeEnded(changeHandler, changeType)

        hasExited = !changeType.isEnter
        if (isDestroyed) {
            DemoApplication.refWatcher.watch(this)
        }
    }

    @OnClick(R.id.btn_next_release_view)
    internal fun onNextWithReleaseClicked() {
        retainViewMode = Controller.RetainViewMode.RELEASE_DETACH

        router.pushController(RouterTransaction.with(TextController("Logcat should now report that the Controller's onDetach() and LifecycleObserver's onPause() methods were called, followed by the Controller's onDestroyView() and LifecycleObserver's onStop()."))
                .pushChangeHandler(HorizontalChangeHandler())
                .popChangeHandler(HorizontalChangeHandler()))
    }

    @OnClick(R.id.btn_next_retain_view)
    internal fun onNextWithRetainClicked() {
        retainViewMode = Controller.RetainViewMode.RETAIN_DETACH

        router.pushController(RouterTransaction.with(TextController("Logcat should now report that the Controller's onDetach() and LifecycleObserver's onPause() methods were called."))
                .pushChangeHandler(HorizontalChangeHandler())
                .popChangeHandler(HorizontalChangeHandler()))
    }

    companion object {

        private val TAG = "ArchLifecycleController"
    }

}
