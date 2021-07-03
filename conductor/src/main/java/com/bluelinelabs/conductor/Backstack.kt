package com.bluelinelabs.conductor

import android.os.Bundle
import java.util.*

internal class Backstack : Iterable<RouterTransaction> {

  private val backstack: Deque<RouterTransaction> = ArrayDeque()

  val isEmpty: Boolean get() = backstack.isEmpty()

  val size: Int get() = backstack.size

  fun root(): RouterTransaction? = backstack.lastOrNull()

  override fun iterator(): MutableIterator<RouterTransaction> {
    return backstack.iterator()
  }

  fun reverseIterator(): Iterator<RouterTransaction> = backstack.descendingIterator()

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
      it.controller.destroy()
    }
  }

  fun peek(): RouterTransaction? = backstack.peek()

  fun push(transaction: RouterTransaction) {
    backstack.push(transaction)
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
    val entryBundles = savedInstanceState.getParcelableArrayList<Bundle?>(KEY_ENTRIES)
    if (entryBundles != null) {
      entryBundles.reverse()
      for (transactionBundle in entryBundles) {
        backstack.push(RouterTransaction(transactionBundle!!))
      }
    }
  }

  companion object {
    private const val KEY_ENTRIES = "Backstack.entries"
  }
}