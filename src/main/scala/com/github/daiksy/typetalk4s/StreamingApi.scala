package com.github.daiksy.typetalk4s

import java.net.URI
import java.util.concurrent.{ TimeUnit, CountDownLatch }

import org.eclipse.jetty.websocket.api.util.WSURI
import org.eclipse.jetty.websocket.api.{ StatusCode, Session }
import org.eclipse.jetty.websocket.api.annotations.{ OnWebSocketMessage, OnWebSocketConnect, OnWebSocketClose, WebSocket }
import org.eclipse.jetty.websocket.client.{ ClientUpgradeRequest, WebSocketClient }
import utils.Closable._

class StreamingApi(typetalk: Typetalk4s)(messageHandler: String => Unit) {
  lazy val endPoint = s"${typetalk.typetalkApiUrl}streaming"
  lazy val uri = WSURI.toWebsocket(endPoint)

  private val latch = new CountDownLatch(1)

  withStop(new WebSocketClient()) { client =>
    client.start()
    val socket = new StreamingSocket()
    val request = {
      val r = new ClientUpgradeRequest()
      r.setHeader("Authorization", s"Bearer ${typetalk.accessToken.accessToken}")
      r
    }
    println(s"start to connect ${uri}")
    client.connect(socket, uri, request)
    println("connected")
    latch.await(180, TimeUnit.SECONDS)
    socket.close()
  }

  def stop(): Unit = latch.countDown()

  @WebSocket(maxTextMessageSize = 64 * 1024)
  private class StreamingSocket() {
    // TODO varェ…
    private var session: Option[Session] = None

    def close(): Unit = {
      session.foreach(_.close(StatusCode.NORMAL, "done."))
    }

    @OnWebSocketClose
    def onClose(statusCode: Int, reason: String): Unit = {
      println(s"connection closed: ${statusCode} - ${reason}")
    }

    @OnWebSocketConnect
    def onConnect(session: Session): Unit = {
      this.session = Some(session)
      println(s"connected: ${session}")
    }

    @OnWebSocketMessage
    def onMessage(message: String): Unit = {
      println(s"got message: ${message}")
      messageHandler(message)
    }

  }
}
