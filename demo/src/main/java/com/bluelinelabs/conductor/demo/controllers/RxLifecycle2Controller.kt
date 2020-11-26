package com.bluelinelabs.conductor.demo.controllers

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.ControllerChangeHandler
import com.bluelinelabs.conductor.ControllerChangeType
import com.bluelinelabs.conductor.RouterTransaction.Companion.with
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler
import com.bluelinelabs.conductor.demo.DemoApplication
import com.bluelinelabs.conductor.demo.R
import com.bluelinelabs.conductor.demo.ToolbarProvider
import com.bluelinelabs.conductor.demo.databinding.ControllerLifecycleBinding
import com.bluelinelabs.conductor.rxlifecycle2.ControllerEvent
import com.bluelinelabs.conductor.rxlifecycle2.RxController
import io.reactivex.Observable
import java.util.concurrent.TimeUnit

// Shamelessly borrowed from the official RxLifecycle demo by Trello and adapted for Conductor Controllers
// instead of Activities or Fragments.
class RxLifecycle2Controller : RxController() {

  private var hasExited = false

  init {
    Observable.interval(1, TimeUnit.SECONDS)
      .doOnDispose { Log.i(TAG, "Disposing from constructor") }
      .compose(bindUntilEvent(ControllerEvent.DESTROY))
      .subscribe { num: Long ->
        Log.i(TAG, "Started in constructor, running until onDestroy(): $num")
      }
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup,
    savedViewState: Bundle?
  ): View {
    Log.i(TAG, "onCreateView() called")
    val binding = ControllerLifecycleBinding.inflate(inflater, container, false)
    binding.title.text = binding.root.resources.getString(R.string.rxlifecycle_title, TAG)

    binding.nextReleaseView.setOnClickListener {
      retainViewMode = RetainViewMode.RELEASE_DETACH
      router.pushController(
        with(TextController("Logcat should now report that the observables from onAttach() and onViewBound() have been disposed of, while the constructor observable is still running."))
          .pushChangeHandler(HorizontalChangeHandler())
          .popChangeHandler(HorizontalChangeHandler())
      )
    }

    binding.nextRetainView.setOnClickListener {
      retainViewMode = RetainViewMode.RETAIN_DETACH
      router.pushController(
        with(TextController("Logcat should now report that the observables from onAttach() has been disposed of, while the constructor and onViewBound() observables are still running."))
          .pushChangeHandler(HorizontalChangeHandler())
          .popChangeHandler(HorizontalChangeHandler())
      )
    }

    Observable.interval(1, TimeUnit.SECONDS)
      .doOnDispose { Log.i(TAG, "Disposing from onCreateView()") }
      .compose(bindUntilEvent(ControllerEvent.DESTROY_VIEW))
      .subscribe { num: Long ->
        Log.i(TAG, "Started in onCreateView(), running until onDestroyView(): $num")
      }

    return binding.root
  }

  override fun onAttach(view: View) {
    super.onAttach(view)
    Log.i(TAG, "onAttach() called")

    (activity as ToolbarProvider).toolbar.title = "RxLifecycle2 Demo"
    Observable.interval(1, TimeUnit.SECONDS)
      .doOnDispose { Log.i(TAG, "Disposing from onAttach()") }
      .compose(bindUntilEvent(ControllerEvent.DETACH))
      .subscribe { num: Long ->
        Log.i(TAG, "Started in onAttach(), running until onDetach(): $num")
      }
  }

  override fun onDestroyView(view: View) {
    super.onDestroyView(view)
    Log.i(TAG, "onDestroyView() called")
  }

  override fun onDetach(view: View) {
    super.onDetach(view)
    Log.i(TAG, "onDetach() called")
  }

  public override fun onDestroy() {
    super.onDestroy()
    Log.i(TAG, "onDestroy() called")
    if (hasExited) {
      DemoApplication.refWatcher.watch(this)
    }
  }

  override fun onChangeEnded(
    changeHandler: ControllerChangeHandler,
    changeType: ControllerChangeType
  ) {
    super.onChangeEnded(changeHandler, changeType)
    hasExited = !changeType.isEnter
    if (isDestroyed) {
      DemoApplication.refWatcher.watch(this)
    }
  }

  companion object {
    private const val TAG = "RxLifecycleController"
  }

}