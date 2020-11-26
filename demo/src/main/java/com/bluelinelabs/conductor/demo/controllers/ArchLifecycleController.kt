package com.bluelinelabs.conductor.demo.controllers

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.bluelinelabs.conductor.ControllerChangeHandler
import com.bluelinelabs.conductor.ControllerChangeType
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.archlifecycle.LifecycleController
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler
import com.bluelinelabs.conductor.demo.DemoApplication
import com.bluelinelabs.conductor.demo.R
import com.bluelinelabs.conductor.demo.ToolbarProvider
import com.bluelinelabs.conductor.demo.databinding.ControllerLifecycleBinding

class ArchLifecycleController : LifecycleController() {

  private var hasExited = false

  init {
    Log.i(TAG, "Conductor: Constructor called")

    lifecycle.addObserver(object : LifecycleObserver {
      @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
      fun onLifecycleEvent(source: LifecycleOwner, event: Lifecycle.Event) {
        Log.d(
          TAG,
          "Lifecycle: " + source.javaClass.simpleName + " emitted event " + event + " and is now in state " + source.lifecycle.currentState
        )
      }
    })

    Log.d(TAG, "Lifecycle: " + javaClass.simpleName + " is now in state " + lifecycle.currentState)
  }

  override fun onContextAvailable(context: Context) {
    Log.i(TAG, "Conductor: onContextAvailable() called")
    super.onContextAvailable(context)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup,
    savedViewState: Bundle?
  ): View {
    Log.i(TAG, "Conductor: onCreateView() called")

    val binding = ControllerLifecycleBinding.inflate(inflater, container, false)

    binding.root.setBackgroundColor(ContextCompat.getColor(container.context, R.color.orange_300))
    binding.title.text = binding.root.resources.getString(R.string.rxlifecycle_title, TAG)

    binding.nextReleaseView.setOnClickListener {
      retainViewMode = RetainViewMode.RELEASE_DETACH

      router.pushController(
        RouterTransaction.with(TextController("Logcat should now report that the Controller's onDetach() and LifecycleObserver's onPause() methods were called, followed by the Controller's onDestroyView() and LifecycleObserver's onStop()."))
          .pushChangeHandler(HorizontalChangeHandler())
          .popChangeHandler(HorizontalChangeHandler())
      )
    }

    binding.nextRetainView.setOnClickListener {
      retainViewMode = RetainViewMode.RETAIN_DETACH

      router.pushController(
        RouterTransaction.with(TextController("Logcat should now report that the Controller's onDetach() and LifecycleObserver's onPause() methods were called."))
          .pushChangeHandler(HorizontalChangeHandler())
          .popChangeHandler(HorizontalChangeHandler())
      )
    }

    return binding.root
  }

  override fun onAttach(view: View) {
    Log.i(TAG, "Conductor: onAttach() called")
    super.onAttach(view)
    (activity as ToolbarProvider).toolbar.title = "Arch Components Lifecycle Demo"
  }

  override fun onDetach(view: View) {
    Log.i(TAG, "Conductor: onDetach() called")
    super.onDetach(view)
  }

  override fun onDestroyView(view: View) {
    Log.i(TAG, "Conductor: onDestroyView() called")
    super.onDestroyView(view)
  }

  override fun onContextUnavailable() {
    Log.i(TAG, "Conductor: onContextUnavailable() called")
    super.onContextUnavailable()
  }

  override fun onDestroy() {
    Log.i(TAG, "Conductor: onDestroy() called")
    super.onDestroy()
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
    private const val TAG = "ArchLifecycleController"
  }
}