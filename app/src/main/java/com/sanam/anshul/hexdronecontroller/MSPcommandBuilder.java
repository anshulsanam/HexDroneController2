package com.sanam.anshul.hexdronecontroller;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Anshul on 7/31/2017.
 */

public class MSPcommandBuilder {
    public static final int
            MSP_IDENT                =100,
            MSP_STATUS               =101,
            MSP_RAW_IMU              =102,
            MSP_SERVO                =103,
            MSP_MOTOR                =104,
            MSP_RC                   =105,
            MSP_RAW_GPS              =106,
            MSP_COMP_GPS             =107,
            MSP_ATTITUDE             =108,
            MSP_ALTITUDE             =109,
            MSP_ANALOG               =110,
            MSP_RC_TUNING            =111,
            MSP_PID                  =112,
            MSP_BOX                  =113,
            MSP_MISC                 =114,
            MSP_MOTOR_PINS           =115,
            MSP_BOXNAMES             =116,
            MSP_PIDNAMES             =117,
            MSP_SERVO_CONF           =120,
            MSP_SET_RAW_RC           =200,
            MSP_SET_RAW_GPS          =201,
            MSP_SET_PID              =202,
            MSP_SET_BOX              =203,
            MSP_SET_RC_TUNING        =204,
            MSP_ACC_CALIBRATION      =205,
            MSP_MAG_CALIBRATION      =206,
            MSP_SET_MISC             =207,
            MSP_RESET_CONF           =208,
            MSP_SELECT_SETTING       =210,
            MSP_SET_HEAD             =211, // Not used
            MSP_SET_SERVO_CONF       =212,
            MSP_SET_MOTOR            =214,
            MSP_BIND                 =241,
            MSP_EEPROM_WRITE         =250,
            MSP_DEBUGMSG             =253,
            MSP_DEBUG                =254;

    public MSPcommandBuilder()
    {

    }
    public List<Byte> createCMD(int msp, Character[] payload)
    {
        if (msp < 0) {
            return null;
        }
        List<Byte> dataPackage = new LinkedList<Byte>();

        byte checksum = 0;
        dataPackage.add((byte)'$');
        dataPackage.add((byte)'M');
        dataPackage.add((byte)'<');

        byte size = (byte) ((payload != null ? (int) (payload.length) : 0) & 0xFF);
        dataPackage.add((byte) size);
        checksum ^= ((byte)size & 0xFF);

        dataPackage.add((byte) (msp & 0xFF));
        checksum ^= (msp & 0xFF);
        if (payload != null) {

            for (char c : payload) {
                dataPackage.add((byte) (c & 0xFF));
                checksum ^= (c & 0xFF);
            }
        }

        dataPackage.add(checksum);

        return dataPackage;


    }

    public byte[] concat(byte[] a, byte[] b) {
        int aLen = a.length;
        int bLen = b.length;
        byte[] c= new byte[aLen+bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }

}
