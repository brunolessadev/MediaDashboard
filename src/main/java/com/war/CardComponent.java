
        package com.war;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.function.Consumer;

public class CardComponent {

    private static final double CARD_WIDTH = 220;
    private static final double CARD_PADDING = 10;
    private static final double COVER_WIDTH =
            CARD_WIDTH - CARD_PADDING * 2;
    private static final double COVER_HEIGHT = 290;

    private CardComponent() {
    }

    public static VBox create(
            String title,
            String progressText,
            double progress,
            String coverPath,
            boolean favorite,
            Consumer<Boolean> onFavoriteChanged
    ) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setPrefWidth(CARD_WIDTH);
        card.setMinWidth(CARD_WIDTH);
        card.setMaxWidth(CARD_WIDTH);
        card.setPadding(new Insets(CARD_PADDING));

        StackPane coverContainer = new StackPane();
        coverContainer.getStyleClass().add("cover-container");
        coverContainer.setPrefSize(COVER_WIDTH, COVER_HEIGHT);
        coverContainer.setMinSize(COVER_WIDTH, COVER_HEIGHT);
        coverContainer.setMaxSize(COVER_WIDTH, COVER_HEIGHT);
        coverContainer.setAlignment(Pos.TOP_RIGHT);

        ImageView coverImageView = loadCoverImage(coverPath);

        if (coverImageView != null) {
            coverContainer.getChildren().add(coverImageView);
            StackPane.setAlignment(coverImageView, Pos.CENTER);
        }

        Button favoriteButton = new Button("✓");
        favoriteButton.getStyleClass().add("btn-org");
        favoriteButton.setFocusTraversable(false);

        boolean[] favoriteState = {favorite};

        favoriteButton.setAccessibleText(
                favorite ? "Remover dos favoritos" : "Adicionar aos favoritos"
        );

        updateFavoriteButton(favoriteButton, favoriteState[0]);

        StackPane.setMargin(favoriteButton, new Insets(8));
        coverContainer.getChildren().add(favoriteButton);

        favoriteButton.setOnAction(event -> {
            favoriteState[0] = !favoriteState[0];

            updateFavoriteButton(favoriteButton, favoriteState[0]);

            favoriteButton.setAccessibleText(
                    favoriteState[0]
                            ? "Remover dos favoritos"
                            : "Adicionar aos favoritos"
            );

            if (onFavoriteChanged != null) {
                onFavoriteChanged.accept(favoriteState[0]);
            }
        });

        Label titleLabel = new Label(
                title != null && !title.isBlank()
                        ? title
                        : "Sem título"
        );
        titleLabel.getStyleClass().add("title");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(Double.MAX_VALUE);

        Label metaLabel = new Label(
                progressText != null ? progressText : "0 / 0"
        );
        metaLabel.getStyleClass().add("meta");

        double validProgress = Double.isFinite(progress)
                ? Math.max(0.0, Math.min(1.0, progress))
                : 0.0;

        ProgressBar progressBar = new ProgressBar(validProgress);
        progressBar.getStyleClass().add("progress");
        progressBar.setMaxWidth(Double.MAX_VALUE);

        VBox infoContainer = new VBox(
                8,
                titleLabel,
                metaLabel,
                progressBar
        );
        infoContainer.getStyleClass().add("card-info");
        infoContainer.setAlignment(Pos.CENTER_LEFT);
        infoContainer.setPadding(new Insets(4, 2, 2, 2));

        VBox.setVgrow(infoContainer, Priority.NEVER);

        card.getChildren().addAll(coverContainer, infoContainer);

        return card;
    }


    private static void updateFavoriteButton(
            Button button,
            boolean favorite
    ) {
        button.setText("✓");

        if (favorite) {
            button.setStyle(
                    "-fx-background-color: #22c55e; " +
                            "-fx-text-fill: white; " +
                            "-fx-border-color: #16a34a; " +
                            "-fx-border-radius: 5px; " +
                            "-fx-background-radius: 5px;"
            );
        } else {
            button.setStyle("");
        }
    }

    private static ImageView loadCoverImage(String coverPath) {
        if (coverPath == null || coverPath.isBlank()) {
            return null;
        }

        File coverFile = new File(coverPath);

        if (!coverFile.isFile()) {
            System.err.println(
                    "Capa não encontrada: " + coverFile.getAbsolutePath()
            );
            return null;
        }

        try {
            Image image = new Image(
                    coverFile.toURI().toString(),
                    false
            );

            if (image.isError()
                    || image.getWidth() <= 0
                    || image.getHeight() <= 0) {
                System.err.println(
                        "Não foi possível carregar a capa: "
                                + coverFile.getAbsolutePath()
                );
                return null;
            }

            double imageWidth = image.getWidth();
            double imageHeight = image.getHeight();
            double targetRatio = COVER_WIDTH / COVER_HEIGHT;
            double imageRatio = imageWidth / imageHeight;

            Rectangle2D viewport;

            if (imageRatio > targetRatio) {
                double cropWidth = imageHeight * targetRatio;
                double cropX = (imageWidth - cropWidth) / 2.0;

                viewport = new Rectangle2D(
                        cropX, 0, cropWidth, imageHeight
                );
            } else {
                double cropHeight = imageWidth / targetRatio;
                double cropY = (imageHeight - cropHeight) / 2.0;

                viewport = new Rectangle2D(
                        0, cropY, imageWidth, cropHeight
                );
            }

            ImageView imageView = new ImageView(image);
            imageView.setViewport(viewport);
            imageView.setFitWidth(COVER_WIDTH);
            imageView.setFitHeight(COVER_HEIGHT);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);

            return imageView;

        } catch (RuntimeException exception) {
            System.err.println(
                    "Erro ao carregar a capa "
                            + coverFile.getAbsolutePath()
                            + ": "
                            + exception.getMessage()
            );
            return null;
        }
    }
}