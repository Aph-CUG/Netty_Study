package org.example.demo;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;

/**
 * IO客户端
 */
public class IOClient {
    public static void main(String[] args) {
        new Thread(() -> {
            try {
                Socket socket = new Socket("localhost", 8000);
                while (true) {
                    try{
                        socket.getOutputStream().write((new Date() + ": hello world").getBytes());
                        Thread.sleep(2000);
                    } catch (Exception e) {
                    }
                }
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }
}
