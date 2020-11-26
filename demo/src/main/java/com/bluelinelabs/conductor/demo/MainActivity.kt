package com.bluelinelabs.conductor.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bluelinelabs.conductor.Conductor.attachRouter
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction.Companion.with
import com.bluelinelabs.conductor.demo.controllers.HomeController
import com.bluelinelabs.conductor.demo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), ToolbarProvider {
  private lateinit var binding: ActivityMainBinding
  private lateinit var router: Router

  override val toolbar: Toolbar
    get() = binding.toolbar

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    router = attachRouter(this, binding.controllerContainer, savedInstanceState)
    if (!router.hasRootController()) {
      router.setRoot(with(HomeController()))
    }
  }

  override fun onBackPressed() {
    if (!router.handleBack()) {
      super.onBackPressed()
    }
  }
}