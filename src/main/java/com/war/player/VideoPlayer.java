
package com.war.player;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

import java.nio.file.Path;
import java.util.function.Consumer;

public class VideoPlayer {

    private MediaPlayer mediaPlayer;
    private MediaView mediaView;
    private VBox playerView;

    public VBox play(
            Path videoPath,
            Runnable onBack,
            Consumer<Path> onFinished
    ) {
        stop();

        Media media = new Media(videoPath.toUri().toString());
        MediaPlayer player = new MediaPlayer(media);
        mediaPlayer = player;

        mediaView = new MediaView(player);
        mediaView.setPreserveRatio(true);
        mediaView.setFitWidth(1100);
        mediaView.setFitHeight(620);

        Label title = new Label(videoPath.getFileName().toString());
        title.getStyleClass().add("header-title");
        title.setWrapText(true);

        Button backButton = new Button("← Voltar aos episódios");
        backButton.setOnAction(event -> {
            if (onBack != null) {
                onBack.run();
            }
        });

        Button playPauseButton = new Button("⏸ Pausar");
        Button stopButton = new Button("⏹ Parar");

        Slider timeline = new Slider(0, 1, 0);
        timeline.setMaxWidth(Double.MAX_VALUE);

        Label timeLabel = new Label("00:00 / 00:00");

        HBox controls = new HBox(
                10,
                playPauseButton,
                stopButton,
                timeline,
                timeLabel
        );
        controls.setAlignment(Pos.CENTER);
        HBox.setHgrow(timeline, Priority.ALWAYS);

        VBox controlsBox = new VBox(10, title, controls);
        controlsBox.setPadding(new Insets(12));

        playerView = new VBox(10, backButton, mediaView, controlsBox);
        playerView.setPadding(new Insets(16));
        playerView.setAlignment(Pos.CENTER);
        VBox.setVgrow(mediaView, Priority.ALWAYS);

        playPauseButton.setOnAction(event -> {
            if (mediaPlayer != player) {
                return;
            }

            if (player.getStatus() == MediaPlayer.Status.PLAYING) {
                player.pause();
                playPauseButton.setText("▶ Continuar");
            } else {
                player.play();
                playPauseButton.setText("⏸ Pausar");
            }
        });

        stopButton.setOnAction(event -> {
            if (mediaPlayer == player) {
                player.stop();
                playPauseButton.setText("▶ Continuar");
            }
        });

        timeline.setOnMouseReleased(event -> {
            if (mediaPlayer != player) {
                return;
            }

            Duration duration = player.getTotalDuration();

            if (duration != null
                    && !duration.isUnknown()
                    && !duration.isIndefinite()) {
                player.seek(Duration.seconds(timeline.getValue()));
            }
        });

        player.setOnReady(() -> {
            if (mediaPlayer != player) {
                return;
            }

            Duration duration = player.getTotalDuration();

            if (duration != null
                    && !duration.isUnknown()
                    && !duration.isIndefinite()) {
                timeline.setMax(Math.max(1, duration.toSeconds()));
                timeLabel.setText(
                        formatTime(Duration.ZERO)
                                + " / "
                                + formatTime(duration)
                );
            }

            player.play();
        });

        player.currentTimeProperty().addListener(
                (observable, oldTime, newTime) -> {
                    if (mediaPlayer != player) {
                        return;
                    }

                    if (!timeline.isValueChanging()
                            && newTime != null
                            && !newTime.isUnknown()
                            && !newTime.isIndefinite()) {
                        timeline.setValue(newTime.toSeconds());
                    }

                    Duration total = player.getTotalDuration();

                    if (newTime != null
                            && !newTime.isUnknown()
                            && total != null
                            && !total.isUnknown()
                            && !total.isIndefinite()) {
                        timeLabel.setText(
                                formatTime(newTime)
                                        + " / "
                                        + formatTime(total)
                        );
                    }
                }
        );

        player.setOnEndOfMedia(() -> {
            if (mediaPlayer != player) {
                return;
            }

            player.stop();

            if (onFinished != null) {
                onFinished.accept(videoPath);
            }
        });

        player.setOnError(() -> {
            if (mediaPlayer != player) {
                return;
            }

            Throwable error = player.getError();

            System.err.println(
                    "Erro na reprodução do vídeo "
                            + videoPath
                            + ": "
                            + (error == null
                            ? "erro desconhecido"
                            : error.getMessage())
            );
        });

        return playerView;
    }

    public VBox getView() {
        return playerView;
    }

    public boolean hasPlayer() {
        return mediaPlayer != null;
    }

    public void stop() {
        MediaPlayer player = mediaPlayer;
        mediaPlayer = null;

        if (player != null) {
            player.stop();
            player.dispose();
        }

        mediaView = null;
        playerView = null;
    }

    private String formatTime(Duration duration) {
        if (duration == null
                || duration.isUnknown()
                || duration.isIndefinite()) {
            return "00:00";
        }

        int totalSeconds = Math.max(
                0,
                (int) duration.toSeconds()
        );

        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        if (hours > 0) {
            return String.format(
                    "%02d:%02d:%02d",
                    hours,
                    minutes,
                    seconds
            );
        }

        return String.format(
                "%02d:%02d",
                minutes,
                seconds
        );
    }
}
