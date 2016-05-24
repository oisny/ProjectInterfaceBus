package com.vivek_wo.interfacebus;


/**
 * Created by Oisny on 2016/5/24.
 */
public class Publish {
    String event;
    Object object;

    public Publish(String event, Object object) {
        this.event = event;
        this.object = object;
    }

    public String getEvent() {
        return event;
    }

    public Object getObject() {
        return object;
    }
}
