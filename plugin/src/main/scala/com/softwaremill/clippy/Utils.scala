package com.softwaremill.clippy

import java.io.{ByteArrayOutputStream, InputStream}

object Utils {
  /**
   * All future callbacks will be running on a daemon thread pool which can be interrupted at any time if the JVM
   * exits, if the compiler finished its job.
   *
   * Here we are trying to make as sure as possible (unless the JVM crashes) that we'll run the given code.
   */
  def runNonDaemon(t: => Unit) = {
    val shutdownHook = new Thread() {
      private val lock = new Object
      @volatile private var didRun = false

      override def run() = {
        lock.synchronized {
          if (!didRun) {
            t
            didRun = true
          }
        }
      }
    }

    Runtime.getRuntime.addShutdownHook(shutdownHook)
    shutdownHook.run()
    Runtime.getRuntime.removeShutdownHook(shutdownHook)
  }

  def inputStreamToBytes(is: InputStream): Array[Byte] = {
    try {
      val baos = new ByteArrayOutputStream()
      val buf = new Array[Byte](512)
      var read = 0
      while ({ read = is.read(buf, 0, buf.length); read } != -1) {
        baos.write(buf, 0, read)
      }
      baos.toByteArray
    }
    finally is.close()
  }
}
