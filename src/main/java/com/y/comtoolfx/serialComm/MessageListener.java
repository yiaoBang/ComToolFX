package com.y.comtoolfx.serialComm;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortMessageListenerWithExceptions;

/**
 * @author Y
 * @version 1.0
 * @date 2024/3/14 15:58
 */

public class MessageListener implements SerialPortMessageListenerWithExceptions {
    private final SerialComm serialComm;

    public MessageListener(SerialComm serialComm) {
        this.serialComm = serialComm;
    }

    @Override
    public void catchException(Exception e) {
        System.out.println(e.getMessage());
    }

    @Override
    public byte[] getMessageDelimiter() {
        return serialComm.messageDelimiter;
    }

    @Override
    public boolean delimiterIndicatesEndOfMessage() {
        //true表示结束符,false表示开始符号
        return true;
    }

    @Override
    public int getListeningEvents() {
        return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
        serialComm.listen(event.getReceivedData());
    }
}
