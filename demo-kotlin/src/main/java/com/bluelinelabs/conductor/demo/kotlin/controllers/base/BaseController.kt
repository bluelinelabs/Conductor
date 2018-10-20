package com.bluelinelabs.conductor.demo.kotlin.controllers.base

import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.view.View

import com.bluelinelabs.conductor.demo.kotlin.ActionBarProvider

abstract class BaseController : RefWatchingController {

    // Note: This is just a quick demo of how an ActionBar *can* be accessed, not necessarily how it *should*
    // be accessed. In a production app, this would use Dagger instead.
    private val actionBar: ActionBar?
        get() {
            return (activity as AppCompatActivity).supportActionBar
        }

    open val title: String?
        get() = null

    protected constructor()

    protected constructor(args: Bundle) : super(args)

    override fun onAttach(view: View) {
        setTitle()
        super.onAttach(view)
    }

    protected fun setTitle() {
        var parentController = parentController
        while (parentController != null) {
            if (parentController is BaseController && parentController.title != null) {
                return
            }
            parentController = parentController.parentController
        }

        val title = title
        val actionBar = actionBar
        if (title != null && actionBar != null) {
            actionBar.title = title
        }
    }
}
