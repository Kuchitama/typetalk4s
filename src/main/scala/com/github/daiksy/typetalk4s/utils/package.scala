package com.github.daiksy.typetalk4s

package object utils {
  object Closable {
    def withClose[A, B <: { def close() }](b: B)(f: B => A) = try { f(b) } catch {
      case e: Throwable => e.printStackTrace()
    } finally {
      b.close()
    }
    def withStop[A, B <: { def stop() }](b: B)(f: B => A) = try { f(b) } catch {
      case e: Throwable => e.printStackTrace()
    } finally {
      b.stop()
    }
  }
}
