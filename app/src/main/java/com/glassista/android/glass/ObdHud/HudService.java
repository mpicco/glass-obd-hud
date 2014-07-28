/*
 * Copyright (C) 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.glassista.android.glass.ObdHud;

import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import com.google.android.glass.timeline.LiveCard;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

/**
 * The main application service that manages the lifetime of the live card.
 */
public class HudService extends Service {
    private static final String TAG = HudRenderer.class.getSimpleName();
    private UUID MY_UUID = UUID.fromString("D04E3068-E15B-4482-8306-4CABFA1726E7");

    private static final String LIVE_CARD_TAG = "Hud";

    private LiveCard mLiveCard;
    private HudRenderer mRenderer;

    // Bluetooth client data
    public static final int REQUEST_TO_ENABLE_BT = 100;
    private BluetoothAdapter mBluetoothAdapter;

    // replace this with your own device names
    private final static String CBT_SERVER_DEVICE_NAME = "Nexus 5";


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mLiveCard == null) {
            // Set up live card

            mLiveCard = new LiveCard(this, LIVE_CARD_TAG);
            mRenderer = new HudRenderer(this);

            mLiveCard.setDirectRenderingEnabled(true);
            mLiveCard.getSurfaceHolder().addCallback(mRenderer);

            // Display the options menu when the live card is tapped.

            Intent menuIntent = new Intent(this, HudMenuActivity.class);
            menuIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            mLiveCard.setAction(PendingIntent.getActivity(this, 0, menuIntent, 0));

            mLiveCard.publish(LiveCard.PublishMode.REVEAL);

            // Set up bluetooth connection

            //mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            final BluetoothManager bluetoothManager =
                    (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();

            if (mBluetoothAdapter == null) {
                Log.v(TAG, "Device does not support Bluetooth");
                return -1;
            } else {
                if (!mBluetoothAdapter.isEnabled()) {
                    Log.v(TAG, "Bluetooth supported but not enabled");
                } else {
                    Log.v(TAG, "Bluetooth supported and enabled");
                    // discover new Bluetooth devices
                    discoverBluetoothDevices();

                    // find devices that have been paired
                    getBondedDevices();
                }
            }

        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mLiveCard != null && mLiveCard.isPublished()) {
            mLiveCard.unpublish();
            mLiveCard.getSurfaceHolder().removeCallback(mRenderer);
            mLiveCard = null;
        }
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    // for each device discovered, the broadcast info is received
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.v(TAG, "BroadcastReceiver on Receive - " + device.getName() + ": " + device.getAddress());
                String name = device.getName();

                // found another Android device of mine and start communication
                if (name != null && name.equalsIgnoreCase(CBT_SERVER_DEVICE_NAME)) {

                    new ConnectThread(device).start();
                }
            }
        }
    };

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == REQUEST_TO_ENABLE_BT) {
//            discoverBluetoothDevices();
//            getBondedDevices();
//            return;
//        }
//    }
//

    void discoverBluetoothDevices() {
        // register a BroadcastReceiver for the ACTION_FOUND Intent
        // to receive info about each device discovered.
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
        mBluetoothAdapter.startDiscovery();
    }


    // bonded devices are those that have already paired with the current device sometime in the past (and have not been unpaired)
    void getBondedDevices() {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                Log.v(TAG, "bonded device - " + device.getName() + ": " + device.getAddress());
                if (device.getName().equalsIgnoreCase(CBT_SERVER_DEVICE_NAME)) {
                    Log.d(TAG, CBT_SERVER_DEVICE_NAME);

                    new ConnectThread(device).start();
                    break;
                }
            }
        } else {
            Log.v(TAG, "No bonded devices");
        }
    }

    private class ConnectThread extends Thread {
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int bytesRead;
        int total;
        private final BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                Log.v(TAG, "before createRfcommSocketToServiceRecord");
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
                Log.v(TAG, "after createRfcommSocketToServiceRecord");
            } catch (IOException e) {
                Log.v(TAG, " createRfcommSocketToServiceRecord exception: " + e.getMessage());
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            mBluetoothAdapter.cancelDiscovery();
            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException e) {
                Log.v(TAG, e.getMessage());
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                }
                return;
            }
            manageConnectedSocket(mmSocket);
        }

        private void manageConnectedSocket(BluetoothSocket socket) {
            FileOutputStream fos = null;
            BufferedOutputStream bos = null;

            try {
                InputStream instream = socket.getInputStream();
                bytesRead = -1;
                total = 0;
                while ((bytesRead = instream.read(buffer)) > 0) {
                    total += bytesRead;

                    String timestamp = new String(buffer, 0, bytesRead);
                    Log.v(TAG, "bt [" + timestamp + "]");

                    mRenderer.setObdData(timestamp, 4500, 80, 6);    // TODO: get real data here

                }
                socket.close();
            } catch (IOException e) {
                try {
                    socket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "socket close exception:", e2);
                }
            }
        }
    }
}
