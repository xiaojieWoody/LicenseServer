package com.mylicense.common;

import java.io.Serializable;

public class CommonConstant implements Serializable {

    private static final long serialVersionUID = 5648963506605275316L;

    public static ResMsg SUCCESS = new ResMsg(200, "success", "", null);
    public static ResMsg FAIL = new ResMsg(-1, "fail", "", null);
}
