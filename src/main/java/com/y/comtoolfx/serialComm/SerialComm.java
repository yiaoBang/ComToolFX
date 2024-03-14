package com.y.comtoolfx.serialComm;

import com.fazecast.jSerialComm.SerialPort;
import com.y.comtoolfx.tools.TimeUtils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * @author Y
 * @version 1.0
 * @date 2024/3/14 15:18
 */
public class SerialComm implements AutoCloseable {
    protected SerialPort serialPort;
    protected String serialPortName;
    protected int baudRate = 9600;
    protected int dataBits = 8;
    protected int stopBits = 1;
    protected int parity = 0;
    protected int flowControl = 0;
    protected MessageListener messageListener;
    protected byte[] messageDelimiter = new byte[0];
    protected volatile boolean open = false;
    protected SerialComm() {
        messageListener = new MessageListener(this);
    }
    protected SerialComm(byte[] messageDelimiter) {
        this.messageDelimiter = messageDelimiter;
        messageListener = new MessageListener(this);
    }

    public static SerialPort[] getSerialPorts() {
        return SerialPort.getCommPorts();
    }

    /**
     * 更新侦听器
     *
     * @param bytes 字节
     */
    protected void updateListener(byte[] bytes) {
        messageDelimiter = bytes;
        messageListener = new MessageListener(this);
        serialPort.removeDataListener();
        serialPort.addDataListener(messageListener);
    }

    protected void listen(byte[] bytes) {
        System.out.println("收到" + Arrays.toString(bytes));
    }

    protected String text(byte[] bytes) {
        return new String(bytes, 0, bytes.length - (messageDelimiter == null ? 0 : messageDelimiter.length));
    }

    protected String textAndTime(byte[] bytes) {
        return new String(bytes, 0, bytes.length - (messageDelimiter == null ? 0 : messageDelimiter.length)) + " " + "[" + TimeUtils.getNow() + "]";

    }

    protected void getSerial() {
        close();
        for (SerialPort serial : getSerialPorts()) {
            if (serial.getSystemPortName().equals(serialPortName)) {
                serialPort = serial;
                return;
            }
        }
        serialPort = null;
    }

    protected boolean openPort() {
        getSerial();
        if (serialPort != null) {
            serialPort.setComPortParameters(baudRate, dataBits, stopBits, parity);
            serialPort.setFlowControl(flowControl);
            serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING | SerialPort.TIMEOUT_WRITE_BLOCKING, 100, 100);
            serialPort.addDataListener(messageListener);
            open = serialPort.openPort();
        }
        return open;
    }

    protected int write(byte[] bytes) {
        if (open) {
            return serialPort.writeBytes(bytes, bytes.length);
        }
        return 0;
    }

    protected int write(String message) {
        return write(message.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void close() {
        if (serialPort != null) {
            serialPort.removeDataListener();
            serialPort.closePort();
        }
    }

}
