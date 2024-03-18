package com.y.comtoolfx.controller;

import com.fazecast.jSerialComm.SerialPort;
import com.y.comtoolfx.AppLauncher;
import com.y.comtoolfx.serialComm.AnalogDevice;
import com.y.comtoolfx.serialComm.SerialComm;
import com.y.comtoolfx.tools.fxtools.FX;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import org.apache.commons.text.StringEscapeUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 家
 *
 * @author Y
 * @version 1.0
 * @date 2024/3/14 15:43
 */

public class Home extends SerialComm {
    //文件选择器
    public static final FileChooser FILE_CHOOSER = new FileChooser();
    public static final File startFile;

    static {
        String runtime = System.getProperty("java.home");
        startFile = new File(runtime).getParentFile();
    }

    /**
     * 周期性发送字节时间线
     */
    private final Timeline cyclicalitySendBytesTimeLine = new Timeline();
    /**
     * 循环性字节
     */
    private volatile byte[] cyclicalBytes;
    /**
     * 回复
     */
    private Map<String, byte[]> replays = new HashMap<>();

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
     * 串口打开标识
     */
    @FXML
    private Circle comLight;
    /**
     * 发送周期
     */
    @FXML
    private TextField cyclical;
    /**
     * 打开串行端口
     */
    @FXML
    private Button openSerialPort;
    /**
     * 发送
     */
    @FXML
    private Label send;
    /**
     * 接收
     */
    @FXML
    private Label receive;
    @FXML
    private TextArea receiveMessage;
    @FXML
    private CheckBox receiveShow;
    @FXML
    private TextArea sendMessage;
    @FXML
    private CheckBox showTime;
    /**
     * 修改延时
     */
    @FXML
    private CheckBox timedDispatch;
    //时间
    private volatile long waitTime = 1000;

    /**
     * 获得回复
     *
     * @param bytes 字节
     * @return {@code byte[]}
     */
    private byte[] getReply(byte[] bytes) {
        String key = new String(bytes);
        return (replays == null) ? null : replays.get(key);
    }

    /**
     * 模拟应答(选择json文件)
     *
     * @param event 事件
     */
    @FXML
    void analogReply(ActionEvent event) {
        File file = FILE_CHOOSER.showOpenDialog(sendMessage.getScene().getWindow());
        if (file != null) {
            replays = AnalogDevice.config(file);
            if (replays != null) {
                updateListener(replays.get("消息分隔符"));
            }
        }
    }

    /**
     * 添加窗口
     *
     * @param event 事件
     */
    @FXML
    void addStage(ActionEvent event) {
        AppLauncher.getStage().show();
    }

    /**
     * 清理接收的数据
     *
     * @param event 事件
     */
    @FXML
    void cleanReceive(ActionEvent event) {
        receiveMessage.clear();
    }

    /**
     * 清理接收的计数
     *
     * @param event 事件
     */
    @FXML
    void cleanReceiveNumber(MouseEvent event) {
        RECEIVE_LONG_PROPERTY.set(0);
    }

    /**
     * 清除发送窗口输入的数据
     *
     * @param event 事件
     */
    @FXML
    void cleanSend(ActionEvent event) {
        sendMessage.clear();
    }

    /**
     * 清理发送计数
     *
     * @param event 事件
     */
    @FXML
    void cleanSendNumber(MouseEvent event) {
        SEND_LONG_PROPERTY.set(0);
    }

    /**
     * 发送数据(单次)
     *
     * @param event 事件
     */
    @FXML
    void sendData(ActionEvent event) {
        write();
    }

    @Override
    protected void listen(byte[] bytes) {
        super.listen(bytes);
        if (receiveShow.isSelected()) {
            FX.run(() -> receiveMessage.appendText(showTime.isSelected() ? textAndTime(bytes) + "\n" : text(bytes) + "\n"));
        }
        //返回数据
        byte[] reply = getReply(bytes);
        if (reply != null) {
            new Thread(() -> {
                try {
                    Thread.sleep(this.waitTime);
                } catch (InterruptedException ignored) {
                }
                write(reply);
            }).start();
        }
    }

    @Override
    public void close() {
        super.close();
        //停止周期性任务
        cyclicalitySendBytesTimeLine.stop();
    }

    private void write() {
        write(StringEscapeUtils.unescapeJava(sendMessage.getText()).getBytes(StandardCharsets.UTF_8));
    }

    private void onActionOpenSerialPort() {
        if (openSerial.get()) {
            close();
        } else {
            openSerialPort();
        }
    }

    @FXML
    void initialize() {
        cyclicalitySendBytesTimeLine.setCycleCount(Timeline.INDEFINITE);

        //串口按钮绑定
        openSerialPort.textProperty().bind(openSerial.map(b -> b ? "关闭串口" : "打开串口"));
        //串口点击事件
        openSerialPort.setOnAction(e -> onActionOpenSerialPort());
        //串口指示灯
        comLight.fillProperty().bind(openSerial.map(b -> b ? Color.LIME : Color.RED));

        //发送计数绑定
        send.textProperty().bind(SEND_LONG_PROPERTY.asString());
        //接收计数绑定
        receive.textProperty().bind(RECEIVE_LONG_PROPERTY.asString());
        addListener();
        initParameters();
        openSerialPort();
        FILE_CHOOSER.setTitle("选择json文件");
        FILE_CHOOSER.setInitialDirectory(startFile);
        FILE_CHOOSER.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("json文件 (*.json)", "*.json")
        );
    }

    private void addListener() {
        //更新列表
        serialPortNamePicker.setOnMouseClicked(event -> updateSerialNumberList());
        //串口号
        serialPortNamePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            serialPortName = newValue;
            openSerialPort();
        });
        //波特率
        baudRatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            baudRate = newValue;
            openSerialPort();
        });
        //数据位
        dataBitsPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            dataBits = newValue;
            openSerialPort();
        });
        //停止位
        stopBitsPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            stopBits = switch (newValue) {
                case "1.5" -> SerialPort.ONE_POINT_FIVE_STOP_BITS;
                case "2" -> SerialPort.TWO_STOP_BITS;
                default -> SerialPort.ONE_STOP_BIT;
            };
            openSerialPort();
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
            openSerialPort();
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
            openSerialPort();
        });

        //实时更新延迟
        cyclical.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                waitTime = Integer.parseInt(newValue);
                if (waitTime < 1) {
                    waitTime = 1;
                }
            } catch (NumberFormatException e) {
                cyclical.setText(oldValue);
            }
            System.out.println("waitTime: = " + waitTime);
        });

        timedDispatch.selectedProperty().addListener((observable, oldValue, newValue) -> {
            //开始
            if (newValue) {
                //获取发送的数组
                cyclicalBytes = StringEscapeUtils.unescapeJava(sendMessage.getText()).getBytes(StandardCharsets.UTF_8);
                //清理帧
                cyclicalitySendBytesTimeLine.getKeyFrames().clear();
                //添加帧
                cyclicalitySendBytesTimeLine.getKeyFrames().add(new KeyFrame(Duration.millis(waitTime), s -> write(cyclicalBytes)));
                //开始发送
                cyclicalitySendBytesTimeLine.play();
            } else {
                //关闭
                cyclicalitySendBytesTimeLine.stop();
            }
        });
        receiveMessage.textProperty().addListener((observable, oldValue, newValue) -> receiveMessage.setScrollTop(Double.MAX_VALUE));

        //更新数据
        sendMessage.textProperty().addListener((observable, oldValue, newValue) -> cyclicalBytes = StringEscapeUtils.unescapeJava(newValue).getBytes(StandardCharsets.UTF_8));
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
