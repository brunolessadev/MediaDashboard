
package com.war.episode;

import com.war.MediaConfig;
import com.war.MediaScanner;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

public class EpisodeList {

    private final VBox view = new VBox(8);

    public EpisodeList(
            Path directory,
            Runnable onChanged,
            Consumer<Path> onPlay
    ) {
        this(directory, onChanged, onPlay, false);
    }

    public EpisodeList(
            Path directory,
            Runnable onChanged,
            Consumer<Path> onPlay,
            boolean openWithWindows
    ) {
        view.setPadding(new Insets(8, 0, 20, 0));

        Label heading = new Label("Episódios");
        heading.getStyleClass().add("header-title");
        view.getChildren().add(heading);

        List<MediaScanner.MediaItem> items =
                MediaScanner.scanDirectory(directory.toString());

        int episodeCount = 0;

        for (MediaScanner.MediaItem item : items) {
            if (item.isDirectory()) {
                continue;
            }

            Path videoPath = Path.of(item.getFolderPath());

            if (!MediaScanner.isVideoFile(videoPath)) {
                continue;
            }

            String mediaId = MediaScanner.getMediaId(videoPath);
            boolean watched = MediaConfig.isWatched(mediaId);

            view.getChildren().add(
                    EpisodeRow.create(
                            item.getTitle(),
                            watched,
                            () -> {
                                boolean newStatus =
                                        !MediaConfig.isWatched(mediaId);

                                MediaConfig.setWatched(mediaId, newStatus);
                                onChanged.run();
                            },
                            () -> {
                                onPlay.accept(videoPath);

                                if (openWithWindows) {
                                    MediaConfig.setWatched(mediaId, true);
                                    onChanged.run();
                                }
                            }
                    )
            );

            episodeCount++;
        }

        heading.setText("Episódios (" + episodeCount + ")");
        view.setVisible(episodeCount > 0);
        view.setManaged(episodeCount > 0);
    }

    public VBox getView() {
        return view;
    }
}
