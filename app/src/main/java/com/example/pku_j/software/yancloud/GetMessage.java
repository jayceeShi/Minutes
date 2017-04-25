//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.example.pku_j.software;

import java.io.Serializable;

public class GetMessage implements Serializable {
    private static final long serialVersionUID = 1870689578034300371L;
    public String type;
    public String pkgName;
    public String method;
    public String arg;

    public GetMessage() {
    }

    public String handle() {
        try {
            return YanCloudStub.get(this.pkgName, this.method, this.arg);
        } catch (Throwable var2) {
            return "Exception:" + var2.getMessage();
        }
    }
}
