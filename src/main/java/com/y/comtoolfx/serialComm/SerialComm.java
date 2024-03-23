package com.y.comtoolfx.serialComm;

import com.fazecast.jSerialComm.SerialPort;
import com.y.comtoolfx.tools.TimeUtils;
import com.y.comtoolfx.tools.fxtools.FX;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleLongProperty;

/**
 * @author Y
 * @version 1.0
 * @date 2024/3/14 15:18
 */

public class SerialComm implements AutoCloseable {
    /**
     * 发送保存
     */
    protected volatile boolean sendSave;
    //发送的数据量
    protected final SimpleLongProperty SEND_LONG_PROPERTY = new SimpleLongProperty(0);
    //接收的数据量
    protected final SimpleLongProperty RECEIVE_LONG_PROPERTY = new SimpleLongProperty(0);
    //串口打开关闭
    protected final SimpleBooleanProperty openSerial = new SimpleBooleanProperty(false);
    /**
     * 接收保存
     */
    protected volatile boolean receiveSave;
    private DataWriteFile dataWriteFile;
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
    protected final void updateListener(byte[] bytes) {
        if (bytes != null) {
            messageDelimiter = bytes;
            messageListener = new MessageListener(this);
            serialPort.removeDataListener();
            serialPort.addDataListener(messageListener);
        }
    }

    protected final String text(byte[] bytes) {
        return new String(bytes, 0, bytes.length - (messageDelimiter == null ? 0 : messageDelimiter.length));
    }

    protected final String textAndTime(byte[] bytes) {
        return "[" + TimeUtils.getNow() + "]" + new String(bytes, 0, bytes.length - (messageDelimiter == null ? 0 : messageDelimiter.length));
    }

    protected final void getSerial() {
        close();
        for (SerialPort serial : getSerialPorts()) {
            if (serial.getSystemPortName().equals(serialPortName)) {
                serialPort = serial;
                return;
            }
        }
        serialPort = null;
    }

    /**
     * 打开串行端口
     */
    protected final void openSerialPort() {
        getSerial();
        if (serialPort != null) {
            serialPort.setComPortParameters(baudRate, dataBits, stopBits, parity);
            serialPort.setFlowControl(flowControl);
            serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING | SerialPort.TIMEOUT_WRITE_BLOCKING, 100, 100);
            serialPort.addDataListener(messageListener);
            boolean b = serialPort.openPort();
            dataWriteFile = b ? new DataWriteFile(serialPortName) : null;
            open = b;
            FX.run(() -> openSerial.set(open));
        }
    }

    protected final void write(byte[] bytes) {
        if (open && bytes.length > 0) {
            int i = serialPort.writeBytes(bytes, bytes.length);
            if (i > 1) {
                FX.run(() -> SEND_LONG_PROPERTY.set(SEND_LONG_PROPERTY.get() + i));
                if (sendSave)
                    Thread.startVirtualThread(() -> dataWriteFile.write(bytes));
            }
        }
    }

    protected void listen(byte[] bytes) {
        FX.run(() -> RECEIVE_LONG_PROPERTY.set(RECEIVE_LONG_PROPERTY.get() + bytes.length));
        if (receiveSave)
            Thread.startVirtualThread(() -> dataWriteFile.read(bytes));
    }

    @Override
    public void close() {
        if (serialPort != null) {
            serialPort.removeDataListener();
            serialPort.closePort();
        }
        open = false;
        FX.run(() -> openSerial.set(open));
    }
}
