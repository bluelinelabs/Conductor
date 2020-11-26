package com.bluelinelabs.conductor.demo.util

import android.content.res.Resources
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.core.content.res.use
import com.bluelinelabs.conductor.demo.R

@ColorInt
fun Resources.getMaterialColor(index: Int): Int {
  return obtainTypedArray(R.array.mdcolor_300).use {
    it.getColor(index % it.length(), Color.BLACK)
  }
}