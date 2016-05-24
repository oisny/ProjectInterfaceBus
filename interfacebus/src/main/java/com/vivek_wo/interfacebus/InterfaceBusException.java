package com.vivek_wo.interfacebus;

/**
 * Created by Oisny on 2016/5/20.
 */
public class InterfaceBusException extends RuntimeException {

    public InterfaceBusException(String detailMessage) {
        super(detailMessage);
    }

    public InterfaceBusException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public InterfaceBusException(Throwable throwable) {
        super(throwable);
    }
}
