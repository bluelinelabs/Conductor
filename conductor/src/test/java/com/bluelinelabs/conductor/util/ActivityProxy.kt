package com.bluelinelabs.conductor.util

import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
import android.os.Bundle
import androidx.annotation.IdRes
import com.bluelinelabs.conductor.R
import org.robolectric.Robolectric

class ActivityProxy {

  @IdRes
  private val containerId = 4
  private val activityController = Robolectric.buildActivity(TestActivity::class.java).apply {
    get().setTheme(R.style.Theme_AppCompat)
  }

  val activity: TestActivity
    get() = activityController.get()

  var view: AttachFakingFrameLayout = AttachFakingFrameLayout(activityController.get())
    .also { it.id = containerId }
    set(value) {
      value.id = containerId
      field = value
    }

  fun create(savedInstanceState: Bundle?): ActivityProxy {
    activityController.create(savedInstanceState)
    return this
  }

  fun start(): ActivityProxy {
    activityController.start()
    view.setAttached(true)
    return this
  }

  fun resume(): ActivityProxy {
    activityController.resume()
    return this
  }

  fun pause(): ActivityProxy {
    activityController.pause()
    return this
  }

  fun saveInstanceState(outState: Bundle?): ActivityProxy {
    activityController.saveInstanceState(outState)
    return this
  }

  fun stop(detachView: Boolean): ActivityProxy {
    activityController.stop()
    if (detachView) {
      view.setAttached(false)
    }
    return this
  }

  fun destroy(): ActivityProxy {
    activityController.destroy()
    view.setAttached(false)
    return this
  }

  fun rotate(): ActivityProxy {
    val requestedPortrait = activity.requestedOrientation == SCREEN_ORIENTATION_PORTRAIT
    activity.requestedOrientation = if (requestedPortrait) {
      SCREEN_ORIENTATION_LANDSCAPE
    } else {
      SCREEN_ORIENTATION_PORTRAIT
    }
    return this
  }
}
