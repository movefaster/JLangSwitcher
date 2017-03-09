import com.google.common.io.Files;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import jdk.nashorn.internal.runtime.options.Option;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Created by Morton on 3/8/17.
 */
public class Controller implements Initializable {

    @FXML
    public Button changeButton;
    @FXML
    public Button addAppButton;
    @FXML
    public ListView<App> appListView;
    @FXML
    public TextField localeTextField;
    @FXML
    public ChoiceBox<String> localeChoice;
    @FXML
    public Label statusLabel;

    private Stage primaryStage;
    private ObservableList<App> apps;
//    private File convertedIconDir;
    private static File convertedIconDir = new File("/var/tmp/" + JLangSwitcher.APP_NAME);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (!convertedIconDir.exists()) {
            boolean dirCreated = convertedIconDir.mkdir();
            if (!dirCreated) {
                convertedIconDir = Files.createTempDir();
            }
        }
        apps = FXCollections.observableArrayList();
        appListView.setCellFactory(listView -> new App.AppListCell());
//        appListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        System.out.println(appListView.isDisabled());
        appListView.setItems(apps);
        appListView.getSelectionModel().selectedIndexProperty().addListener(this::onListViewSelectionChanged);
        new Thread(() -> {
            try {
                loadApps();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void loadApps() throws IOException {
        File appsDir = new File("/Applications");
        for (File f : appsDir.listFiles(f -> new ExtensionPredicate("app").test(f))) {
            App app = new App(f);
            if (!apps.contains(app)) apps.add(app);
        }
    }

    public void setStage(Stage stage) {
        primaryStage = stage;
    }

    @FXML
    public void addButtonClicked(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setInitialDirectory(new File("/Applications"));
        chooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Applications", "app"));
        File result = chooser.showOpenDialog(primaryStage);
        if (result != null) {
            App app = new App(result);
            // check if app is present in list: if not add to list and update its index
            int indexInList = apps.indexOf(app);
            if (indexInList < 0) {
                apps.add(app);
                indexInList = apps.size() - 1;
            }
            appListView.scrollTo(indexInList);
            appListView.getFocusModel().focus(indexInList);
            appListView.getSelectionModel().select(indexInList);
        }
    }

    public void onListViewSelectionChanged(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        statusLabel.setText("Selected: " + apps.get(newValue.intValue()).name);
    }

    private static class App {
        static class AppListCell extends ListCell<App> {
            @Override
            public void updateItem(App app, boolean empty) {
                super.updateItem(app, empty);
                if (empty || app == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(app.name);
                    ImageView view = new ImageView(app.icon);
                    view.setFitHeight(20);
                    view.setFitWidth(20);
                    setGraphic(view);
                }
            }
        }
        Image icon;
        String name;
        public App(Image icon, String name) {
            this.icon = icon;
            this.name = name;
        }
        public App(File pathToApp) {
            File pngFile = loadIconFile(pathToApp);
            if (pngFile != null) {
                try {
                    icon = new Image(new FileInputStream(pngFile));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            name = pathToApp.getName();
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof App)) return false;
            App other = (App) o;
            return other.name.equals(this.name);
        }

        private File loadIconFile(File app) {
            File resources = new File(app, "Contents/Resources/");
            Optional<File> fileOptional =  Stream.of(resources.listFiles())
                    .filter(new ExtensionPredicate("icns")).findFirst();
            if (fileOptional.isPresent()) {
                File iconFile = fileOptional.get();
                File pngFile = new File(convertedIconDir,
                        FilenameUtils.getBaseName(app.getName()) + ".png");
                if (!pngFile.exists()) {
                    Shell.execAndWait(Shell.sipsCommandBuilder(iconFile, pngFile));
                }
                return pngFile;
            }
            return null;
        }
    }

    private static class ExtensionPredicate implements Predicate<File> {
        private String extension;
        ExtensionPredicate(String extension) {
            this.extension = extension;
        }
        @Override
        public boolean test(File file) {
            return FilenameUtils.getExtension(file.getName()).endsWith(extension);
        }
    }
}
