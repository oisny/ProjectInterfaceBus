package com.vivek_wo.interfacebus;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Oisny on 2016/5/19.
 */
public class SubscribtionFilter {

    private int priority;

    private List<String> lists;

    public SubscribtionFilter(String event) {
        this(event, 0);
    }

    public SubscribtionFilter(String event, int priority) {
        lists = new ArrayList<String>();
        this.priority = priority;
        addFilter(event);
    }

    public void addFilter(String event) {
        if (!lists.contains(event)) {
            lists.add(event);
        }
    }

    public int getPriority() {
        return priority;
    }

    public Iterator<String> eventIterator() {
        return lists != null ? lists.iterator() : null;
    }

    public List<String> getSubscribtionEvents() {
        return lists;
    }

}
