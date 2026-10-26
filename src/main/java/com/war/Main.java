
        package com.war;

import com.war.navigation.MediaBrowser;
import com.war.style.StyleA;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main extends Application {

    private BorderPane root;
    private WindowBar windowBar;
    private MediaBrowser browser;

    @Override
    public void start(Stage primaryStage) {
        MediaScanner.initializeCategories();

        primaryStage.initStyle(StageStyle.UNDECORATED);

        root = new BorderPane();

        windowBar = new WindowBar(
                primaryStage,
                root,
                this::reloadMedia
        );

        browser = new MediaBrowser(
                root,
                windowBar.getTabs(),
                this::playVideo
        );

        root.setTop(windowBar.getView());
        root.setCenter(browser.getView());

        Scene scene = new Scene(root, 1250, 800);

        StyleA.Theme theme;

        try {
            theme = StyleA.Theme.valueOf(MediaConfig.getTheme());
        } catch (IllegalArgumentException e) {
            theme = StyleA.Theme.CYBERPUNK;
        }

        StyleA.applyTheme(root, theme);

        primaryStage.setTitle("MediaDashboard - JavaFX");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();

        browser.reloadMedia();
    }

    private void reloadMedia() {
        if (browser != null) {
            browser.reloadMedia();
        }
    }

    private void playVideo(Path videoPath) {
        try {
            if (videoPath == null || !Files.isRegularFile(videoPath)) {
                browser.showMessage(
                        "O arquivo de vídeo não foi encontrado."
                );
                return;
            }

            if (!Desktop.isDesktopSupported()
                    || !Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                browser.showMessage(
                        "Não foi possível abrir o vídeo pelo Windows."
                );
                return;
            }

            Desktop.getDesktop().open(videoPath.toFile());

        } catch (IOException | UnsupportedOperationException
                 | SecurityException e) {
            System.err.println(
                    "Não foi possível abrir o vídeo "
                            + videoPath
                            + ": "
                            + e.getMessage()
            );

            browser.showMessage(
                    "Não foi possível abrir o vídeo no aplicativo padrão do Windows."
            );
        }
    }

    @Override
    public void stop() {
        // A reprodução é executada externamente; não há player interno para encerrar.
    }

    public static void main(String[] args) {
        launch(args);
    }
}