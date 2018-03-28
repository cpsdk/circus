package com.cloudpoint.cpsdkdemo;

import android.app.Application;
import android.widget.Toast;

import com.cloudpoint.plugins.log.CPLogger;
import com.cloudpoint.sdk.circus.CPSDK;
import com.cloudpoint.sdk.circus.api.ICPSDKCallback;
import com.cloudpoint.sdk.circus.api.domain.CPError;

/**
 * Created by apple on 2/9/18.
 */

public class CPSDKDemoApplication extends Application  implements ICPSDKCallback<Boolean>{

    public static final int GameEndAutoTimeout =  90;
    public static final int GameEndManualTimeout = GameEndAutoTimeout -15;

    @Override
    public void onCreate() {
        super.onCreate();

        //TODO: 1. CPSDK 使用appId与appSecret 初始化SDK
       CPSDK.initialize(getApplicationContext(),"435","ql4ub857c68e2kft6vono7of4e",this);
        //339

        //CPSDK.initialize(getApplicationContext(),"339","guf6r7mb6aj5tdlgk1eilngl1s",this);
    }


    /**
     * SDK初始化成功
     * @param aBoolean
     */
    @Override
    public void onOk(Boolean aBoolean) {
        CPLogger.d("CPSDKDemoApplication","onOk",">>>>> "+aBoolean);
        CPSDK.setIdleTimeout(GameEndAutoTimeout);
        Toast.makeText(getApplicationContext(),"SDK initialized!",Toast.LENGTH_LONG).show();
    }


    /**
     * 初化失败
     * @param cpError
     */
    @Override
    public void onFailure(CPError cpError) {
        CPLogger.d("CPSDKDemoApplication","onOk",">>>>> "+cpError.toString());
    }

    /**
     * 初始化异常
     * @param throwable
     */
    @Override
    public void onError(Throwable throwable) {
        CPLogger.d("CPSDKDemoApplication","onOk",">>>>> "+throwable.getMessage());
    }
}
