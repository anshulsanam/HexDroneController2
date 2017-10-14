package com.sanam.anshul.hexdronecontroller;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.input.InputManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.app.Activity;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.InputEvent;
import android.view.MotionEvent;
import android.view.WindowManager;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import android.view.Menu;
import android.widget.SeekBar;
import android.widget.ToggleButton;

import static android.support.v4.math.MathUtils.clamp;
import static com.sanam.anshul.hexdronecontroller.Dpad.LEFT;
import static com.sanam.anshul.hexdronecontroller.Dpad.RIGHT;
import static com.sanam.anshul.hexdronecontroller.Dpad.UP;

import com.sanam.anshul.hexdronecontroller.inputmanagercompat.InputManagerCompat;
import com.sanam.anshul.hexdronecontroller.inputmanagercompat.InputManagerCompat.InputDeviceListener;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.os.Build;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import org.w3c.dom.Attr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class MainActivity extends Activity implements JoystickView.JoystickListener, JoystickViewR.JoystickListener, SensorEventListener, InputManager.InputDeviceListener {
    private final static String TAG = MainActivity.class.getSimpleName();



    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private TextView isSerial;
    private TextView mDataField;
    private Button  armButton;
    private Button disarmButton;

    private Button connectButton;
    private Button disconnectButton;

    private Button calib_imuButton;
    private Button calib_compassButton;

    private Button tiltControl;

    private Button joystickControl;

    private String mDeviceName;
    public String mDeviceAddress;
    //  private ExpandableListView mGattServicesList;
    private BluetoothLeService mBluetoothLeService;
    private boolean mConnected = false;
    private BluetoothGattCharacteristic characteristicTX;
    private BluetoothGattCharacteristic characteristicRX;

    private Transmitter rcTransmitter;
    private float xval;
    private float yval;

    private float xvalR;
    private float yvalR;

    private boolean armed = false;

    boolean paused = true;

    boolean fingerOnScreen;

    public final static UUID HM_RX_TX =
            UUID.fromString(SampleGattAttributes.HM_RX_TX);

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    int[] channels = { 1500, 1500, 1500, 1000};

    final Handler handler = new Handler();
    Timer timer = new Timer();
    TimerTask doAsynchronousTask;

    double sensorX;
    float pitch;
    float roll;
    float yaw;

    float Joythrottle, Joyyaw, Joyroll, Joypitch;

    private SensorManager mSensorManager = null;

    // angular speeds from gyro
    private float[] gyro = new float[3];

    // rotation matrix from gyro data
    private float[] gyroMatrix = new float[9];

    // orientation angles from gyro matrix
    private float[] gyroOrientation = new float[3];

    // magnetic field vector
    private float[] magnet = new float[3];

    // accelerometer vector
    private float[] accel = new float[3];

    // orientation angles from accel and magnet
    private float[] accMagOrientation = new float[3];

    // final orientation angles from sensor fusion
    private float[] fusedOrientation = new float[3];

    // accelerometer and magnetometer based rotation matrix
    private float[] rotationMatrix = new float[9];

    public static final int TIME_CONSTANT = 10;
    public static final float FILTER_COEFFICIENT = 0.98f;
    private Timer fuseTimer = new Timer();


    private static final boolean ADAPTIVE_ACCEL_FILTER = true;
    float lastAccel[] = new float[3];
    float accelFilter[] = new float[3];

    public boolean useJoystick = true;
    public boolean useGamepad = false;

    Dpad mDpad = new Dpad();


    public ArrayList getGameControllerIds() {
        ArrayList gameControllerDeviceIds = new ArrayList();
        int[] deviceIds = InputDevice.getDeviceIds();
        for (int deviceId : deviceIds) {
            InputDevice dev = InputDevice.getDevice(deviceId);
            int sources = dev.getSources();

            // Verify that the device has gamepad buttons, control sticks, or both.
            if (((sources & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD)
                    || ((sources & InputDevice.SOURCE_JOYSTICK)
                    == InputDevice.SOURCE_JOYSTICK)) {
                // This device is a game controller. Store its device ID.
                if (!gameControllerDeviceIds.contains(deviceId)) {
                    gameControllerDeviceIds.add(deviceId);
                }
            }
        }
        return gameControllerDeviceIds;
    }



    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {

        // Check that the event came from a game controller
        if ((event.getSource() & InputDevice.SOURCE_JOYSTICK) ==
                InputDevice.SOURCE_JOYSTICK &&
                event.getAction() == MotionEvent.ACTION_MOVE) {

            // Process all historical movement samples in the batch
            final int historySize = event.getHistorySize();

            // Process the movements starting from the
            // earliest historical position in the batch
            for (int i = 0; i < historySize; i++) {
                // Process the event at historical position i
                processJoystickInput(event, i);
            }

            // Process the current movement sample in the batch (position -1)
            processJoystickInput(event, -1);
            return true;
        }
        return super.onGenericMotionEvent(event);
    }
    private static float getCenteredAxis(MotionEvent event,
                                         InputDevice device, int axis, int historyPos) {
        final InputDevice.MotionRange range =
                device.getMotionRange(axis, event.getSource());

        // A joystick at rest does not always report an absolute position of
        // (0,0). Use the getFlat() method to determine the range of values
        // bounding the joystick axis center.
        if (range != null) {
            final float flat = range.getFlat();
            final float value =
                    historyPos < 0 ? event.getAxisValue(axis):
                            event.getHistoricalAxisValue(axis, historyPos);

            // Ignore axis values that are within the 'flat' region of the
            // joystick axis center.
            if (Math.abs(value) > flat) {
                return value;
            }
        }
        return 0;
    }
    private void processJoystickInput(MotionEvent event,
                                      int historyPos) {

        InputDevice mInputDevice = event.getDevice();


        // Calculate the horizontal distance to move by
        // using the input value from one of these physical controls:
        // the left control stick, hat axis, or the right control stick.
        Joyyaw = smoothenOutput(getCenteredAxis(event, mInputDevice,
                MotionEvent.AXIS_X, historyPos), 1.1f);
//        if (x == 0) {
//            x = getCenteredAxis(event, mInputDevice,
//                    MotionEvent.AXIS_HAT_X, historyPos);
//        }
        //if (x == 0) {
        Joyroll = smoothenOutput(getCenteredAxis(event, mInputDevice,
                    MotionEvent.AXIS_Z, historyPos), 1.1f);

        //}

        // Calculate the vertical distance to move by
        // using the input value from one of these physical controls:
        // the left control stick, hat switch, or the right control stick.
        Joythrottle = smoothenOutput(getCenteredAxis(event, mInputDevice,
                MotionEvent.AXIS_Y, historyPos), 1f);
//        if (y == 0) {
//            y = getCenteredAxis(event, mInputDevice,
//                    MotionEvent.AXIS_HAT_Y, historyPos);
//        }
        //if (y == 0) {
        Joypitch = smoothenOutput(getCenteredAxis(event, mInputDevice,
                    MotionEvent.AXIS_RZ, historyPos), 1.1f);
        //}


        //System.out.println("Throttle: " + throttle + "  " + "Yaw: " + yaw + "   " + "Roll: " + roll + "   " + "Pitch: " + pitch);
        // Update the ship object based on the new x and y values
    }

    public float smoothenOutput(float joy, float p)
    {

        if (joy > 0)
            joy = (float) Math.pow(joy, p);
        else
            joy = (float) -Math.pow(-joy, p);

        return joy;
    }

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
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };


    public float map(float x, float in_min, float in_max, float out_min, float out_max)
    {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }

    public float constrain(float x, float a, float b) {
        if(x < a) {
            return a;
        }
        else if(b < x) {
            return b;
        }
        else
            return x;
    }

    public static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(mBluetoothLeService.EXTRA_DATA));
            }
        }
    };

    private void clearUI() {
        //mDataField.setText(R.string.no_data);
    }

    public void callAsynchronousTask() {

        doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            //System.out.println("X percent: " + getJoystickX() + " Y percent: " + getJoystickY());
                            //System.out.println(fingerOnScreen);
                            float oneMinusCoeff = 1.0f - FILTER_COEFFICIENT;
                            fusedOrientation[0] =
                                    FILTER_COEFFICIENT * gyroOrientation[0]
                                            + oneMinusCoeff * accMagOrientation[0];

                            fusedOrientation[1] =
                                    FILTER_COEFFICIENT * gyroOrientation[1]
                                            + oneMinusCoeff * accMagOrientation[1];

                            fusedOrientation[2] =
                                    FILTER_COEFFICIENT * gyroOrientation[2]
                                            + oneMinusCoeff * accMagOrientation[2];





                            // overwrite gyro matrix and orientation with fused orientation
                            // to comensate gyro drift
                            //sensorX = fusedOrientation[0];
                            yaw = round(fusedOrientation[0], 2);
                            roll = round(fusedOrientation[1], 2);
                            pitch = round(fusedOrientation[2], 2);

                            //System.out.println("Roll: " + roll + "    " + "Pitch: " + pitch);

                            if(paused==false)
                            {
//                                channels[0] = (int) constrain( map(roll, -0.7f, 0.7f, 2000f, 1000f), 1000f, 2000f);
//                                channels[1] = (int) constrain( map(pitch, -0.7f, 0.7f, 1000f, 2000f), 1000f, 2000f);
                                if(useGamepad)
                                {
                                    channels[3] = (int) (map(Joythrottle, 0f, -1f, 1000f, 2000f));
                                    channels[2] = (int) (map(Joyyaw, -1f, 1f, 1000f, 2000f));
                                    channels[0] = (int) (map(Joyroll, -1f, 1f, 1000f, 2000f));
                                    channels[1] = (int) (map(Joypitch, 1f, -1f, 1000f, 2000f));



                                }

                                else if(useJoystick) {
                                    channels[0] = (int) getJoystickXR();
                                    channels[1] = (int) getJoystickYR();
                                    //System.out.println("Roll: " + channels[0] + "    " + "Pitch: " + channels[1]);
                                }

                                else
                                {
                                    channels[0] = (int) constrain( map(roll, -0.7f, 0.7f, 2000f, 1000f), 1000f, 2000f);
                                    channels[1] = (int) constrain( map(pitch, -0.7f, 0.7f, 1000f, 2000f), 1000f, 2000f);
                                    //System.out.println("Roll: " + channels[0] + "    " + "Pitch: " + channels[1]);
                                }

                                channels[2] = (int) getJoystickX();
                                                                         //channels[2] = (int) constrain( map(yaw, -0.7f, 0.7f, 1000f, 2000f), 1000f, 2000f);
                                channels[3] = (int) getJoystickY();

                                //System.out.println(channels[0]  + " " + channels[1] + " " + channels[2] + " " + channels[3]);
                                writeValue(rcTransmitter.sendRC(channels));
                            }



                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                        }
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, 20);

    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//
//        for(int i=0; i<event.getPointerCount();i++) {
//            int x = (int) event.getX();
//            int y = (int) event.getY();
//            switch (event.getAction()) {
//                case MotionEvent.ACTION_DOWN:
//                    // System.out.println(x);
//                    if (x > 900) {
//                        fingerOnScreen = true;
//                    }
//                    break;
//                case MotionEvent.ACTION_MOVE:
//                    break;
//
//                case MotionEvent.ACTION_UP:
//                    if (x > 900) {
//                        fingerOnScreen = false;
//                    }
//                    break;
//            }
//        }
//        return false;
//    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gyroOrientation[0] = 0.0f;
        gyroOrientation[1] = 0.0f;
        gyroOrientation[2] = 0.0f;

        // initialise gyroMatrix with identity matrix
        gyroMatrix[0] = 1.0f; gyroMatrix[1] = 0.0f; gyroMatrix[2] = 0.0f;
        gyroMatrix[3] = 0.0f; gyroMatrix[4] = 1.0f; gyroMatrix[5] = 0.0f;
        gyroMatrix[6] = 0.0f; gyroMatrix[7] = 0.0f; gyroMatrix[8] = 1.0f;

        // get sensorManager and initialise sensor listeners
        mSensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        initListeners();
//        fuseTimer.scheduleAtFixedRate(new calculateFusedOrientationTask(),
//                0, TIME_CONSTANT);

        callAsynchronousTask();


        connectButton = (Button) findViewById(R.id.connect);
        disconnectButton = (Button) findViewById(R.id.disconnect);
        disconnectButton.setVisibility(View.GONE);
        connectButton.setVisibility(View.GONE);
        calib_imuButton = (Button) findViewById(R.id.calib_imu);
        calib_compassButton = (Button) findViewById(R.id.calib_compass);

        tiltControl = (Button) findViewById(R.id.tilt);
        joystickControl = (Button) findViewById(R.id.joy);


        JoystickView joystick = new JoystickView(this, 0, 1);
        final JoystickViewR joystickR = new JoystickViewR(this, 0, 1);

        getWindow().getDecorView().setBackgroundColor(getResources().getColor(R.color.colorPrimaryLight));

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        //mConnectionState = (TextView) findViewById(R.id.connection_state);
        // is serial present?
        //isSerial = (TextView) findViewById(R.id.connection_state);

        //mDataField = (TextView) findViewById(R.id.data_value);

        armButton = (Button) findViewById(R.id.arm_btn);
        disarmButton = (Button) findViewById(R.id.disarm_btn);
        disarmButton.setVisibility(View.GONE);
        joystickControl.setVisibility(View.GONE);




        rcTransmitter = new Transmitter();

        connectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mBluetoothLeService.connect(mDeviceAddress);
            }
        });
        disconnectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mBluetoothLeService.disconnect();
            }
        });


        armButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                paused = true;

                Timer t = new Timer();

                t.scheduleAtFixedRate(new TimerTask() {
                    long t0 = System.currentTimeMillis();

                    @Override
                    public void run() {
                        if (System.currentTimeMillis() - t0 > 0.5 * 1000) {
                            cancel();
                            paused = false;

                        } else {
                            writeValue(rcTransmitter.arm());
                        }
                    }

                },0,50);

                disarmButton.setVisibility(View.VISIBLE);
                armButton.setVisibility(View.GONE);
                armed = true;
           }

        });
        disarmButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                paused = true;
                Timer t = new Timer();
                t.scheduleAtFixedRate(new TimerTask() {
                    long t0 = System.currentTimeMillis();

                    @Override
                    public void run() {
                        if (System.currentTimeMillis() - t0 > 0.5 * 1000) {
                            cancel();
                            paused = false;
                        } else {
                            writeValue(rcTransmitter.disarm());
                        }
                    }

                },0,50);
                armButton.setVisibility(View.VISIBLE);
                disarmButton.setVisibility(View.GONE);
                armed = false;
            }
        });
        calib_imuButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                paused = true;
                Timer t = new Timer();
                t.scheduleAtFixedRate(new TimerTask() {
                    long t0 = System.currentTimeMillis();

                    @Override
                    public void run() {
                        if (System.currentTimeMillis() - t0 > 0.5 * 1000) {
                            cancel();
                            paused = false;
                        } else {
                            writeValue(rcTransmitter.CalibrateIMU());
                        }
                    }

                },0,50);
            }
        });
        calib_compassButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                paused = true;
                Timer t = new Timer();
                t.scheduleAtFixedRate(new TimerTask() {
                    long t0 = System.currentTimeMillis();

                    @Override
                    public void run() {
                        if (System.currentTimeMillis() - t0 > 0.5 * 1000) {
                            cancel();
                            paused = false;
                        } else {
                            writeValue(rcTransmitter.CalibrateCompass());
                        }
                    }

                },0,50);
            }
        });

        tiltControl.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                useJoystick = false;
                tiltControl.setVisibility(View.INVISIBLE);
                joystickControl.setVisibility(View.VISIBLE);
            }
        });

        joystickControl.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                useJoystick = true;
                joystickControl.setVisibility(View.INVISIBLE);

                tiltControl.setVisibility(View.VISIBLE);
            }
        });


        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
        //getActionBar().hide();
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
              | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }
    public void initListeners(){
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_FASTEST);

        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                SensorManager.SENSOR_DELAY_FASTEST);

        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_FASTEST);
    }
    public void onJoystickMoved(float xPercent, float yPercent, int id)
    {
        //System.out.println("X percent: " + xPercent + " Y percent: " + yPercent);

        switch(id)
        {
            case R.id.joystick:
                xval = xPercent;

                yval = yPercent;

                break;


        }

    }

    public void onJoystickMovedR(float xPercent, float yPercent, int id)
    {
        //System.out.println("X percent: " + xPercent + " Y percent: " + yPercent);

        switch(id)
        {
            case R.id.joystickR:
                xvalR = xPercent;

                yvalR = yPercent;

                break;


        }

    }

    public float getJoystickX()
    {
        float RCx = map(xval, -1, 1, 1000, 2000);
        return RCx;
    }

    public float getJoystickY()
    {
        float RCy = map(yval, -1, 1, 2000, 1000);
        return RCy;
    }

    public float getJoystickXR()
    {
        float RCx = map(xvalR, -1, 1, 1000, 2000);
        return RCx;
    }

    public float getJoystickYR()
    {
        float RCy = map(yvalR, -1, 1, 2000, 1000);
        return RCy;
    }

    public void writeValue(String strValue)
    {
        if (mConnected) {
            characteristicTX.setValue(strValue.getBytes());
            mBluetoothLeService.writeCharacteristic(characteristicTX);
        }
    }

    public void writeValue(List<Byte> msp)
    {

            byte[] arr = new byte[msp.size()];
            int i = 0;
            for (byte b : msp) {
                arr[i++] = b;
            }
            if(mConnected) {

                characteristicTX.setValue(arr);
                mBluetoothLeService.writeCharacteristic(characteristicTX);
            }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);

        if (mConnected) {
            //menu.findItem(R.id.menu_connect).setVisible(false);
            //menu.findItem(R.id.menu_disconnect).setVisible(false);
            connectButton.setVisibility(View.GONE);
            disconnectButton.setVisibility(View.VISIBLE);

        } else {
            //menu.findItem(R.id.menu_connect).setVisible(false);
            //menu.findItem(R.id.menu_disconnect).setVisible(false);
            disconnectButton.setVisibility(View.GONE);
            connectButton.setVisibility(View.VISIBLE);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {

            case android.R.id.home:
                timer.cancel();
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //isSerial.setText(resourceId);
            }
        });
    }

    private void displayData(String data) {

        if (data != null) {
            //mDataField.setText(data);
        }
    }


    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();


        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));

            // If the service exists for HM 10 Serial, say so.
            if(SampleGattAttributes.lookup(uuid, unknownServiceString) == "HM 10 Serial" && mConnected)
            {
                //isSerial.setText("Connected");
            }
            else
            {
                //isSerial.setText("Disconnected");
            }
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            // get characteristic when UUID matches RX/TX UUID
            characteristicTX = gattService.getCharacteristic(BluetoothLeService.UUID_HM_RX_TX);
            characteristicRX = gattService.getCharacteristic(BluetoothLeService.UUID_HM_RX_TX);
        }

    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch(event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                // copy new accelerometer data into accel array
                // then calculate new orientation
                System.arraycopy(event.values, 0, accel, 0, 3);
                calculateAccMagOrientation();
                break;

            case Sensor.TYPE_GYROSCOPE:
                // process gyro data
                gyroFunction(event);
                break;

            case Sensor.TYPE_MAGNETIC_FIELD:
                // copy new magnetometer data into magnet array
                System.arraycopy(event.values, 0, magnet, 0, 3);
                break;
        }
    }
    public void calculateAccMagOrientation() {
        if(SensorManager.getRotationMatrix(rotationMatrix, null, accel, magnet)) {
            SensorManager.getOrientation(rotationMatrix, accMagOrientation);
        }
    }

    public static final float EPSILON = 0.000000001f;

    private void getRotationVectorFromGyro(float[] gyroValues,
                                           float[] deltaRotationVector,
                                           float timeFactor)
    {
        float[] normValues = new float[3];

        // Calculate the angular speed of the sample
        float omegaMagnitude =
                (float)Math.sqrt(gyroValues[0] * gyroValues[0] +
                        gyroValues[1] * gyroValues[1] +
                        gyroValues[2] * gyroValues[2]);

        // Normalize the rotation vector if it's big enough to get the axis
        if(omegaMagnitude > EPSILON) {
            normValues[0] = gyroValues[0] / omegaMagnitude;
            normValues[1] = gyroValues[1] / omegaMagnitude;
            normValues[2] = gyroValues[2] / omegaMagnitude;
        }

        // Integrate around this axis with the angular speed by the timestep
        // in order to get a delta rotation from this sample over the timestep
        // We will convert this axis-angle representation of the delta rotation
        // into a quaternion before turning it into the rotation matrix.
        float thetaOverTwo = omegaMagnitude * timeFactor;
        float sinThetaOverTwo = (float)Math.sin(thetaOverTwo);
        float cosThetaOverTwo = (float)Math.cos(thetaOverTwo);
        deltaRotationVector[0] = sinThetaOverTwo * normValues[0];
        deltaRotationVector[1] = sinThetaOverTwo * normValues[1];
        deltaRotationVector[2] = sinThetaOverTwo * normValues[2];
        deltaRotationVector[3] = cosThetaOverTwo;
    }

    private static final float NS2S = 1.0f / 1000000000.0f;
    private float timestamp;
    private boolean initState = true;

    public void gyroFunction(SensorEvent event) {
        // don't start until first accelerometer/magnetometer orientation has been acquired
        if (accMagOrientation == null)
            return;

        // initialisation of the gyroscope based rotation matrix
        if(initState) {
            float[] initMatrix = new float[9];
            initMatrix = getRotationMatrixFromOrientation(accMagOrientation);
            float[] test = new float[3];
            SensorManager.getOrientation(initMatrix, test);
            gyroMatrix = matrixMultiplication(gyroMatrix, initMatrix);
            initState = false;
        }

        // copy the new gyro values into the gyro array
        // convert the raw gyro data into a rotation vector
        float[] deltaVector = new float[4];
        if(timestamp != 0) {
            final float dT = (event.timestamp - timestamp) * NS2S;
            System.arraycopy(event.values, 0, gyro, 0, 3);
            getRotationVectorFromGyro(gyro, deltaVector, dT / 2.0f);
        }

        // measurement done, save current time for next interval
        timestamp = event.timestamp;

        // convert rotation vector into rotation matrix
        float[] deltaMatrix = new float[9];
        SensorManager.getRotationMatrixFromVector(deltaMatrix, deltaVector);

        // apply the new rotation interval on the gyroscope based rotation matrix
        gyroMatrix = matrixMultiplication(gyroMatrix, deltaMatrix);

        // get the gyroscope based orientation from the rotation matrix
        SensorManager.getOrientation(gyroMatrix, gyroOrientation);
    }

    private float[] getRotationMatrixFromOrientation(float[] o) {
        float[] xM = new float[9];
        float[] yM = new float[9];
        float[] zM = new float[9];

        float sinX = (float)Math.sin(o[1]);
        float cosX = (float)Math.cos(o[1]);
        float sinY = (float)Math.sin(o[2]);
        float cosY = (float)Math.cos(o[2]);
        float sinZ = (float)Math.sin(o[0]);
        float cosZ = (float)Math.cos(o[0]);

        // rotation about x-axis (pitch)
        xM[0] = 1.0f; xM[1] = 0.0f; xM[2] = 0.0f;
        xM[3] = 0.0f; xM[4] = cosX; xM[5] = sinX;
        xM[6] = 0.0f; xM[7] = -sinX; xM[8] = cosX;

        // rotation about y-axis (roll)
        yM[0] = cosY; yM[1] = 0.0f; yM[2] = sinY;
        yM[3] = 0.0f; yM[4] = 1.0f; yM[5] = 0.0f;
        yM[6] = -sinY; yM[7] = 0.0f; yM[8] = cosY;

        // rotation about z-axis (azimuth)
        zM[0] = cosZ; zM[1] = sinZ; zM[2] = 0.0f;
        zM[3] = -sinZ; zM[4] = cosZ; zM[5] = 0.0f;
        zM[6] = 0.0f; zM[7] = 0.0f; zM[8] = 1.0f;

        // rotation order is y, x, z (roll, pitch, azimuth)
        float[] resultMatrix = matrixMultiplication(xM, yM);
        resultMatrix = matrixMultiplication(zM, resultMatrix);
        return resultMatrix;
    }

    private float[] matrixMultiplication(float[] A, float[] B) {
        float[] result = new float[9];

        result[0] = A[0] * B[0] + A[1] * B[3] + A[2] * B[6];
        result[1] = A[0] * B[1] + A[1] * B[4] + A[2] * B[7];
        result[2] = A[0] * B[2] + A[1] * B[5] + A[2] * B[8];

        result[3] = A[3] * B[0] + A[4] * B[3] + A[5] * B[6];
        result[4] = A[3] * B[1] + A[4] * B[4] + A[5] * B[7];
        result[5] = A[3] * B[2] + A[4] * B[5] + A[5] * B[8];

        result[6] = A[6] * B[0] + A[7] * B[3] + A[8] * B[6];
        result[7] = A[6] * B[1] + A[7] * B[4] + A[8] * B[7];
        result[8] = A[6] * B[2] + A[7] * B[5] + A[8] * B[8];

        return result;
    }

    @Override
    public void onInputDeviceAdded(int i) {

    }

    @Override
    public void onInputDeviceRemoved(int i) {

    }

    @Override
    public void onInputDeviceChanged(int i) {

    }

    class calculateFusedOrientationTask extends TimerTask {
        public void run() {
            float oneMinusCoeff = 1.0f - FILTER_COEFFICIENT;
            fusedOrientation[0] =
                    FILTER_COEFFICIENT * gyroOrientation[0]
                            + oneMinusCoeff * accMagOrientation[0];

            fusedOrientation[1] =
                    FILTER_COEFFICIENT * gyroOrientation[1]
                            + oneMinusCoeff * accMagOrientation[1];

            fusedOrientation[2] =
                    FILTER_COEFFICIENT * gyroOrientation[2]
                            + oneMinusCoeff * accMagOrientation[2];

            // overwrite gyro matrix and orientation with fused orientation
            // to comensate gyro drift
            //sensorX = fusedOrientation[0];
            roll = fusedOrientation[1];
            pitch = fusedOrientation[2];
            gyroMatrix = getRotationMatrixFromOrientation(fusedOrientation);
            System.arraycopy(fusedOrientation, 0, gyroOrientation, 0, 3);
        }
    }

//    public double getPitch()
//    {
//        return(map(pitch, -0.7f, 0.7f, 1000f, 2000f));
//
//    }
//
//    public double getRoll()
//    {
//        return(map(roll, 0.7f, -0.7f, 1000f, 2000f));
//
//    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    // on change of bars write char


}

