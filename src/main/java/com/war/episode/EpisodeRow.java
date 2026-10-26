
package com.war.episode;

import com.war.style.StyleA;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;

public class EpisodeRow {

    public static HBox create(
            String title,
            boolean watched,
            Runnable onToggleWatched,
            Runnable onPlay
    ) {
        Label titleLabel = new Label(limitTitle(title));
        titleLabel.getStyleClass().add("episode");
        titleLabel.setWrapText(true);
        titleLabel.setAlignment(Pos.CENTER_LEFT);
        titleLabel.setPrefWidth(420);
        titleLabel.setMinWidth(420);
        titleLabel.setMaxWidth(420);

        Label statusIcon = new Label();
        statusIcon.getStyleClass().add("episode-status-icon");
        statusIcon.setAlignment(Pos.CENTER);
        statusIcon.setMinWidth(22);
        statusIcon.setPrefWidth(22);
        statusIcon.setMaxWidth(22);
        statusIcon.setStyle("-fx-font-family: 'Segoe UI Symbol'; -fx-font-size: 15px;");

        Label statusText = new Label();
        statusText.getStyleClass().add("episode-status-text");
        statusText.setAlignment(Pos.CENTER_LEFT);
        statusText.setStyle("-fx-font-size: 13px;");

        HBox statusContainer = new HBox(5, statusIcon, statusText);
        statusContainer.setAlignment(Pos.CENTER);
        statusContainer.setMinWidth(125);
        statusContainer.setPrefWidth(125);
        statusContainer.setMaxWidth(125);

        Button watchedButton = new Button();
        watchedButton.setMinWidth(165);
        watchedButton.setPrefWidth(165);
        watchedButton.setMaxWidth(165);
        watchedButton.getStyleClass().add("episode-button");

        Button playButton = new Button("▶ Abrir");
        playButton.setMinWidth(100);
        playButton.setPrefWidth(100);
        playButton.setMaxWidth(100);
        playButton.getStyleClass().addAll(
                "episode-button",
                "episode-open"
        );

        updateStatus(watched, statusIcon, statusText, watchedButton);

        watchedButton.setOnAction(event -> onToggleWatched.run());
        playButton.setOnAction(event -> onPlay.run());

        HBox row = new HBox(
                12,
                titleLabel,
                statusContainer,
                watchedButton,
                playButton
        );

        row.getStyleClass().add("episode");
        row.setAlignment(Pos.CENTER);
        row.setPadding(new Insets(10, 16, 10, 16));
        row.setMaxWidth(Double.MAX_VALUE);

        HBox.setHgrow(titleLabel, Priority.NEVER);

        return row;
    }

    static String limitTitle(String title) {
        if (title == null) {
            return "";
        }

        if (title.length() <= 200) {
            return title;
        }

        return title.substring(0, 197) + "...";
    }

    static void updateStatus(
            boolean watched,
            Label statusIcon,
            Label statusText,
            Button watchedButton
    ) {
        Color statusColor = watched
                ? Color.LIMEGREEN
                : Color.RED;

        statusIcon.setText(watched ? "✓" : "●");
        statusIcon.setTextFill(statusColor);
        statusIcon.setStyle(
                "-fx-font-family: 'Segoe UI Symbol';"
                        + "-fx-font-size: 15px;"
                        + "-fx-text-fill: "
                        + (watched ? "#32CD32" : "#FF0000")
                        + ";"
        );

        statusText.setText(
                watched ? "Assistido" : "Não assistido"
        );
        statusText.setTextFill(statusColor);
        statusText.setStyle(
                "-fx-font-size: 13px;"
                        + "-fx-text-fill: "
                        + (watched ? "#32CD32" : "#FF0000")
                        + ";"
        );

        watchedButton.getStyleClass().removeAll(
                "episode-mark-watched",
                "episode-unmark-watched"
        );

        if (watched) {
            watchedButton.setText("Desmarcar assistido");
            watchedButton.getStyleClass().add(
                    "episode-unmark-watched"
            );
        } else {
            watchedButton.setText("Marcar como assistido");
            watchedButton.getStyleClass().add(
                    "episode-mark-watched"
            );
        }

        StyleA.refresh(watchedButton);
    }
}
