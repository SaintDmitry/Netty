package com.geekbrains.client;

import com.geekbrains.client.controllers.ClientController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Client extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/appScene.fxml"));
        Parent root = fxmlLoader.load();
        primaryStage.setTitle("Netty cloud");
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();

        ClientController controller = fxmlLoader.getController();
        controller.onStartApp();
        Network.start(controller);

        primaryStage.setOnCloseRequest(event -> Network.stop());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
