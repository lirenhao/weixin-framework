package com.yada.comm.util

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock

/**
  * Created by cuitao-pc on 16/3/10.
  */
class FullLock(capacity: Int) {
  val rl = new ReentrantLock
  val c = rl.newCondition()
  val count = new AtomicInteger()

  def increment(): Unit = {
    var cur = -1
    rl.lockInterruptibly()
    while (count.get() == capacity) {
      c.await()
    }
    cur = count.incrementAndGet()
    if (cur < capacity) c.signal()
    rl.unlock()
  }

  def decrement(): Unit = {
    val cur = count.getAndDecrement()
    if(cur == capacity) {
      rl.lockInterruptibly()
      c.signal()
      rl.unlock()
    }
  }
}

object FullLock {
  def apply(capacity: Int) = new FullLock(capacity)
}