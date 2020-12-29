package org.reactnative.camera.events;

import androidx.core.util.Pools;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.Event;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import org.reactnative.camera.CameraViewManager;

public class FaceVerifiedEvent extends Event<FaceVerifiedEvent> {
    private static final Pools.SynchronizedPool<FaceVerifiedEvent> EVENTS_POOL=
            new Pools.SynchronizedPool<>(3);

//    private WritableArray mData;
    private float mData;

    private FaceVerifiedEvent(){}

    public static FaceVerifiedEvent obtain(int viewTag,float result){
        FaceVerifiedEvent event=EVENTS_POOL.acquire();
        if(event==null){
            event=new FaceVerifiedEvent();
        }
        event.init(viewTag,result);
        return event;
    }

    private void init(int viewTag,float result){
        super.init(viewTag);
//        mData = Arguments.createArray();
//        mData.put(result);
//        mData= String.valueOf(result);
        mData=result;
    }

    /**
     * note(@sjchmiela)
     * Should the events about detected faces coalesce, the best strategy will be
     * to ensure that events with different faces count are always being transmitted.
     */
//@Override
//public short getCoalescingKey(){
////        if(mData.size()>Short.MAX_VALUE){
////        return Short.MAX_VALUE;
////        }
////
////        return(short)mData.size();
//
//        }

    @Override
    public String getEventName(){
        return CameraViewManager.Events.EVENT_ON_FACE_VERIFIED.toString();
    }

    @Override
    public void dispatch(RCTEventEmitter rctEventEmitter){
        rctEventEmitter.receiveEvent(getViewTag(),getEventName(),serializeEventData());
    }

    private WritableMap serializeEventData(){
        WritableMap event= Arguments.createMap();
        event.putString("type","verify");
        event.putDouble("result",mData);
        event.putInt("target",getViewTag());
        return event;
    }
}
