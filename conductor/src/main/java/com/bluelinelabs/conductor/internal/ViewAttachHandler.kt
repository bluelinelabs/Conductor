package com.bluelinelabs.conductor.internal

import android.view.View
import android.view.ViewGroup

class ViewAttachHandler(private val attachListener: ViewAttachListener) : View.OnAttachStateChangeListener {
    private enum class ReportedState {
        VIEW_DETACHED, ACTIVITY_STOPPED, ATTACHED
    }

    interface ViewAttachListener {
        fun onAttached()
        fun onDetached(fromActivityStop: Boolean)
        fun onViewDetachAfterStop()
    }

    private interface ChildAttachListener {
        fun onAttached()
    }

    private var rootAttached = false
    private var activityStopped = false
    private var reportedState = ReportedState.VIEW_DETACHED

    var childrenAttached = false
    var childOnAttachStateChangeListener: View.OnAttachStateChangeListener? = null

    override fun onViewAttachedToWindow(v: View) {
        if (rootAttached) {
            return
        }
        rootAttached = true
        listenForDeepestChildAttach(v, object : ChildAttachListener {
            override fun onAttached() {
                childrenAttached = true
                reportAttached()
            }
        })
    }

    override fun onViewDetachedFromWindow(v: View) {
        rootAttached = false
        if (childrenAttached) {
            childrenAttached = false
            reportDetached(false)
        }
    }

    fun listenForAttach(view: View) {
        view.addOnAttachStateChangeListener(this)
    }

    fun unregisterAttachListener(view: View) {
        view.removeOnAttachStateChangeListener(this)
        val childOnAttachStateChangeListener = childOnAttachStateChangeListener ?: return
        if (view is ViewGroup) {
            findDeepestChild(view).removeOnAttachStateChangeListener(childOnAttachStateChangeListener)
            this.childOnAttachStateChangeListener = null
        }
    }

    fun onActivityStarted() {
        activityStopped = false
        reportAttached()
    }

    fun onActivityStopped() {
        activityStopped = true
        reportDetached(true)
    }

    fun reportAttached() {
        if (rootAttached && childrenAttached && !activityStopped && reportedState != ReportedState.ATTACHED) {
            reportedState = ReportedState.ATTACHED
            attachListener.onAttached()
        }
    }

    private fun reportDetached(detachedForActivity: Boolean) {
        val wasDetachedForActivity = reportedState == ReportedState.ACTIVITY_STOPPED

        reportedState = if (detachedForActivity) ReportedState.ACTIVITY_STOPPED else ReportedState.VIEW_DETACHED

        if (wasDetachedForActivity && !detachedForActivity) {
            attachListener.onViewDetachAfterStop()
        } else {
            attachListener.onDetached(detachedForActivity)
        }
    }

    private fun listenForDeepestChildAttach(view: View, attachListener: ChildAttachListener) {
        if (view !is ViewGroup) {
            attachListener.onAttached()
            return
        }

        if (view.childCount == 0) {
            attachListener.onAttached()
            return
        }

        childOnAttachStateChangeListener = object : View.OnAttachStateChangeListener {
            var attached = false
            override fun onViewAttachedToWindow(v: View) {
                if (!attached && childOnAttachStateChangeListener != null) {
                    attached = true
                    attachListener.onAttached()
                    v.removeOnAttachStateChangeListener(this)
                    childOnAttachStateChangeListener = null
                }
            }

            override fun onViewDetachedFromWindow(v: View) {}
        }
        findDeepestChild(view).addOnAttachStateChangeListener(childOnAttachStateChangeListener)
    }

    private fun findDeepestChild(viewGroup: ViewGroup): View {
        if (viewGroup.childCount == 0) {
            return viewGroup
        }
        val lastChild = viewGroup.getChildAt(viewGroup.childCount - 1)
        return if (lastChild is ViewGroup) {
            findDeepestChild(lastChild)
        } else {
            lastChild
        }
    }

}
