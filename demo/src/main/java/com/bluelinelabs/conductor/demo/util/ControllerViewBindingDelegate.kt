package com.bluelinelabs.conductor.demo.util

import android.view.View
import androidx.lifecycle.LifecycleObserver
import androidx.viewbinding.ViewBinding
import com.bluelinelabs.conductor.Controller
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

fun <T : ViewBinding> Controller.viewBinding(bindingFactory: (View) -> T) =
  ControllerViewBindingDelegate(this, bindingFactory)

class ControllerViewBindingDelegate<T : ViewBinding>(
  controller: Controller,
  private val viewBinder: (View) -> T
) : ReadOnlyProperty<Controller, T>, LifecycleObserver {

  private var binding: T? = null

  init {
    controller.addLifecycleListener(object : Controller.LifecycleListener() {
      override fun postDestroyView(controller: Controller) {
        binding = null
      }
    })
  }

  override fun getValue(thisRef: Controller, property: KProperty<*>): T {
    return binding ?: viewBinder(thisRef.view!!).also { binding = it }
  }
}
