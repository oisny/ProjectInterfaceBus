package com.vivek_wo.projectinterfacebus.activity;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.vivek_wo.interfacebus.BaseSubscribtionInterface;
import com.vivek_wo.interfacebus.InterfaceBus;
import com.vivek_wo.interfacebus.Publish;
import com.vivek_wo.interfacebus.SubscribtionFilter;
import com.vivek_wo.projectinterfacebus.R;

public class MainActivity extends AppCompatActivity {
    private static final String TEST_ACTION1 = "com.vivek_wo.projectinterfacebus.action1";
    private static final String TEST_ACTION2 = "com.vivek_wo.projectinterfacebus.action2";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SubscribtionFilter filter1 = new SubscribtionFilter(TEST_ACTION1);
        InterfaceBus.getDefault().register(mBaseSubscribtionInterface1, filter1);

        SubscribtionFilter filter2 = new SubscribtionFilter(TEST_ACTION1, 1);
        filter2.addFilter(TEST_ACTION2);
        InterfaceBus.getDefault().register(mBaseSubscribtionInterface2, filter2);

        mHandler.sendEmptyMessageDelayed(1, 5000);
    }

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    InterfaceBus.getDefault().post(new Publish(TEST_ACTION1, new byte[]{0x01}));
                    mHandler.sendEmptyMessageDelayed(2 , 5000);
                    break;
                case 2:
//                    InterfaceBus.getDefault().post(new Publish(TEST_ACTION1, new byte[]{0x01}));
                    InterfaceBus.getDefault().post(new Publish(TEST_ACTION2, new byte[]{0x02}));
                    break;
            }
        }
    };

    private BaseSubscribtionInterface mBaseSubscribtionInterface1 = new BaseSubscribtionInterface() {
        @Override
        public void onSubscribed(Publish object) {
            Log.d(getLocalClassName(), "interface 1 event : " + object.getEvent());
        }
    };

    private BaseSubscribtionInterface mBaseSubscribtionInterface2 = new BaseSubscribtionInterface() {
        @Override
        public void onSubscribed(Publish object) {
            Log.d(getLocalClassName(), "interface 2 event : " + object.getEvent());
            if(object.getEvent().equals(TEST_ACTION1)){
                InterfaceBus.getDefault().cancelSubscriberDelivery(TEST_ACTION1);
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        InterfaceBus.getDefault().unregister(mBaseSubscribtionInterface1);
        InterfaceBus.getDefault().unregister(mBaseSubscribtionInterface2);
    }

}
