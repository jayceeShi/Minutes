//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.example.pku_j.software;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class YanCloudGet {
    private String ipAddress;
    private int port;
    private Socket socket = null;

    public YanCloudGet(String ipAddress, int port) {
        this.ipAddress = ipAddress;
        this.port = port;
    }

    public static YanCloudGet fromPackageName(String ip, int maxPort, String pkgName) {
        try {
            for(int e = 1716; e < maxPort; ++e) {
                try {
                    YanCloudGet e1 = new YanCloudGet(ip, e);
                    String processName = e1.get("APIPorter", "getProcessName", (String)null);
                    if(processName.equals(pkgName)) {
                        return e1;
                    }
                } catch (Exception var6) {
                    var6.printStackTrace();
                }
            }
        } catch (Exception var7) {
            var7.printStackTrace();
        }

        return null;
    }

    public String get(String pkgName, String method, String arg) {
        try {
            GetMessage e = new GetMessage();
            e.type = "get";
            e.pkgName = pkgName;
            e.method = method;
            e.arg = arg;
            this.socket = new Socket(this.ipAddress, this.port);
            ObjectOutputStream output = new ObjectOutputStream(this.socket.getOutputStream());
            output.writeObject(e);
            output.flush();
            ObjectInputStream input = new ObjectInputStream(this.socket.getInputStream());
            String ret = (String)input.readObject();
            output.close();
            input.close();
            return ret;
        } catch (Exception var8) {
            return "Client Exception:" + var8.getMessage();
        }
    }
}
