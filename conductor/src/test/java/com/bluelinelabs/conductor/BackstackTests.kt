package com.bluelinelabs.conductor

import org.junit.Assert.assertEquals
import org.junit.Test

class BackstackTests {

  private val backstack = Backstack()

  @Test
  fun testPush() {
    assertEquals(0, backstack.size.toLong())
    backstack.push(TestController().asTransaction())
    assertEquals(1, backstack.size.toLong())
  }

  @Test
  fun testPop() {
    backstack.push(TestController().asTransaction())
    backstack.push(TestController().asTransaction())
    assertEquals(2, backstack.size.toLong())

    backstack.pop()
    assertEquals(1, backstack.size.toLong())

    backstack.pop()
    assertEquals(0, backstack.size.toLong())
  }

  @Test
  fun testPeek() {
    val transaction1 = TestController().asTransaction()
    val transaction2 = TestController().asTransaction()

    backstack.push(transaction1)
    assertEquals(transaction1, backstack.peek())

    backstack.push(transaction2)
    assertEquals(transaction2, backstack.peek())

    backstack.pop()
    assertEquals(transaction1, backstack.peek())
  }

  @Test
  fun testPopTo() {
    val transaction1 = TestController().asTransaction()
    val transaction2 = TestController().asTransaction()
    val transaction3 = TestController().asTransaction()

    backstack.push(transaction1)
    backstack.push(transaction2)
    backstack.push(transaction3)
    assertEquals(3, backstack.size.toLong())

    backstack.popTo(transaction1)
    assertEquals(1, backstack.size.toLong())
    assertEquals(transaction1, backstack.peek())
  }
}