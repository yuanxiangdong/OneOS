package com.example;

import org.eclipse.jetty.websocket.client.WebSocketClient;

import java.net.URI;

public class JettyWebsocketClient {

    private static void println(String msg) {
        System.out.println("------" + msg + "------");
    }

    public static void main(String[] args) {
//        Log.getLogger(WebSocketClient.class).setDebugEnabled(true);
        WebSocketClient client = new WebSocketClient();
        MyWebSocket socket = new MyWebSocket();
        try {
            client.start();
            URI uri = new URI("ws://192.168.1.170:1080");
            println("Connecting");
            client.connect(socket, uri).get();
            println("Send Message");
            socket.sendMessage("Hi! --from Jetty Socket Client!");
        } catch (Exception e) {
            println("Exception-1");
            e.printStackTrace();
        } finally {
            try {
                client.stop();
            } catch (Exception e) {
                println("Exception-2");
                e.printStackTrace();
            }
        }
    }
}
