package com.sanam.anshul.hexdronecontroller;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Anshul on 8/1/2017.
 */

public class Transmitter
{

    public Transmitter()
    {

    }

    public List<Byte> arm()
    {
        int[] channels = { 1500,1500,2000,1000};


        MSPcommandBuilder MSPcommandBuilder = new MSPcommandBuilder();
        ArrayList<Character> payload = new ArrayList<Character>();
        for (int i = 0; i < channels.length; i++) {
            payload.add((char) (channels[i] & 0xFF));
            payload.add((char) ((channels[i] >> 8) & 0xFF));
        }


        return MSPcommandBuilder.createCMD(MSPcommandBuilder.MSP_SET_RAW_RC, payload.toArray(new Character[payload.size()]));
    }
    public List<Byte> start()
    {
        int[] channels = { 1500,1500,1500,1000};


        MSPcommandBuilder MSPcommandBuilder = new MSPcommandBuilder();
        ArrayList<Character> payload = new ArrayList<Character>();
        for (int i = 0; i < channels.length; i++) {
            payload.add((char) (channels[i] & 0xFF));
            payload.add((char) ((channels[i] >> 8) & 0xFF));
        }


        return MSPcommandBuilder.createCMD(MSPcommandBuilder.MSP_SET_RAW_RC, payload.toArray(new Character[payload.size()]));
    }

    public List<Byte> disarm()
    {
        int[] channels = { 1500,1500,1000,1000};


        MSPcommandBuilder MSPcommandBuilder = new MSPcommandBuilder();
        ArrayList<Character> payload = new ArrayList<Character>();
        for (int i = 0; i < channels.length; i++) {
            payload.add((char) (channels[i] & 0xFF));
            payload.add((char) ((channels[i] >> 8) & 0xFF));
        }


        return MSPcommandBuilder.createCMD(MSPcommandBuilder.MSP_SET_RAW_RC, payload.toArray(new Character[payload.size()]));
    }

    public List<Byte> CalibrateIMU()
    {
        int[] channels = { 1500,1000,1000,2000};


        MSPcommandBuilder MSPcommandBuilder = new MSPcommandBuilder();
        ArrayList<Character> payload = new ArrayList<Character>();
        for (int i = 0; i < channels.length; i++) {
            payload.add((char) (channels[i] & 0xFF));
            payload.add((char) ((channels[i] >> 8) & 0xFF));
        }


        return MSPcommandBuilder.createCMD(MSPcommandBuilder.MSP_SET_RAW_RC, payload.toArray(new Character[payload.size()]));
    }
    public List<Byte> CalibrateCompass()
    {
        int[] channels = { 1500,1000,2000,2000};


        MSPcommandBuilder MSPcommandBuilder = new MSPcommandBuilder();
        ArrayList<Character> payload = new ArrayList<Character>();
        for (int i = 0; i < channels.length; i++) {
            payload.add((char) (channels[i] & 0xFF));
            payload.add((char) ((channels[i] >> 8) & 0xFF));
        }


        return MSPcommandBuilder.createCMD(MSPcommandBuilder.MSP_SET_RAW_RC, payload.toArray(new Character[payload.size()]));
    }

    public List<Byte> sendRC(int[] channels1)
    {
        MSPcommandBuilder MSPcommandBuilder = new MSPcommandBuilder();
        ArrayList<Character> payload = new ArrayList<Character>();
        for (int i = 0; i < channels1.length; i++) {
            payload.add((char) (channels1[i] & 0xFF));
            payload.add((char) ((channels1[i] >> 8) & 0xFF));
        }


        return MSPcommandBuilder.createCMD(MSPcommandBuilder.MSP_SET_RAW_RC, payload.toArray(new Character[payload.size()]));
    }










}
