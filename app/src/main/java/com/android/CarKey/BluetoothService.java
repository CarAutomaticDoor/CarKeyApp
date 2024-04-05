//package com.android.CarKey;
//
//import android.app.Activity;
//import android.app.Service;
//import android.bluetooth.BluetoothAdapter;
//import android.bluetooth.BluetoothDevice;
//import android.bluetooth.BluetoothSocket;
//import android.content.Intent;
//import android.os.Handler;
//import android.os.IBinder;
//import android.util.Log;
//
//import androidx.annotation.Nullable;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.util.UUID;
//
//public class BluetoothService extends Service {
//
//    private static final int REQUEST_CONNECT_DEVICE = 1;
//    private static final int REQUEST_ENABLE_BT = 2;
//
//    public static final int STATE_NONE = 1;
//    public static final int STATE_LISTEN = 2;
//    public static final int STATE_CONNECTING = 3;
//    public static final int STATE_CONNECTED = 4;
//    public static final int STATE_FAIL = 7;
//
//    private int mState;
//    public int mMode ;
//    private ConnectThread mConnectThread;
//    private ConnectedThread mConnectedThread;
//
//
//    private static final String TAG = "BluetoothService";
//    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
//
//    private BluetoothAdapter btAdapter;
//    private Activity mActivity;
//    private Handler mHandler;
//
//    public BluetoothService(Activity activity, Handler handler) {
//        mActivity = activity;
//        mHandler = handler;
//
////bluetoothAdapter 얻기
//        btAdapter = BluetoothAdapter.getDefaultAdapter();
//    }
//
//    @Nullable
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }
//
//    public boolean getDeviceState() {
//        Log.d(TAG, "Check the Bluetooth support");
//
//        if (btAdapter == null) {
//            Log.d(TAG, "Bluetooth is not available");
//            return false;
//        } else {
//            Log.e(TAG, "Bluetooth is available");
//            return true;
//        }
//    }
//
//    public void enableBluetooth() {
//        Log.i(TAG, "Check the enable Bluetooth");
//
//        if (btAdapter.isEnabled()) {
////기기의 블루투스 상태가 On일 경우..
//            Log.d(TAG, "Bluetooth Enable Now");
//            scanDevice();
//        } else {
////기기의 블루투스 상태가 off일 경우
//            Log.d(TAG, "Bluetooth Enable Request");
//
//            Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            mActivity.startActivityForResult(i, REQUEST_ENABLE_BT);
//        }
//    }
//
//    public void scanDevice() {
//        Log.d(TAG, "Scan Device");
//
//        Intent serverIntent = new Intent(mActivity, DeviceListActivity.class);
//        mActivity.startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
//
//    }
//
//    /* setState() : Bluetooth 상태를 set한다.*/
//    private synchronized void setState(int state) {
//        Log.d(TAG, "setState() " + mState + " -> " + state);
//        mState = state;
//
//// 핸들러를 통해 상태를 메인에 넘겨준다.
//        mHandler.obtainMessage(MainActivity.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
//    }
//
//
//    /* getState() : Bluetooth 상태를 get한다. */
//    public synchronized int getState() {
//        return mState;
//    }
//
//    /* start() : Thread관련 service를 시작합니다.*/
//    public synchronized void start() {
//        Log.d(TAG, "start");
//
//// Cancel any thread attempting to make a connection
//        if (mConnectThread == null) {
//
//        } else {
//            mConnectThread.cancel();
//            mConnectThread = null;
//        }
//    }
//
//    /* getDeviceInfo() : 기기의 주소를 가져와 정보를 connect 메소드에 넘긴다.*/
//    public void getDeviceInfo(Intent data)
//    {
////MAC address를 가져온다.
//        String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
////BluetoothDevice object를 가져온다
//        BluetoothDevice device = btAdapter.getRemoteDevice(address);
//
//        Log.d(TAG, "Get Device Info \n" + "address : "+address);
//
//        connect(device);
//    }
//    /* connect() : ConnectThread 초기화와 시작 device의 모든 연결 제거*/
//    public synchronized void connect(BluetoothDevice device) {
//        Log.d(TAG, "connect to: " + device);
//
//// Cancel any thread attempting to make a connection
//        if (mState == STATE_CONNECTING) {
//            if (mConnectThread == null) {
//
//            } else {
//                mConnectThread.cancel();
//                mConnectThread = null;
//            }
//        }
//
//// Cancel any thread currently running a connection
//        if (mConnectedThread == null) {
//
//        } else {
//            mConnectedThread.cancel();
//            mConnectedThread = null;
//        }
//
//// Start the thread to connect with the given device
//        mConnectThread = new ConnectThread(device);
//
//        mConnectThread.start();
//        setState(STATE_CONNECTING);
//    }
//
//    /* connected() : ConnectedThread 초기화*/
//    public synchronized void connected(BluetoothSocket socket,
//                                       BluetoothDevice device) {
//        Log.d(TAG, "connected");
//
//// Cancel the thread that completed the connection
//        if (mConnectThread == null) {
//
//        } else {
//            mConnectThread.cancel();
//            mConnectThread = null;
//        }
//
//// Cancel any thread currently running a connection
//        if (mConnectedThread == null) {
//
//        } else {
//            mConnectedThread.cancel();
//            mConnectedThread = null;
//        }
//
//// Start the thread to manage the connection and perform transmissions
//        mConnectedThread = new ConnectedThread(socket);
//        mConnectedThread.start();
//
//        setState(STATE_CONNECTED);
//    }
//
//    /* stop() : 모든 thread stop */
//    public synchronized void stop() {
//        Log.d(TAG, "stop");
//
//        if (mConnectThread != null) {
//            mConnectThread.cancel();
//            mConnectThread = null;
//        }
//
//        if (mConnectedThread != null) {
//            mConnectedThread.cancel();
//            mConnectedThread = null;
//        }
//
//        setState(STATE_NONE);
//    }
//    /* ConnectThread() : 소켓과 쓰레드를 생성하여 기기사이의 connecttion을 가능하게 합니다.*/
//    private class ConnectThread extends Thread
//    {
//        private final BluetoothSocket mmSocket;
//        private final BluetoothDevice mmDevice;
//
//        public ConnectThread(BluetoothDevice device)
//        {
//            mmDevice = device;
//            BluetoothSocket tmp = null;
//
////디바이스 정보를 얻어서 BluetoothSocket 생성
//            try
//            {
//                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
//            }
//            catch(IOException e)
//            {
//                Log.e(TAG, "create() failed",e);
//            }
//            mmSocket = tmp;
//        }
//
//        public void run()
//        {
//            Log.i(TAG, "BEGIN mConnectThread");
//            setName("ConnectThread");
//
//// 연결을 시도하기 전에는 항상 기기 검색을 중지한다.
//// 기기 검색이 계속되면 연결속도가 느려지기 때문이다.
//            btAdapter.cancelDiscovery();
//
//// BluetoothSocket 연결 시도
//            try
//            {
//// BluetoothSocket 연결 시도에 대한 return 값은 succes 또는 exception이다.
//                mmSocket.connect();
//                Log.d(TAG, "Connect Success");
//            }
//            catch(IOException e)
//            {
//                connectionFailed(); //연결 실패 시 불러오는 메소드
//                Log.d(TAG, "Connect Fail");
//
////소켓을 닫는다.
//                try
//                {
//                    mmSocket.close();
//                }
//                catch(IOException e2)
//                {
//                    Log.e(TAG, "unable to close() socket during connection failure", e2);
//                }
////연결 중 혹은 연결 대기상태인 메소드를 호출
//                BluetoothService.this.start();
//                return;
//            }
//// ConnectThread 클래스를 reset한다.
//            synchronized (BluetoothService.this) {
//                mConnectThread = null;
//            }
//// ConnectThread를 시작한다.
//            connected(mmSocket, mmDevice);
//        }
//
//        public void cancel() {
//            try {
//                mmSocket.close();
//            } catch (IOException e) {
//                Log.e(TAG, "close() of connect socket failed", e);
//            }
//        }
//    }
//    private class ConnectedThread extends Thread {
//        private final BluetoothSocket mmSocket;
//        private final InputStream mmInStream;
//        private final OutputStream mmOutStream;
//
//        public ConnectedThread(BluetoothSocket socket) {
//            Log.d(TAG, "create ConnectedThread");
//            mmSocket = socket;
//            InputStream tmpIn = null;
//            OutputStream tmpOut = null;
//
//// BluetoothSocket의 inputstream 과 outputstream을 얻는다.
//            try {
//                tmpIn = socket.getInputStream();
//                tmpOut = socket.getOutputStream();
//            } catch (IOException e) {
//                Log.e(TAG, "temp sockets not created", e);
//            }
//
//            mmInStream = tmpIn;
//            mmOutStream = tmpOut;
//        }
//
//        public void run() {
//            Log.i(TAG, "BEGIN mConnectedThread");
//            byte[] buffer = new byte[1024];
//            int bytes;
//
//// Keep listening to the InputStream while connected
//            while (true) {
//                try {
//// InputStream으로부터 값을 받는 읽는 부분(값을 받는다)
//                    bytes = mmInStream.read(buffer);
//
//                } catch (IOException e) {
//                    Log.e(TAG, "disconnected", e);
//                    connectionLost();
//                    break;
//                }
//            }
//        }
//
//        /**
//         * Write to the connected OutStream.
//         * @param msg The String to write
//         */
//        // synchronized write
//        public synchronized void write( String msg, int mode ) {
//            try {
//                mmOutStream.write(msg.getBytes()) ;
//                mMode = mode ;
//
//                if ( mode == MainActivity.MODE_REQUEST ) { //main에서 MODE_REQUEST모드일 때 다음 메시지를 보냄.
//// Share the sent message back to the UI Activity
//                    mHandler.obtainMessage(MainActivity.MESSAGE_WRITE, -1, -1, msg).sendToTarget(); //버퍼(burrer)에담은 메시지를 보내는 부분.
//                }
//
//            } catch (IOException e) {
//                Log.e(TAG, "Exception during write", e);
//            }
//        }
//
//
//        public void cancel() {
//            try {
//                mmSocket.close();
//            } catch (IOException e) {
//                Log.e(TAG, "close() of connect socket failed", e);
//            }
//        }
//    }
//    /* write() : 값을 쓰는 부분(보내는 부분) */
//    public void write( String msg, int mode ) {
//// ConnectedThread 객체 생성
//        ConnectedThread r ;
//
//        synchronized ( this ) {
//            if ( mState != STATE_CONNECTED ) return ; //블루투스에 연결되는 상태일 때는 값을 쓰지 않는다.
//            r = mConnectedThread ; //그렇지 않으면 쓰레드를 보내는 ConnectedThread클래스 객체 생성.
//        }
//
//        r.write(msg, mode) ; // ConnectedThread클래스 내에 있는 write함수를 호출하여 메시지를 보낸다.
//    }
//
//
//    /* connectionFailed() : 연결 실패했을때 */
//    private void connectionFailed() {
//        setState(STATE_FAIL);
//    }
//
//
//
//
//    /* connectionLost() : 연결을 잃었을 때 */
//    private void connectionLost() {
//        setState(STATE_LISTEN);
//    }
//
//}