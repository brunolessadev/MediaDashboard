package com.war;

import com.war.navigation.MediaBrowser;
import com.war.player.VideoPlayer;
import com.war.style.StyleA;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaException;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.nio.file.Path;

public class Main extends Application {

    private BorderPane root;
    private WindowBar windowBar;
    private MediaBrowser browser;
    private final VideoPlayer videoPlayer = new VideoPlayer();
    private VBox playerView;

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
        stopPlayer();

        try {
            playerView = videoPlayer.play(
                    videoPath,
                    () -> {
                        stopPlayer();
                        browser.refreshCurrentDirectory();
                    },
                    finishedPath -> {
                        MediaConfig.setWatched(
                                MediaScanner.getMediaId(finishedPath),
                                true
                        );

                        stopPlayer();
                        browser.refreshCurrentDirectory();
                    }
            );

            root.setCenter(playerView);
            StyleA.refresh(root);

        } catch (MediaException | IllegalArgumentException e) {
            System.err.println(
                    "Não foi possível reproduzir "
                            + videoPath
                            + ": "
                            + e.getMessage()
            );

            stopPlayer();

            browser.showMessage(
                    "Não foi possível reproduzir este vídeo. "
                            + "Verifique a compatibilidade do formato."
            );
        }
    }

    private void stopPlayer() {
        videoPlayer.stop();
        playerView = null;
    }

    @Override
    public void stop() {
        stopPlayer();
    }

    public static void main(String[] args) {
        launch(args);
    }
}