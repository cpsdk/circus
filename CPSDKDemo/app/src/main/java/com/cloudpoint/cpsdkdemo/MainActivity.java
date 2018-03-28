package com.cloudpoint.cpsdkdemo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.cloudpoint.cpsdkdemo.dummy.DummyContent;
import com.cloudpoint.plugins.log.CPLogger;
import com.cloudpoint.sdk.circus.CPSDK;
import com.cloudpoint.sdk.circus.api.ICPGameListener;
import com.cloudpoint.sdk.circus.api.ICPSDKCallback;
import com.cloudpoint.sdk.circus.api.domain.CPConsume;
import com.cloudpoint.sdk.circus.api.domain.CPDevice;
import com.cloudpoint.sdk.circus.api.domain.CPDeviceList;
import com.cloudpoint.sdk.circus.api.domain.CPError;
import com.cloudpoint.sdk.circus.api.domain.ICPSDKJoinedRoom;
import com.cloudpoint.sdk.circus.http.domain.ChargeCoin;
import com.cloudpoint.sdk.circus.http.domain.ConsumeItemValue;
import com.cloudpoint.sdk.circus.http.domain.UserWallet;

import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class MainActivity extends AppCompatActivity implements ItemFragment.OnListFragmentInteractionListener, ICPGameListener {

    private static final String TAG = "MainActivity";


    @BindView(R.id.confirm) //用户id绑定操作
            Button btnConfirm;
    @BindView(R.id.userId)  //输入用户id
            EditText etUserId;

    @BindView(R.id.showMsg) //展示日志信息
            TextView tvShowMsg;


    @BindView(R.id.btnLeaveRoom) //离开房间
            Button btnLeaveRoom;
    @BindView(R.id.btnJoinRoom) //加入房间
            Button btnJoinRoom;
    @BindView(R.id.btnCharge) //充值
            Button btnCharge;
    @BindView(R.id.tvPrize) //掉落数
            TextView tvPrize;
    @BindView(R.id.tvReward)//奖励数
            TextView tvReward;
    @BindView(R.id.tvUserWallet) //用户钱包值
            TextView tvWallet;
    @BindView(R.id.tvUserWalletEnd)
    TextView tvWalletEnd;

    @BindView(R.id.tvUserInRoom) //是否在房间内。
            TextView tvUserInRoom;

    @BindView(R.id.tvTimeout) // 自动timeout时间
            TextView tvTimeout;
    @BindView(R.id.btnRefreshRoom)
    Button btnRefreshRoom;

    @BindView(R.id.tvUserDrops)
    TextView tvUserDrops;
    @BindView(R.id.tvUserFee)
    TextView tvUserFee;
    @BindView(R.id.btnBrush)
    Button btnBrush;


    boolean userInRoom;


    Unbinder unbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        unbinder = ButterKnife.bind(this);

        //初始化事件
        initView();


        readSimSerialNumber();


    }


    private void readSimSerialNumber() {
        TelephonyManager manager = (TelephonyManager) getApplicationContext().getSystemService(TELEPHONY_SERVICE);

        String serialNumber = manager.getSimSerialNumber();
        CPLogger.d("serial:"+serialNumber+" "+manager.getSimOperator());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private StringBuffer buffer = new StringBuffer();
    //static int lines = 0;



    private Handler handler = new Handler();

    private static  int lines = 0;

    private void appendText(String text){
        appendText(text,System.currentTimeMillis());
    }

    private void appendText(String text,long start){
        lines++;
        buffer.append("    ").append(lines).append(" - ").append(text).append(" - { duration:").append(System.currentTimeMillis()-start).append( "}\n");

        final String msg = buffer.toString();
        handler.post(  new Runnable() {
            @Override
            public void run() {
                tvShowMsg.setText(msg);
            }
        });
        if(lines>30){
            lines = 0;
            buffer.delete(0, buffer.length());
        }
    }


    private String userId;

    private void initView(){

        userInRoom = false;

        //TODO:
        btnCharge.setEnabled(false);
        btnLeaveRoom.setEnabled(false);
        btnJoinRoom.setEnabled(false);


        //
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                //TODO: 2.绑定用户id

                userId=etUserId.getText().toString();
                binding(userId);


            }
        });

        //


        btnLeaveRoom.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {

                CPSDK.leaveRoom(id, new ICPSDKCallback<Boolean>() {
                    @Override
                    public void onOk(Boolean aBoolean) {
                        appendText("离开房间！ "+ id);

                        //tvUserInRoom.setText("false");
                        unregisterListener();
                        userInRoom = false;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                tvUserInRoom.setText("false");
                                tvPrize.setText("0");
                                tvReward.setText("0");
                                tvWallet.setText("0");



                            }
                        });
                    }

                    @Override
                    public void onFailure(CPError cpError) {

                    }

                    @Override
                    public void onError(Throwable throwable) {

                    }
                });

            }
        });


        btnJoinRoom.setOnClickListener(new View.OnClickListener(){


            @Override
            public void onClick(View view) {
                CPSDK.joinRoom(id,new ICPSDKJoinedRoom() {
                    @Override
                    public void onJoinedRoom() {
                        appendText("进入房间"+id);
                        registerListener();
                        userInRoom = true;

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                tvUserInRoom.setText("true");
                                btnLeaveRoom.setEnabled(true);

                            }
                        });


                    }
                });

            }
        });

        btnCharge.setOnClickListener(new View.OnClickListener(){


            @Override
            public void onClick(View view) {
                chargeCoin(id,1000,true);
            }
        });

        btnRefreshRoom.setOnClickListener(new View.OnClickListener(){


            @Override
            public void onClick(View view) {
                fetchDeviceList();
            }
        });

        btnBrush.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                CPSDK.switchWiper(id, new ICPSDKCallback<Boolean>() {
                    @Override
                    public void onOk(Boolean aBoolean) {

                    }

                    @Override
                    public void onFailure(CPError cpError) {

                    }

                    @Override
                    public void onError(Throwable throwable) {

                    }
                });
            }
        });

    }





    private void binding(final String userId){



        final long start = System.currentTimeMillis();
        CPSDK.binding(userId, new ICPSDKCallback<String>() {
            @Override
            public void onOk(String s) {
                //Toast.makeText(MainActivity.this,"  7654321 <===> "+s ,Toast.LENGTH_LONG).show();
                appendText("用户绑定成功!"+userId+"，"+s,start);
                //buffer.append(line).append(". 用户绑定成功! ").append(userId).append(" { onOk:").append(s).append(" , duration:").append(System.currentTimeMillis()-start).append( "}\n");
                CPLogger.d(TAG,"binding",userId+"   <===> "+s);
               // fet
                fetchDeviceList();


            }

            @Override
            public void onFailure(CPError cpError) {
                appendText("用户绑定失败!"+userId,start);
                //buffer.append(line).append(". 用户绑定失败! ").append(userId).append("{ onFailure:").append(cpError.toString()).append(" , duration:").append(System.currentTimeMillis()-start).append(" }\n");

            }

            @Override
            public void onError(Throwable throwable) {
                // throwable.printStackTrace();
                CPLogger.e(TAG,"binding",">>>>>>."+throwable.getMessage());
                //buffer.append(line).append(". 用户绑定异常! ").append(userId).append("{ onError:").append(throwable.getMessage()).append(" , duration:").append(System.currentTimeMillis()-start).append(" } \n");
                appendText("用户绑定异常! "+userId,start);
            }
        });


    }

    private void fetchDeviceList(){
        CPSDK.fetchDeviceList(1, 20, new ICPSDKCallback<CPDeviceList>() {
            @Override
            public void onOk(CPDeviceList cpDeviceList) {

                //cleanupFragment();
                if(cpDeviceList!=null) {
                    CPLogger.d(TAG, "onOk", " total :" + cpDeviceList.getTotal() + ", page: " + cpDeviceList.getCurrentPage());
                    List<CPDevice> devices= cpDeviceList.getDevices();
                    List<DummyContent.DummyItem> data = new LinkedList<>();

                    for(CPDevice  device:devices){
                        CPLogger.d(TAG,"onOK",""+device.toString());
                        DummyContent.DummyItem item = new DummyContent.DummyItem(device.getDeviceId(),String.valueOf(""),String.valueOf("2."),device.getState()==0?"空闲":"游戏中");
                        data.add(item);
                    }

                    Fragment fragment= getSupportFragmentManager().findFragmentById(R.id.fragmentDeviceList);
                    if(fragment!=null){
                        getSupportFragmentManager().beginTransaction().remove(fragment).commit();
                    }
                    getSupportFragmentManager().beginTransaction().add(R.id.fragmentDeviceList, ItemFragment.setDatas(data,ItemFragment.TYPE_DEVICE_LIST)).commit();




                }

            }

            @Override
            public void onFailure(CPError cpError) {

            }

            @Override
            public void onError(Throwable throwable) {
                CPLogger.e("MainActivity","onError",throwable.getMessage());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(unbinder!=null){
            unbinder.unbind();
            unbinder = null;
        }
    }



    private void registerListener(){
        CPSDK.registerGameListener(this);
    }

    private void unregisterListener(){
        CPSDK.unRegisterGameListener(this);
    }


    private void fetchConsumeList(final String deviceId){

        appendText("选择设备："+deviceId,System.currentTimeMillis());
        final long start = System.currentTimeMillis();

        // after join room do fetch consume list.
        CPSDK.fetchConsumeList(deviceId, new ICPSDKCallback<List<CPConsume>>() {
            @Override
            public void onOk(List<CPConsume> cpConsumes) {
                if (cpConsumes != null) {
                    List<DummyContent.DummyItem> data = new LinkedList<>();

                    for (CPConsume device : cpConsumes) {
                        CPLogger.d(TAG, "onOK", "" + device.toString());
                        DummyContent.DummyItem item = new DummyContent.DummyItem(device.getId(), deviceId
                                , String.valueOf("5."),"币:"+device.getCoins()+",价:"+device.getPrice());
                        data.add(item);
                    }
                    //getSupportFragmentManager().beginTransaction().remove()
                    Fragment fragment= getSupportFragmentManager().findFragmentById(R.id.fragmentConsumeList);
                    if(fragment!=null){
                        getSupportFragmentManager().beginTransaction().remove(fragment).commit();
                    }
                    getSupportFragmentManager().beginTransaction().add(R.id.fragmentConsumeList, ItemFragment.setDatas(data,ItemFragment.TYPE_CONSUME_LIST)).commit();



                }
            }

            @Override
            public void onFailure(CPError cpError) {

                if(cpError!=null)
                    appendText(cpError.getMsg(),start);
            }

            @Override
            public void onError(Throwable throwable) {

            }
        });

    }


    private String id;
    private void playGame(final String consumeId, final String deviceId){
        id = deviceId;

        CPSDK.getUserWallet( new ICPSDKCallback<UserWallet>() {
            @Override
            public void onOk(final UserWallet userWallet) {


                appendText("用户币数: "+userWallet.getCoin());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        tvWallet.setText(userWallet.getCoin()+"");

                    }
                });


                CPSDK.playGame(consumeId, deviceId, new ICPSDKCallback<ConsumeItemValue>() {
                    @Override
                    public void onOk(ConsumeItemValue consumeItemValue) {

                        appendText("推币 ：" + consumeId + " orderNumber:" + consumeItemValue.getOrderNumber());

                    }

                    @Override
                    public void onFailure(CPError cpError) {
                        if(cpError!=null){
                            appendText("失败！"+cpError.getMsg());
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {

                    }
                });

            }

            @Override
            public void onFailure(CPError cpError) {

            }

            @Override
            public void onError(Throwable throwable) {

            }
        });



    }


    private void fetchUserWallet(String deviceId){
        CPSDK.getUserWallet( new ICPSDKCallback<UserWallet>() {
            @Override
            public void onOk(final UserWallet userWallet) {


                appendText("用户币数: "+userWallet.getCoin());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        tvWalletEnd.setText(userWallet.getCoin()+"");
                    }
                });
            }

            @Override
            public void onFailure(CPError cpError) {

            }

            @Override
            public void onError(Throwable throwable) {

            }
        });
    }

    //
    private void chargeCoin(final String deviceId, int coin, boolean isCharge){
        CPSDK.chargeCoins(deviceId,coin,isCharge,new ICPSDKCallback<ChargeCoin>(){

            @Override
            public void onOk(final ChargeCoin chargeCoin) {
                // get consumelist

                appendText("充值成功！"+chargeCoin.getCoin());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        tvWallet.setText(""+chargeCoin.getCoin());
                    }
                });
                //fetchUserWallet(id);

            }

            @Override
            public void onFailure(CPError cpError) {

            }

            @Override
            public void onError(Throwable throwable) {

            }
        });
    }

    @Override
    public void onListFragmentInteraction(final DummyContent.DummyItem item,int type) {


        CPLogger.d("interaction:"+item.id);
        //oast.makeText(MainActivity.this,"clicked :"+item.id+" , "+item.content,Toast.LENGTH_LONG).show();

        //TODO：4 选择房间，列出消费档位
        if(type==ItemFragment.TYPE_DEVICE_LIST){
            id = item.id;
            final long start=System.currentTimeMillis();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    btnCharge.setEnabled(true);
                    btnJoinRoom.setEnabled(true);
                }
            });
            fetchUserWallet(id);
            fetchConsumeList(item.id);


        } else if(type==ItemFragment.TYPE_CONSUME_LIST){
            //fetchUserWallet(item.content);
            if(userInRoom) {
                fetchUserWallet(id);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        tvUserDrops.setText("");
                        tvUserFee.setText(item.desc);
                    }
                });
                playGame(item.id, item.content);
            }
            else
                Toast.makeText(getApplicationContext(),"请先加入房间再时行游戏 ",Toast.LENGTH_LONG).show();;
        }








    }

    int drops = 0;
    @Override
    public void gameStart() {
        CPLogger.d(TAG,"gameStart","  GAME START....");
        appendText("Game start");

        fetchDeviceList();
        drops = 0;
    }

    private int prize;
    private int reward;

    @Override
    public void updatePrize(int i) {
        prize = i;
        CPLogger.d(TAG,"updatePrize","  "+i);
        appendText("updatePrize "+i);
        handler.post(new Runnable() {
            @Override
            public void run() {
                tvPrize.setText(""+prize);
            }
        });

        drops+=i;
        fetchUserWallet(id);

    }

    @Override
    public void updateReward(int i) {
        reward = i;
        CPLogger.d(TAG,"updateReward","  "+i);
        appendText("updateReward "+(reward));

        handler.post(new Runnable() {
            @Override
            public void run() {
                tvReward.setText(""+reward);
            }
        });
    }

    @Override
    public void gameEnd() {
        CPLogger.d(TAG,"gameEnd","....  GAME END");
        appendText("gameEnd ");
        fetchUserWallet(id);
        fetchDeviceList();
        handler.post(new Runnable() {
            @Override
            public void run() {
                tvUserDrops.setText(""+drops);
            }
        });

    }

    @Override
    public void onWebsocketStateChanged(final boolean b) {


            handler.post(new Runnable() {
                @Override
                public void run() {
                    if(b){
                        tvUserInRoom.setText("online");
                        registerListener();
                        btnLeaveRoom.setEnabled(true);
                        userInRoom = true;

                    }else{
                        tvUserInRoom.setText("offline");
                        btnLeaveRoom.setEnabled(false);
                        btnJoinRoom.setEnabled(true);
                        unregisterListener();
                        userInRoom = false;
                    }
                }
            });


    }

    @Override
    public void countDown( int i) {

        final int cnt = i;
        handler.post(new Runnable() {
            @Override
            public void run() {

                tvTimeout.setText(" "+cnt);

                if(cnt<CPSDKDemoApplication.GameEndManualTimeout){
                     CPSDK.endGame();
                }
            }
        });

    }


}
