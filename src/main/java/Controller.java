import com.dd.plist.NSArray;
import com.dd.plist.NSDictionary;
import com.dd.plist.NSObject;
import com.dd.plist.PropertyListParser;
import com.google.common.base.CharMatcher;
import com.google.common.io.Files;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.function.Predicate;
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
    @FXML
    public Button resetButton;

    private Stage primaryStage;
    private ObservableList<App> apps;
    private ObservableList<String> locales;
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
        locales = FXCollections.observableArrayList();
        initializeListView();
        initializeChoiceBox();
        initializeTextField();
    }

    private void initializeTextField() {
        localeTextField.textProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue.length() > 0) localeChoice.getSelectionModel().clearSelection();
        }));
    }

    private void initializeChoiceBox() {
        localeChoice.setItems(locales);
        new Thread(() -> {
            try {
                locales.addAll(loadLocales());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void initializeListView() {
        appListView.setCellFactory(listView -> new App.AppListCell());
        // this is the default but it's good to be explicit
        appListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
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

    private String[] loadLocales() throws Exception {
        File plist = new File(System.getProperty("user.home") + "/Library/Preferences/.GlobalPreferences.plist");
        NSDictionary rootDict = (NSDictionary) PropertyListParser.parse(plist);
        NSObject[] langs = ((NSArray) rootDict.objectForKey("AppleLanguages")).getArray();
        return Stream.of(langs).map(NSObject::toString).toArray(String[]::new);
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

    @FXML
    public void resetButtonClicked(ActionEvent event) {
        if (validateAppSelected()) {
            App selectedApp = appListView.getSelectionModel().getSelectedItem();
            boolean confirm = AlertBox.displayYesNo("Warning",
                    "Are you sure you want to remove language setting for "
                            + selectedApp.name + "?");
            if (confirm) {
                Shell.execAndWait(Shell.defaultsDeleteCommandBuilder(selectedApp.id));
                AlertBox.display("Success", "Successfully removed language preference for "
                            + selectedApp.name);
            }
        }
    }

    @FXML
    public void changeButtonClicked(ActionEvent event) {
        if (validateAppSelected() && validateLocaleSelected()) {
            App selectedApp = appListView.getSelectionModel().getSelectedItem();
            String selectedLocale = getSelectedLocale();
            Shell.execAndWait(Shell.defaultsWriteCommandBuilder(selectedApp.id, selectedLocale));
            AlertBox.display("Success", "Successfully change language of app "
                    + selectedApp.name + " to " + selectedLocale
                    + "\nYou might need to restart the app for the change to take effect.");
        }
    }

    private boolean validateAppSelected() {
        if (appListView.getSelectionModel().getSelectedIndex() == -1) {
            AlertBox.display("Error", "You must select an app from the list.");
            return false;
        }
        return true;
    }

    private boolean validateLocaleSelected() {
        if (localeChoice.getSelectionModel().getSelectedIndex() == -1 && localeTextField.getText().length() == 0) {
            AlertBox.display("Error", "You must select a locale from the dropdown or manually enter a locale.");
            return false;
        }
        return true;
    }

    public void onListViewSelectionChanged(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        statusLabel.setText("Selected: " + apps.get(newValue.intValue()).name);
    }

    public String getSelectedLocale() {
        if (localeChoice.getSelectionModel().getSelectedIndex() != -1) {
            return localeChoice.getValue();
        }
        if (localeTextField.getText().length() > 0) {
            return localeTextField.getText();
        }
        return null;
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
        String id;  // identifier used by Mac: looks like com.apple.<app-name>
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
            new Thread(() -> {
                Shell.execForResult(Shell.appIdCommandBuilder(name), this::setId);
            }).start();
        }

        public void setId(String id) {
            this.id = CharMatcher.invisible().trimFrom(id);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof App)) return false;
            App other = (App) o;
            return other.name.equals(this.name);
        }

        /**
         * Loads the icon of the app from its {@code Contents/Resources/} directory. If the icon is in {@code icns}
         * format, then execute the shell command<br>
         *     {@code sips -s format png <IN-FILE> --out <OUT-FILE>}<br>
         * to convert it to PNG first.
         * @param app a {@code File} object pointing to the app
         * @return a {@code File} object pointing to the resulting PNG file
         */
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
