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
import javafx.scene.media.MediaException;
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
        mediaPlayer = new MediaPlayer(media);
        mediaView = new MediaView(mediaPlayer);

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

        playerView = new VBox(
                10,
                backButton,
                mediaView,
                controlsBox
        );

        playerView.setPadding(new Insets(16));
        playerView.setAlignment(Pos.CENTER);

        VBox.setVgrow(mediaView, Priority.ALWAYS);

        playPauseButton.setOnAction(event -> {
            if (mediaPlayer == null) {
                return;
            }

            if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                mediaPlayer.pause();
                playPauseButton.setText("▶ Continuar");
            } else {
                mediaPlayer.play();
                playPauseButton.setText("⏸ Pausar");
            }
        });

        stopButton.setOnAction(event -> {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
            }
        });

        timeline.setOnMouseReleased(event -> {
            if (mediaPlayer != null
                    && mediaPlayer.getTotalDuration() != null
                    && !mediaPlayer.getTotalDuration().isUnknown()) {
                mediaPlayer.seek(Duration.seconds(timeline.getValue()));
            }
        });

        mediaPlayer.setOnReady(() -> {
            Duration duration = mediaPlayer.getTotalDuration();

            if (duration != null && !duration.isUnknown()) {
                timeline.setMax(Math.max(1, duration.toSeconds()));
                timeLabel.setText(
                        formatTime(Duration.ZERO)
                                + " / "
                                + formatTime(duration)
                );
            }

            mediaPlayer.play();
        });

        mediaPlayer.currentTimeProperty().addListener(
                (observable, oldTime, newTime) -> {
                    if (!timeline.isValueChanging()
                            && newTime != null
                            && !newTime.isUnknown()) {
                        timeline.setValue(newTime.toSeconds());
                    }

                    Duration total = mediaPlayer.getTotalDuration();

                    if (newTime != null
                            && total != null
                            && !total.isUnknown()) {
                        timeLabel.setText(
                                formatTime(newTime)
                                        + " / "
                                        + formatTime(total)
                        );
                    }
                }
        );

        mediaPlayer.setOnEndOfMedia(() -> {
            mediaPlayer.stop();

            if (onFinished != null) {
                onFinished.accept(videoPath);
            }
        });

        mediaPlayer.setOnError(() -> {
            Throwable error = mediaPlayer.getError();

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
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
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