package com.bluelinelabs.conductor.demo.kotlin.controllers.base

import android.os.Bundle

import com.bluelinelabs.conductor.ControllerChangeHandler
import com.bluelinelabs.conductor.ControllerChangeType
import com.bluelinelabs.conductor.demo.kotlin.DemoApplication

abstract class RefWatchingController : ButterKnifeController {

    private var hasExited: Boolean = false

    protected constructor()
    protected constructor(args: Bundle) : super(args)

    public override fun onDestroy() {
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
}
