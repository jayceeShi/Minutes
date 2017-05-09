//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.example.pku_j.software.yancloud;

import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server extends Thread {
    public static ExecutorService executor = Executors.newFixedThreadPool(5);
    private static final String TAG = "ApiminierServer";
    public static Server instance;
    private ServerSocket serverSocket;

    public Server() {
    }

    public static void main(String[] args) {
        instance = new Server();
        instance.start();
    }

    public void run() {
        int listeningPort = 1716;

        while(true) {
            try {
                this.serverSocket = new ServerSocket(listeningPort);
                break;
            } catch (Exception var5) {
                ++listeningPort;
            }
        }

        Log.d("ApiminierServer", "Server port:" + listeningPort);

        while(true) {
            while(true) {
                try {
                    Socket client = this.serverSocket.accept();
                    executor.execute(new Server.HandlerThread(client));
                } catch (IOException var4) {
                    var4.printStackTrace();
                }
            }
        }
    }

    private class HandlerThread implements Runnable {
        private Socket socket;

        public HandlerThread(Socket client) {
            this.socket = client;
        }

        public void run() {
            try {
                ObjectInputStream e = new ObjectInputStream(this.socket.getInputStream());
                GetMessage message = (GetMessage)e.readObject();
                ObjectOutputStream out = new ObjectOutputStream(this.socket.getOutputStream());
                String str = message.handle();
                out.writeObject(str);
                out.flush();
                out.close();
                e.close();
            } catch (Throwable var13) {
                System.out.println("Exception: " + var13.getMessage());
                var13.printStackTrace();
            } finally {
                try {
                    if(this.socket != null) {
                        this.socket.close();
                    }
                } catch (Exception var12) {
                    this.socket = null;
                    Log.d("ApiminierServer", "Server Error:" + var12.getMessage());
                }

            }

        }
    }
}
