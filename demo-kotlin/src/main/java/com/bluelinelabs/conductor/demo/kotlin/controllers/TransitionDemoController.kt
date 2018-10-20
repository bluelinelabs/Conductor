package com.bluelinelabs.conductor.demo.kotlin.controllers

import android.content.res.ColorStateList
import android.os.Bundle
import android.support.annotation.ColorRes
import android.support.annotation.LayoutRes
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.ControllerChangeHandler
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler
import com.bluelinelabs.conductor.changehandler.VerticalChangeHandler
import com.bluelinelabs.conductor.demo.kotlin.R
import com.bluelinelabs.conductor.demo.kotlin.changehandler.ArcFadeMoveChangeHandlerCompat
import com.bluelinelabs.conductor.demo.kotlin.changehandler.CircularRevealChangeHandlerCompat
import com.bluelinelabs.conductor.demo.kotlin.changehandler.FlipChangeHandler
import com.bluelinelabs.conductor.demo.kotlin.controllers.base.BaseController
import com.bluelinelabs.conductor.demo.kotlin.util.BundleBuilder

class TransitionDemoController : BaseController {

    @BindView(R.id.tv_title)
    internal var tvTitle: TextView? = null
    @BindView(R.id.btn_next)
    internal var btnNext: FloatingActionButton? = null
    @BindView(R.id.transition_root)
    internal var containerView: View? = null

    private val transitionDemo: TransitionDemo

    override val title: String
        get() = "Transition Demos"

    enum class TransitionDemo private constructor(internal var title: String, @param:LayoutRes internal var layoutId: Int, @param:ColorRes internal var colorId: Int) {
        VERTICAL("Vertical Slide Animation", R.layout.controller_transition_demo, R.color.blue_grey_300),
        CIRCULAR("Circular Reveal Animation (on Lollipop and above, else Fade)", R.layout.controller_transition_demo, R.color.red_300),
        FADE("Fade Animation", R.layout.controller_transition_demo, R.color.blue_300),
        FLIP("Flip Animation", R.layout.controller_transition_demo, R.color.deep_orange_300),
        HORIZONTAL("Horizontal Slide Animation", R.layout.controller_transition_demo, R.color.green_300),
        ARC_FADE("Arc/Fade Shared Element Transition (on Lollipop and above, else Fade)", R.layout.controller_transition_demo_shared, 0),
        ARC_FADE_RESET("Arc/Fade Shared Element Transition (on Lollipop and above, else Fade)", R.layout.controller_transition_demo, R.color.pink_300);


        companion object {

            fun fromIndex(index: Int): TransitionDemo {
                return TransitionDemo.values()[index]
            }
        }
    }

    constructor(index: Int) : this(BundleBuilder(Bundle())
            .putInt(KEY_INDEX, index)
            .build())

    constructor(args: Bundle) : super(args) {
        transitionDemo = TransitionDemo.fromIndex(args.getInt(KEY_INDEX))
    }

    override fun inflateView(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(transitionDemo.layoutId, container, false)
    }

    override fun onViewBound(view: View) {
        super.onViewBound(view)

        val bgView = ButterKnife.findById<View>(view, R.id.bg_view)
        if (transitionDemo.colorId != 0 && bgView != null) {
            bgView.setBackgroundColor(ContextCompat.getColor(activity!!, transitionDemo.colorId))
        }

        val nextIndex = transitionDemo.ordinal + 1
        var buttonColor = 0
        if (nextIndex < TransitionDemo.values().size) {
            buttonColor = TransitionDemo.fromIndex(nextIndex).colorId
        }
        if (buttonColor == 0) {
            buttonColor = TransitionDemo.fromIndex(0).colorId
        }

        btnNext!!.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(activity!!, buttonColor))
        tvTitle!!.text = transitionDemo.title
    }

    @OnClick(R.id.btn_next)
    internal fun onNextClicked() {
        val nextIndex = transitionDemo.ordinal + 1

        if (nextIndex < TransitionDemo.values().size) {
            router.pushController(getRouterTransaction(nextIndex, this))
        } else {
            router.popToRoot()
        }
    }

    fun getChangeHandler(from: Controller): ControllerChangeHandler {
        return when (transitionDemo) {
            TransitionDemoController.TransitionDemo.VERTICAL -> VerticalChangeHandler()
            TransitionDemoController.TransitionDemo.CIRCULAR -> {
                val demoController = from as TransitionDemoController
                CircularRevealChangeHandlerCompat(demoController.btnNext!!, demoController.containerView!!)
            }
            TransitionDemoController.TransitionDemo.FADE -> FadeChangeHandler()
            TransitionDemoController.TransitionDemo.FLIP -> FlipChangeHandler()
            TransitionDemoController.TransitionDemo.ARC_FADE -> ArcFadeMoveChangeHandlerCompat(from.resources!!.getString(R.string.transition_tag_dot), from.resources!!.getString(R.string.transition_tag_title))
            TransitionDemoController.TransitionDemo.ARC_FADE_RESET -> ArcFadeMoveChangeHandlerCompat(from.resources!!.getString(R.string.transition_tag_dot), from.resources!!.getString(R.string.transition_tag_title))
            TransitionDemoController.TransitionDemo.HORIZONTAL -> HorizontalChangeHandler()
        }
    }

    companion object {

        private val KEY_INDEX = "TransitionDemoController.index"

        fun getRouterTransaction(index: Int, fromController: Controller): RouterTransaction {
            val toController = TransitionDemoController(index)

            return RouterTransaction.with(toController)
                    .pushChangeHandler(toController.getChangeHandler(fromController))
                    .popChangeHandler(toController.getChangeHandler(fromController))
        }
    }

}
