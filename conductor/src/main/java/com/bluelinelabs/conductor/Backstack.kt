package com.bluelinelabs.conductor

import android.os.Bundle
import java.util.ArrayDeque
import java.util.Deque

import com.bluelinelabs.conductor.internal.parcelableArrayListCompat

internal class Backstack : Iterable<RouterTransaction> {

  private val backstack: Deque<RouterTransaction> = ArrayDeque()

  val isEmpty: Boolean get() = backstack.isEmpty()

  val size: Int get() = backstack.size

  var onBackstackUpdatedListener: OnBackstackUpdatedListener? = null

  fun root(): RouterTransaction? = backstack.lastOrNull()

  override fun iterator(): Iterator<RouterTransaction> = backstack.toTypedArray().iterator()

  fun reverseIterator(): Iterator<RouterTransaction> = backstack.reversed().iterator()

  fun remove(transaction: RouterTransaction) = backstack.remove(transaction)

  fun popTo(transaction: RouterTransaction): List<RouterTransaction> {
    if (transaction in backstack) {
      val popped: MutableList<RouterTransaction> = ArrayList()
      while (backstack.peek() != transaction) {
        val poppedTransaction = pop()
        popped.add(poppedTransaction)
      }
      return popped
    } else {
      throw RuntimeException("Tried to pop to a transaction that was not on the back stack")
    }
  }

  fun pop(): RouterTransaction {
    return backstack.pop().also {
      onBackstackUpdatedListener?.onBackstackUpdated()
      it.controller.destroy()
    }
  }

  fun peek(): RouterTransaction? = backstack.peek()

  fun push(transaction: RouterTransaction) {
    backstack.push(transaction)
    onBackstackUpdatedListener?.onBackstackUpdated()
  }

  fun popAll(): List<RouterTransaction> {
    val list: MutableList<RouterTransaction> = ArrayList()
    while (!isEmpty) {
      list.add(pop())
    }
    return list
  }

  fun setBackstack(backstack: List<RouterTransaction>) {
    this.backstack.clear()
    backstack.forEach { transaction ->
      this.backstack.push(transaction)
    }

    onBackstackUpdatedListener?.onBackstackUpdated()
  }

  operator fun contains(controller: Controller): Boolean {
    return backstack.any {
      it.controller == controller
    }
  }

  fun saveInstanceState(outState: Bundle) {
    val entryBundles = ArrayList<Bundle>(backstack.size)
    backstack.mapTo(entryBundles) {
      it.saveInstanceState()
    }
    outState.putParcelableArrayList(KEY_ENTRIES, entryBundles)
  }

  fun restoreInstanceState(savedInstanceState: Bundle) {
    val entryBundles = savedInstanceState.parcelableArrayListCompat<Bundle>(KEY_ENTRIES)

    entryBundles?.reversed()?.forEach { bundle ->
        backstack.push(RouterTransaction(bundle))
    }

    onBackstackUpdatedListener?.onBackstackUpdated()
  }

  fun interface OnBackstackUpdatedListener {
    fun onBackstackUpdated()
  }

  companion object {
    private const val KEY_ENTRIES = "Backstack.entries"
  }
}
