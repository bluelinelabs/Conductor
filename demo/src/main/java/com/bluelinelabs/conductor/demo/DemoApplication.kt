package com.bluelinelabs.conductor.demo

import android.app.Application
import com.squareup.leakcanary.LeakCanary
import com.squareup.leakcanary.RefWatcher

class DemoApplication : Application() {
  override fun onCreate() {
    super.onCreate()
    refWatcher = LeakCanary.install(this)
  }

  companion object {
    lateinit var refWatcher: RefWatcher
  }
}