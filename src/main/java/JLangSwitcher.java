/**
 * Created by Morton on 3/8/17.
 */

import com.aquafx_project.AquaFx;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class JLangSwitcher extends Application {

    public static final String APP_NAME = "JLangSwitcher";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        String os = System.getProperty("os.name");
        if (!os.toLowerCase().equals("mac os x")) {
            AlertBox.display("Error", "Sorry. JLangSwitcher only works on macOS.");
            Platform.exit();
        }
        FXMLLoader loader = new FXMLLoader(getClass().getResource("main.fxml"));
        Parent root = null;
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Controller controller = loader.getController();
        controller.setStage(primaryStage);

        Scene scene = new Scene(root);
        primaryStage.setTitle(APP_NAME);
        primaryStage.setScene(scene);

        primaryStage.show();
    }
}
