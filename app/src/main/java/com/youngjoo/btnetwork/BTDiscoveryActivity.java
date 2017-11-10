package com.youngjoo.btnetwork;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class BTDiscoveryActivity extends AppCompatActivity {
    private static final String TAG = "BTDiscoveryActivity";

    private static final int REQUEST_BT_ENABLE = 100;
    public static final int BLUETOOTH_STATE_UNKNOWN = -1;


    private MenuItem mProgressMenuItem;
    private ListView mDeviceListView;
    private Button mScanButton;
    private TextView mBluetoothStateTextView;
    private BluetoothAdapter mBluetoothAdapter;
    private Toolbar mToolbar;
    private ArrayAdapter<String> mBTArrayAdapter;
    private BluetoothStateReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btdiscovery);
        mReceiver = new BluetoothStateReceiver();

        //mToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        //setSupportActionBar(mToolbar);

        mScanButton = (Button) findViewById(R.id.scanButton);
        mBluetoothStateTextView = (TextView) findViewById(R.id.bluetooth_state_text_view);
        mDeviceListView = (ListView) findViewById(R.id.deivce_listView);
        mBTArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        mDeviceListView.setAdapter(mBTArrayAdapter);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if(mBluetoothAdapter == null){
            Log.i(TAG, "Device does not support bluetooth connectivity.");
            mBluetoothStateTextView.setText(R.string.BT_not_support);
            return;
        }

        if(mBluetoothAdapter.isEnabled()){
            if(mBluetoothAdapter.isDiscovering()){
                mBluetoothStateTextView.setText(R.string.BT_discovering);
            } else{
                mBluetoothStateTextView.setText(R.string.BT_enabled);
                mScanButton.setEnabled(true);
            }
        } else {
            mBluetoothStateTextView.setText(R.string.BT_disabled);
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_BT_ENABLE );
        }

        mScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mBluetoothAdapter.isDiscovering())
                    return; //already in a discovery mode.

                mBTArrayAdapter.clear();
                mProgressMenuItem.setVisible(true);
                mBluetoothAdapter.startDiscovery();
            }
        });

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_discovery, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        mProgressMenuItem = menu.findItem(R.id.action_progress);

        return super.onPrepareOptionsMenu(menu);
    }


    private class BluetoothStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent){
            String action = intent.getAction();
            if(action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mBTArrayAdapter.add(device.getName()+"\n"+device.getAddress());
                Log.i(TAG, "Found : "+device.getName()+" at "+device.getAddress());
                mBTArrayAdapter.notifyDataSetChanged();
            } else if(action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)){
                mProgressMenuItem.setVisible(false);
                Toast.makeText(context, "Scan complete.", Toast.LENGTH_LONG).show();
            } else {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BLUETOOTH_STATE_UNKNOWN);
                String message = null;
                switch(state){
                    case BluetoothAdapter.STATE_CONNECTED :
                        message = "STATE_CONNECTED";
                        break;
                    case BluetoothAdapter.STATE_CONNECTING :
                        message = "STATE_CONNECTING";
                        break;
                    case BluetoothAdapter.STATE_DISCONNECTED :
                        message = "STATE_DISCONNECTED";
                        break;
                    case BluetoothAdapter.STATE_DISCONNECTING :
                        message = "STATE_DISCONNECTING";
                        break;
                    case BluetoothAdapter.STATE_OFF :
                        message = "STATE_OFF";
                        break;
                    case BluetoothAdapter.STATE_ON :
                        message = "STATE_ON";
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF :
                        message = "STATE_TURNING_OFF";
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON :
                        message = "STATE_TURNING_ON";
                        break;
                    default :
                }
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();

            }
        }
    }
}
