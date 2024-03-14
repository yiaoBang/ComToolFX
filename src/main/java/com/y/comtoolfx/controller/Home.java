package com.y.comtoolfx.controller;

import com.fazecast.jSerialComm.SerialPort;
import com.y.comtoolfx.AppLauncher;
import com.y.comtoolfx.serialComm.SerialComm;
import com.y.comtoolfx.tools.DataHandler;
import com.y.comtoolfx.tools.fxtools.FX;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

/**
 * 家
 *
 * @author Y
 * @version 1.0
 * @date 2024/3/14 15:43
 */
public class Home extends SerialComm {
    private final SimpleLongProperty SEND_LONG_PROPERTY = new SimpleLongProperty(0);
    private final SimpleLongProperty RECEIVE_LONG_PROPERTY = new SimpleLongProperty(0);
    private final SimpleBooleanProperty openSerial = new SimpleBooleanProperty(false);
    private final Timeline cyclicalitySendBytes = new Timeline();
    private volatile byte[] cyclicalityBytes;
    /**
     * 串口名称选取器
     */
    @FXML
    private ComboBox<String> serialPortNamePicker;
    /**
     * 波特率选择器
     */
    @FXML
    private ComboBox<Integer> baudRatePicker;
    /**
     * 数据位选取器
     */
    @FXML
    private ComboBox<Integer> dataBitsPicker;
    /**
     * 停止位拾取器
     */
    @FXML
    private ComboBox<String> stopBitsPicker;
    /**
     * 奇偶校验选择器
     */
    @FXML
    private ComboBox<String> parityPicker;
    /**
     * 流量控制拾取器
     */
    @FXML
    private ComboBox<String> flowControlPicker;


    /**
     * 分隔符
     */
    @FXML
    private TextField delimiter;
    /**
     * 串口打开标识
     */
    @FXML
    private Circle comLight;
    /**
     * 发送周期
     */
    @FXML
    private TextField cyclicality;
    /**
     * 打开串行端口
     */
    @FXML
    private Button openSerialPort;
    @FXML
    private Label receive;
    @FXML
    private TextArea receiveMessage;
    @FXML
    private CheckBox receiveShow;
    @FXML
    private Label send;
    @FXML
    private TextArea sendMessage;
    @FXML
    private CheckBox showTime;
    @FXML
    private CheckBox timedDispatch;

    @FXML
    void addStage(ActionEvent event) {
        AppLauncher.getStage().show();
    }

    @FXML
    void cleanReceive(ActionEvent event) {
        receiveMessage.clear();
    }

    @FXML
    void cleanReceiveNumber(MouseEvent event) {
        RECEIVE_LONG_PROPERTY.set(0);
    }

    @FXML
    void cleanSend(ActionEvent event) {
        sendMessage.clear();
    }

    @FXML
    void cleanSendNumber(MouseEvent event) {
        SEND_LONG_PROPERTY.set(0);
    }

    @FXML
    void sendData(ActionEvent event) {
        write();
    }

    @Override
    protected void listen(byte[] bytes) {
        if (bytes.length > 0) {
            FX.run(() -> RECEIVE_LONG_PROPERTY.set(RECEIVE_LONG_PROPERTY.getValue() + bytes.length));
        }
        if (receiveShow.isSelected()) {
            if (showTime.isSelected()) {
                FX.run(() -> receiveMessage.appendText(textAndTime(bytes) + "\n"));
            } else {
                FX.run(() -> receiveMessage.appendText(text(bytes) + "\n"));
            }
        }
    }

    @Override
    public void close() {
        super.close();
        cyclicalitySendBytes.stop();
    }

    private void addSendNumber(int number) {
        if (number > 0) {
            SEND_LONG_PROPERTY.set(SEND_LONG_PROPERTY.getValue() + number);
        }
    }

    private void write() {
        String messageText = sendMessage.getText();
        String delimiterText = delimiter.getText();
        addSendNumber(write(DataHandler.getByte(messageText, delimiterText)));
    }

    private void onActionOpenSerialPort() {
        if (openSerial.get()) {
            close();
            openSerial.set(false);
        } else {
            openSerial.set(openPort());
        }
    }

    private void open() {
        openSerial.set(openPort());
    }

    @FXML
    void initialize() {
        cyclicalitySendBytes.setCycleCount(Timeline.INDEFINITE);

        //串口按钮绑定
        openSerialPort.textProperty().bind(openSerial.map(b -> b ? "关闭串口" : "打开串口"));
        //串口点击事件
        openSerialPort.setOnAction(e -> onActionOpenSerialPort());
        //串口只是灯
        comLight.fillProperty().bind(openSerial.map(b -> b ? Color.LIME : Color.RED));

        //发送计数绑定
        send.textProperty().bind(SEND_LONG_PROPERTY.asString());
        //接收计数绑定
        receive.textProperty().bind(RECEIVE_LONG_PROPERTY.asString());
        addListener();
        initParameters();
        open();
    }

    private void addListener() {
        //更新列表
        serialPortNamePicker.setOnMouseClicked(event -> updateSerialNumberList());
        //串口号
        serialPortNamePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            serialPortName = newValue;
            open();
        });
        //波特率
        baudRatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            baudRate = newValue;
            open();
        });
        //数据位
        dataBitsPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            dataBits = newValue;
            open();
        });
        //停止位
        stopBitsPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            stopBits = switch (newValue) {
                case "1.5" -> SerialPort.ONE_POINT_FIVE_STOP_BITS;
                case "2" -> SerialPort.TWO_STOP_BITS;
                default -> SerialPort.ONE_STOP_BIT;
            };
            open();
        });
        //校验
        parityPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            parity = switch (newValue) {
                case "奇校验" -> SerialPort.ODD_PARITY;
                case "偶校验" -> SerialPort.EVEN_PARITY;
                case "标记校验" -> SerialPort.MARK_PARITY;
                case "空格校验" -> SerialPort.SPACE_PARITY;
                default -> SerialPort.NO_PARITY;
            };
            open();
        });
        //流控
        flowControlPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            flowControl = switch (newValue) {
                case "RTS/CTS" -> SerialPort.FLOW_CONTROL_RTS_ENABLED | SerialPort.FLOW_CONTROL_CTS_ENABLED;
                case "DSR/DTR" -> SerialPort.FLOW_CONTROL_DSR_ENABLED | SerialPort.FLOW_CONTROL_DTR_ENABLED;
                case "ON/OFF" ->
                        SerialPort.FLOW_CONTROL_XONXOFF_IN_ENABLED | SerialPort.FLOW_CONTROL_XONXOFF_OUT_ENABLED;
                default -> SerialPort.FLOW_CONTROL_DISABLED;
            };
            open();
        });
        timedDispatch.selectedProperty().addListener((observable, oldValue, newValue) -> {
            //开始
            if (newValue) {
                int value = 1000;
                try {
                    value = Integer.parseInt(cyclicality.getText().trim());
                } catch (NumberFormatException e) {
                    //throw new RuntimeException(e);
                }
                cyclicality.setText(String.valueOf(value));
                //获取发送的数组
                cyclicalityBytes = DataHandler.getByte(sendMessage.getText(), delimiter.getText());
                //清理帧
                cyclicalitySendBytes.getKeyFrames().clear();
                //添加帧
                cyclicalitySendBytes.getKeyFrames().add(new KeyFrame(Duration.millis(value), s -> addSendNumber(write(cyclicalityBytes))));
                //开始发送
                cyclicalitySendBytes.play();
            } else {
                //关闭
                cyclicalitySendBytes.stop();
            }
        });
        receiveMessage.textProperty().addListener((observable, oldValue, newValue) -> receiveMessage.setScrollTop(Double.MAX_VALUE));


        //更新数据
        sendMessage.textProperty().addListener((observable, oldValue, newValue) -> cyclicalityBytes = DataHandler.getByte(newValue, delimiter.getText()));
        delimiter.textProperty().addListener((observable, oldValue, newValue) -> cyclicalityBytes = DataHandler.getByte(sendMessage.getText(), newValue));
    }

    public void updateSerialNumberList() {
        ObservableList<String> serialNumbers = serialPortNamePicker.getItems();
        serialNumbers.clear();
        boolean b = false;
        SerialPort[] commPorts = SerialPort.getCommPorts();
        for (SerialPort commPort : commPorts) {
            serialNumbers.add(commPort.getSystemPortName());
        }
        for (SerialPort commPort : commPorts) {
            if (commPort.getSystemPortName().equals(serialPortNamePicker.getValue())) {
                b = true;
            }
        }
        if (!b) {
            if (!serialNumbers.isEmpty()) serialPortNamePicker.setValue(serialNumbers.getFirst());
            else serialPortNamePicker.setValue("");
        }
    }

    private void initParameters() {
        //串口号
        updateSerialNumberList();
        //波特率
        baudRatePicker.getItems().addAll(9600, 19200, 38400, 115200, 128000, 230400, 256000, 460800, 921600, 1382400);
        //数据位
        dataBitsPicker.getItems().addAll(8, 7, 6, 5);
        //停止
        stopBitsPicker.getItems().addAll("1", "1.5", "2");
        //校验
        parityPicker.getItems().addAll("无", "奇校验", "偶校验", "标记校验", "空格校验");
        //流控
        flowControlPicker.getItems().addAll("无", "RTS/CTS", "DSR/DTR", "ON/OFF");
        if (!serialPortNamePicker.getItems().isEmpty()) {
            serialPortNamePicker.setValue(serialPortNamePicker.getItems().getFirst());
        }
        baudRatePicker.setValue(9600);
        dataBitsPicker.setValue(8);
        stopBitsPicker.setValue("1");
        parityPicker.setValue("无");
        flowControlPicker.setValue("无");
    }
}