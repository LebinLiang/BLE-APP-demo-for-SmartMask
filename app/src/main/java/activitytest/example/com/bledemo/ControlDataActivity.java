package activitytest.example.com.bledemo;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import activitytest.example.com.bledemo.ReceiverAdapter;


public class ControlDataActivity extends AppCompatActivity {

    private final static String TAG = DeviceControlActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private TextView mConnectionState , Mask_Count,Target_Tem,Vocs,Co2,EN_Tem,En_Hum,Danger_Index;


    private String mServiceUUID = "0000fff0-0000-1000-8000-00805f9b34fb";

    private String mCharaUUID_TX = "0000fff2-0000-1000-8000-00805f9b34fb";

    private String mCharaUUID_RX ="0000fff1-0000-1000-8000-00805f9b34fb";

    private String mDeviceName ,mDeviceAddress;

    private TextView mCharaDescriptor;
    private TextView mDataField;
    private TextView mDeviceAddressTextView;

    private  static BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

    private boolean mConnected = true;
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    private Button Night_Mode_Button,Auto_Mode_Button,Manual_Mode_Button;

    private ReceiverAdapter mReceiverAdapter;

    private Switch mswitch ;

    private  SimpleDateFormat simpleDateFormat;

    // private final String LIST_NAME = "NAME";
    //  private final String LIST_UUID = "UUID";
    //  private String CHARA_DESC = "";




    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);

            // mBluetoothLeService.readCustomDescriptor(mCharaUUID, mServiceUUID);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case BluetoothLeService.ACTION_GATT_CONNECTED:
                    mConnected = true;
                    updateConnectionState(R.string.connected_state);

                    //  invalidateOptionsMenu();
                    break;
                case BluetoothLeService.ACTION_GATT_DISCONNECTED:
                    mConnected = false;
                    updateConnectionState(R.string.disconnected_state);
                    //  invalidateOptionsMenu();
                    break;
                case BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED:
                    break;
                case BluetoothLeService.ACTION_DATA_AVAILABLE:

                    String Rev_str = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                    String[] strArray = Rev_str.split("\\|");
                    Danger_Index.setText(Rev_str);

                    for(int i = 0;i<strArray.length;i++)
                    {
                        Danger_Index.setText(strArray[0]);
                        Mask_Count.setText(strArray[1]);
                        Target_Tem.setText(strArray[2]);
                        Vocs.setText(strArray[3]);
                        Co2.setText(strArray[4]);
                        EN_Tem.setText(strArray[5]);
                        En_Hum.setText(strArray[6]);
                    }
                    if ( Integer.valueOf(strArray[0])>=3) {
                        AlertDialog.Builder dialog = new AlertDialog.Builder(ControlDataActivity.this);
                        dialog.setTitle("WARNING!!注意！！");
                        dialog.setMessage(
                                "发热或不带口罩人员在附近\n" +
                                "People with fever or without mask are nearby");
                        dialog.setCancelable(true);
                        dialog.show();
                        //mReceiverAdapter.notifyItemInserted(0);
                    }
                   // displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                    break;
                case BluetoothLeService.ACTION_DESCRIPTOR_AVAILABLE:


                    Log.i("Receiving data", "Broadcast received");

                default:
                    break;
            }
        }
    };



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contorl_data_layout);

        final Intent intent = getIntent();
        Log.i("OnCreate", "Created");
        mDeviceName = intent.getStringExtra("DEVICE_NAME");
        mDeviceAddress = intent.getStringExtra("DEVICE_ADDRESS");

        mDataField = (TextView) findViewById(R.id.receive_text);

        // Sets up UI references.
        // ((TextView) findViewById(R.id.device_address_rxtx)).setText("Characteristic UUID: " + mCharaUUID);
        //  ((TextView) findViewById(R.id.characteristic_Descriptor)).setText("Characteristic Descriptor: " + CHARA_DESC);

        mConnectionState = (TextView) findViewById(R.id.connect_state3);
        mConnectionState.setText("Connected");

        Mask_Count = (TextView) findViewById(R.id.mask_count);
        Target_Tem = (TextView)findViewById(R.id.target_tem);
        Vocs = (TextView)findViewById(R.id.vocs);
        Co2 = (TextView)findViewById(R.id.co2);
        EN_Tem = (TextView)findViewById(R.id.env_tem);
        En_Hum = (TextView)findViewById(R.id.env_hum);
        Danger_Index = (TextView)findViewById(R.id.danger_index);

        Auto_Mode_Button = (Button) findViewById(R.id.auto_mode_btn);
        Night_Mode_Button = (Button) findViewById(R.id.night_mode_btn);
        Manual_Mode_Button = (Button) findViewById(R.id.manual_mode_btn);

        mswitch = (Switch)findViewById(R.id.switch2);

        mswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    //选中时 do some thing
                    String str = "C0";
                    mBluetoothLeService.writeCustomCharacteristic(str, mServiceUUID, mCharaUUID_TX);
                } else {
                    //非选中时 do some thing
                    String str = "C1";
                    mBluetoothLeService.writeCustomCharacteristic(str, mServiceUUID, mCharaUUID_TX);
                }
            }
        });

        Auto_Mode_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = "M0";
                mBluetoothLeService.writeCustomCharacteristic(str, mServiceUUID, mCharaUUID_TX);
            }
        });

        Night_Mode_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = "M1";
                mBluetoothLeService.writeCustomCharacteristic(str, mServiceUUID, mCharaUUID_TX);

            }
        });
        Manual_Mode_Button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String str = "M2";
                mBluetoothLeService.writeCustomCharacteristic(str, mServiceUUID, mCharaUUID_TX);
            }
        });



        //  checkProperties();
        //  getActionBar().setTitle(mDeviceName);
        //  getActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);


    }




    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }


    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }


    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
            }
        });
    }


    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.ACTION_DESCRIPTOR_AVAILABLE);
        return intentFilter;
    }
}

