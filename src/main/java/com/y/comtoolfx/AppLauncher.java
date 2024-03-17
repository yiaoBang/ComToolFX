package com.y.comtoolfx;

import atlantafx.base.theme.PrimerLight;
import com.y.comtoolfx.controller.Home;
import com.y.comtoolfx.tools.DefaultExceptionHandler;
import com.y.comtoolfx.tools.fxtools.FX;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.stage.Stage;

/**
 * @author Y
 * @version 1.0
 * @date 2024/3/14 15:27
 */
public class AppLauncher extends Application {
    private static JavaFXBuilderFactory javaFXBuilderFactory;
    public static void main(String[] args) {
        System.setProperty("prism.lcdtext", "false");
        launch(args);
    }

    public static Stage getStage() {
        Stage stage = new Stage();
        Thread.currentThread().setUncaughtExceptionHandler(new DefaultExceptionHandler(stage));
        FXMLLoader loader = FX.FXMLLoader("Home");
        loader.setBuilderFactory(javaFXBuilderFactory);
        stage.setScene(FX.scene(loader));
        stage.setTitle("ComToolFX");
        stage.getIcons().add(FX.image("application.png"));
        stage.setResizable(false);
        stage.show();
        Home home = loader.getController();
        //窗口关闭清理串口
        stage.setOnCloseRequest(_ -> home.close());
        return stage;
    }

    @Override
    public void start(Stage stage) {
        javaFXBuilderFactory = new JavaFXBuilderFactory();
        //亮色主题
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
        getStage().show();
    }
}
