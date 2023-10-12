package org.example.demo;


import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *IO服务端
 */
public class IOServer {
    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(8000);

        //接收新连接线程
        new Thread(() -> {
           while(true) {
               try{
                   //  阻塞方法获取新连接
                   Socket socket = serverSocket.accept();

                   new Thread(() -> {
                       try {
                           int len;
                           byte[] data = new byte[1024];
                           InputStream inputStream = socket.getInputStream();

                           //按照字节流的方式读取数据   赋值符号返回右侧的值
                           while ((len = inputStream.read(data)) != -1) {
                               System.out.println(new String(data,0,len));
                           }

                       } catch (IOException e) {
                           throw new RuntimeException(e);
                       }
                   }).start();
               } catch (IOException e) {
                   throw new RuntimeException(e);
               }
           }
        }).start();
    }
}
