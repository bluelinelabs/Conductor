package com.bluelinelabs.conductor.demo.kotlin.controllers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import butterknife.BindView
import butterknife.OnClick
import com.bluelinelabs.conductor.ControllerChangeHandler
import com.bluelinelabs.conductor.ControllerChangeType
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler
import com.bluelinelabs.conductor.demo.kotlin.R
import com.bluelinelabs.conductor.demo.kotlin.controllers.base.BaseController
import com.bluelinelabs.conductor.demo.kotlin.util.BundleBuilder
import com.bluelinelabs.conductor.demo.kotlin.util.ColorUtil

class NavigationDemoController(args: Bundle) : BaseController(args) {

    @BindView(R.id.tv_title)
    internal var tvTitle: TextView? = null

    private val index: Int
    private val displayUpMode: DisplayUpMode

    override val title: String
        get() = "Navigation Demos"

    enum class DisplayUpMode {
        SHOW,
        SHOW_FOR_CHILDREN_ONLY,
        HIDE;

        val displayUpModeForChild: DisplayUpMode
            get() {
                return when (this) {
                    HIDE -> HIDE
                    else -> SHOW
                }
            }
    }

    constructor(index: Int, displayUpMode: DisplayUpMode) : this(BundleBuilder(Bundle())
            .putInt(KEY_INDEX, index)
            .putInt(KEY_DISPLAY_UP_MODE, displayUpMode.ordinal)
            .build())

    init {
        index = args.getInt(KEY_INDEX)
        displayUpMode = DisplayUpMode.values()[args.getInt(KEY_DISPLAY_UP_MODE)]
    }

    override fun inflateView(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(R.layout.controller_navigation_demo, container, false)
    }

    override fun onViewBound(view: View) {
        super.onViewBound(view)

        if (displayUpMode != DisplayUpMode.SHOW) {
            view.findViewById<Button>(R.id.btn_up).visibility = View.GONE
        }

        view.setBackgroundColor(ColorUtil.getMaterialColor(resources!!, index))
        tvTitle?.text = resources?.getString(R.string.navigation_title, index)
    }

    override fun onChangeEnded(changeHandler: ControllerChangeHandler, changeType: ControllerChangeType) {
        super.onChangeEnded(changeHandler, changeType)

        setButtonsEnabled(true)
    }

    override fun onChangeStarted(changeHandler: ControllerChangeHandler, changeType: ControllerChangeType) {
        super.onChangeStarted(changeHandler, changeType)

        setButtonsEnabled(false)
    }

    private fun setButtonsEnabled(enabled: Boolean) {
        view?.apply {
            findViewById<Button>(R.id.btn_next).isEnabled = enabled
            findViewById<Button>(R.id.btn_up).isEnabled = enabled
            findViewById<Button>(R.id.btn_pop_to_root).isEnabled = enabled
        }
    }

    @OnClick(R.id.btn_next)
    internal fun onNextClicked() {
        router.pushController(RouterTransaction.with(NavigationDemoController(index + 1, displayUpMode.displayUpModeForChild))
                .pushChangeHandler(HorizontalChangeHandler())
                .popChangeHandler(HorizontalChangeHandler()))
    }

    @OnClick(R.id.btn_up)
    internal fun onUpClicked() {
        router.popToTag(TAG_UP_TRANSACTION)
    }

    @OnClick(R.id.btn_pop_to_root)
    internal fun onPopToRootClicked() {
        router.popToRoot()
    }

    companion object {

        val TAG_UP_TRANSACTION = "NavigationDemoController.up"

        private val KEY_INDEX = "NavigationDemoController.index"
        private val KEY_DISPLAY_UP_MODE = "NavigationDemoController.displayUpMode"
    }
}
