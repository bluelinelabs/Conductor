package com.bluelinelabs.conductor.demo.controllers.base

import android.os.Bundle
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.ControllerChangeHandler
import com.bluelinelabs.conductor.ControllerChangeType
import com.bluelinelabs.conductor.demo.DemoApplication

abstract class RefWatchingController(args: Bundle?) : Controller(args) {

  private var hasExited = false

  public override fun onDestroy() {
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
}