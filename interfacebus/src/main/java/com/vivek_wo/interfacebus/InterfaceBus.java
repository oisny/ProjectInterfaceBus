package com.vivek_wo.interfacebus;

import android.os.Looper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Oisny on 2016/5/18.
 */
public class InterfaceBus {

    static volatile InterfaceBus instance;
    private static final InterfaceBuilder DEFAULT_BUILDER = new InterfaceBuilder();

    private Map<String, CopyOnWriteArrayList<Subscribtion>> mSubscribtionInterfaceTypes;
    private Map<BaseSubscribtionInterface, List<String>> mtypeEvents;

    private final ThreadLocal<PostThreadState> mCurrentPostThreadState = new ThreadLocal<PostThreadState>() {
        @Override
        protected PostThreadState initialValue() {
            return new PostThreadState();
        }
    };

    public static InterfaceBus getDefault() {
        if (instance == null) {
            synchronized (InterfaceBus.class) {
                if (instance == null) {
                    instance = new InterfaceBus();
                }
            }
        }
        return instance;
    }

    public static InterfaceBuilder builder() {
        return new InterfaceBuilder();
    }

    public InterfaceBus() {
        this(DEFAULT_BUILDER);
    }

    InterfaceBus(InterfaceBuilder builder) {
        mSubscribtionInterfaceTypes = new HashMap<String, CopyOnWriteArrayList<Subscribtion>>();
        mtypeEvents = new HashMap<BaseSubscribtionInterface, List<String>>();
    }

    public void register(BaseSubscribtionInterface subscribtionInterface, String event) {
        register(subscribtionInterface, new SubscribtionFilter(event));
    }

    public synchronized void register(BaseSubscribtionInterface subscribtionInterface, SubscribtionFilter filter) {
        List<String> events = filter.getSubscribtionEvents();
        for (String event : events) {
            subscribe(subscribtionInterface, event, filter.getPriority());
        }
    }

    private void subscribe(BaseSubscribtionInterface subscribtionInterface, String event, int priority) {
        CopyOnWriteArrayList<Subscribtion> subscribeInterfaces = mSubscribtionInterfaceTypes.get(event);
        Subscribtion subscribtion = new Subscribtion(subscribtionInterface, event, priority);
        if (subscribeInterfaces == null) {
            subscribeInterfaces = new CopyOnWriteArrayList<Subscribtion>();
            mSubscribtionInterfaceTypes.put(event, subscribeInterfaces);
        }

        int size = subscribeInterfaces.size();
        for (int i = 0; i <= size; i++) {
            if (i == size || priority > subscribeInterfaces.get(i).getPriority()) {
                subscribeInterfaces.add(i, subscribtion);
                break;
            }
        }

        List<String> typeEvents = mtypeEvents.get(subscribtionInterface);
        if (typeEvents == null) {
            typeEvents = new ArrayList<String>();
            mtypeEvents.put(subscribtionInterface, typeEvents);
        }

        typeEvents.add(event);
    }

    public synchronized void unregister(BaseSubscribtionInterface subscribtionInterface) {
        List<String> events = mtypeEvents.get(subscribtionInterface);
        if (events != null) {
            for (String event : events) {
                unSubscribtionInterface(subscribtionInterface, event);
            }
            mtypeEvents.remove(subscribtionInterface);
        }
    }

    private void unSubscribtionInterface(BaseSubscribtionInterface subscribtionInterface, String event) {
        List<Subscribtion> subscribtionInterfaces = mSubscribtionInterfaceTypes.get(event);
        if (subscribtionInterfaces != null) {
            int size = subscribtionInterfaces.size();
            for (int i = 0; i < size; i++) {
                Subscribtion subscribtion = subscribtionInterfaces.get(i);
                if (subscribtionInterface == subscribtion.get()) {
                    subscribtionInterfaces.remove(i);
                    i--;
                    size--;
                }
            }
        }
    }

    public void cancelSubscriberDelivery(String event) {
        PostThreadState postThreadState = mCurrentPostThreadState.get();
        if (!postThreadState.isPosting) {
            throw new InterfaceBusException("This method may only be called from inside event handling methods on the posting thread");
        } else if (event == null) {
            throw new InterfaceBusException("subscriber may not be null.");
        } else if (postThreadState.publish.getEvent() != event) {
            throw new InterfaceBusException("Only the currently handled event may be aborted");
        }

        postThreadState.canceled = true;
    }

    public void post(Publish publish) {
        PostThreadState postThreadState = mCurrentPostThreadState.get();
        List<Publish> subscriberQueue = postThreadState.subscriberQueue;
        subscriberQueue.add(publish);

        if (!postThreadState.isPosting) {
            postThreadState.isMainThread = Looper.getMainLooper() == Looper.myLooper();
            postThreadState.isPosting = true;
            if (postThreadState.canceled) {
                throw new InterfaceBusException("Internal error.Abort state was not reset");
            }
            try {
                while (!subscriberQueue.isEmpty()) {
                    postSubscriber(subscriberQueue.remove(0), postThreadState);
                }
            } finally {
                postThreadState.isPosting = false;
                postThreadState.isMainThread = false;
            }
        }
    }

    private void postSubscriber(Publish publish, PostThreadState postThreadState) {
        CopyOnWriteArrayList<Subscribtion> subscribtions = null;
        synchronized (this) {
            subscribtions = mSubscribtionInterfaceTypes.get(publish.getEvent());
        }
        if (subscribtions != null && !subscribtions.isEmpty()) {
            for (Subscribtion subscribtion : subscribtions) {
                postThreadState.publish = publish;
                boolean aborted = false;
                try {
                    BaseSubscribtionInterface subscribtionInterface = subscribtion.get();
                    subscribtionInterface.onSubscribed(publish);
                    aborted = postThreadState.canceled;
                } finally {
                    postThreadState.publish = null;
                    postThreadState.canceled = false;
                }
                if (aborted) {
                    break;
                }
            }
        }
    }

    final static class PostThreadState {
        final List<Publish> subscriberQueue = new ArrayList<Publish>();
        Publish publish;
        boolean isPosting;
        boolean isMainThread;
        boolean canceled;
    }


}
