package GUI;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class CiaApp extends Application
{

    private static Stage stage;

    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException
    {
        Parent root = FXMLLoader.load(getClass().getResource("CIA.fxml"));

        //((HybridTestGen)root).setParentStage(primaryStage);

        setStage(primaryStage);

        primaryStage.setTitle("CIA tool for C/C++");

        primaryStage.setScene(new Scene(root));

        primaryStage.show();

        primaryStage.setOnCloseRequest(t ->
        {
            Platform.exit();
            System.exit(0);
        });

    }


    public static Stage getStage()
    {
        return stage;
    }

    public static void setStage(Stage _stage)
    {
        stage = _stage;
    }
}
