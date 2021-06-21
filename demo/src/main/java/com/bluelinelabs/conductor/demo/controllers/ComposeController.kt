package com.bluelinelabs.conductor.demo.controllers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.asTransaction
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler
import com.bluelinelabs.conductor.demo.ToolbarProvider

class ComposeController : Controller() {

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup,
    savedViewState: Bundle?
  ): View {
    return ComposeView(container.context).apply {
      setContent {
        LongNumberList {
          router.pushController(
            ComposeController().asTransaction(
              pushChangeHandler = HorizontalChangeHandler(),
              popChangeHandler = HorizontalChangeHandler()
            )
          )
        }
      }
    }
  }

  override fun onAttach(view: View) {
    super.onAttach(view)

    (activity as? ToolbarProvider)?.toolbar?.apply {
      title = "Jetpack Compose Demo"
      menu.clear()
    }
  }
}

@Composable
fun LongNumberList(onClick: () -> Unit) {
  MaterialTheme {
    LazyColumn {
      item {
        Spacer(modifier = Modifier.height(8.dp))
      }
      items((0..100).toList()) {
        Box(
          modifier = Modifier
            .height(48.dp)
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp)
        ) {
          Text(
            modifier = Modifier
              .align(Alignment.CenterStart),
            text = "Line $it"
          )
        }
      }
      item {
        Spacer(modifier = Modifier.height(8.dp))
      }
    }
  }
}
