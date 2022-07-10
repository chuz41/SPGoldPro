package com.example.spgold.Util;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Iushaheed on 1/14/2018. Modified by Ing. Jesus Barrantes Ramirez on 1/4/2022
 */

public class BluetoothUtil {
    private static final UUID PRINTER_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    //private static final String Innerprinter_Address = "00:11:22:33:44:55";//SunmiV2
    //private static final String Innerprinter_Address = "20:18:05:22:03:06";
    //private static final String Innerprinter_Address = "66:22:0B:15:5C:87";
    private static final String Innerprinter_Address = "66:22:4F:58:E8:C3";//GOOJPRT Portable prir
    public static BluetoothAdapter getBTAdapter() {
        return BluetoothAdapter.getDefaultAdapter();
    }

    public static BluetoothDevice getDevice(BluetoothAdapter bluetoothAdapter) {
        BluetoothDevice innerprinter_device = null;
        @SuppressLint("MissingPermission") Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : devices) {
            if (device.getAddress().equals(Innerprinter_Address)) {
                innerprinter_device = device;
                break;
            }
        }
        return innerprinter_device;
    }
    @SuppressLint("MissingPermission")
    public static BluetoothSocket getSocket(BluetoothDevice device) throws IOException {
        @SuppressLint("MissingPermission") BluetoothSocket socket = device.createRfcommSocketToServiceRecord(PRINTER_UUID);
        socket.connect();
        return socket;
    }

    public static void sendData(byte[] bytes, BluetoothSocket socket) throws IOException {
        OutputStream out = socket.getOutputStream();
        out.write(bytes, 0, bytes.length);
        out.close();
    }
}
