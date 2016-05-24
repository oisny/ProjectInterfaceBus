package com.vivek_wo.interfacebus;

/**
 * Created by Oisny on 2016/5/19.
 */
public class Subscribtion {

    private int priority;

    private String event;

    private BaseSubscribtionInterface subscribtionInterface;

    Subscribtion(BaseSubscribtionInterface subscribtionInterface, String event) {
        this(subscribtionInterface, event, 0);
    }

    Subscribtion(BaseSubscribtionInterface subscribtionInterface, String event, int priority) {
        this.subscribtionInterface = subscribtionInterface;
        this.event = event;
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    public BaseSubscribtionInterface get() {
        return subscribtionInterface;
    }
}
