
package com.war;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.TilePane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class Main extends Application {

    private TilePane cardGrid;
    private BorderPane root;
    private WindowBar windowBar;
    private javafx.scene.layout.HBox tabs;
    private String selectedCategory;
    private Stage primaryStage;

    private final String[] allCategories = {
            "Animes",
            "Desenhos",
            "Séries",
            "Filmes",
            "Mangás",
            "Livros",
            "Desejos"
    };

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        MediaScanner.initializeCategories();

        primaryStage.initStyle(StageStyle.UNDECORATED);

        root = new BorderPane();

        cardGrid = new TilePane();
        cardGrid.getStyleClass().add("grid");
        cardGrid.setHgap(20);
        cardGrid.setVgap(20);
        cardGrid.setPadding(new Insets(20));
        cardGrid.setAlignment(Pos.TOP_CENTER);
        cardGrid.setTileAlignment(Pos.TOP_CENTER);

        windowBar = new WindowBar(
                primaryStage,
                root,
                this::reloadMedia
        );

        root.setTop(windowBar.getView());
        tabs = windowBar.getTabs();

        ScrollPane scrollPane = new ScrollPane(cardGrid);
        scrollPane.setFitToWidth(true);
        root.setCenter(scrollPane);

        Scene scene = new Scene(root, 1250, 800);

        StyleA.Theme savedTheme;

        try {
            savedTheme = StyleA.Theme.valueOf(MediaConfig.getTheme());
        } catch (IllegalArgumentException e) {
            savedTheme = StyleA.Theme.CYBERPUNK;
        }

        StyleA.applyTheme(root, savedTheme);

        primaryStage.setTitle("MediaDashboard - JavaFX");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();

        reloadMedia();
    }

    private void reloadMedia() {
        String previousCategory = selectedCategory;
        List<String> activeCategories = new ArrayList<>();

        Path mediaDirectory = MediaScanner.getMediaDirectory();

        for (String category : allCategories) {
            List<MediaScanner.MediaItem> items =
                    MediaScanner.scanDirectory(
                            mediaDirectory.resolve(category).toString()
                    );

            if (!items.isEmpty()) {
                activeCategories.add(category);
            }
        }

        tabs.getChildren().clear();

        if (activeCategories.isEmpty()) {
            selectedCategory = null;

            showEmptyMessage(
                    "Nenhuma mídia encontrada. Adicione pastas "
                            + "nas categorias dentro de 'midia/'."
            );

            StyleA.refresh(root);
            return;
        }

        if (previousCategory != null
                && activeCategories.contains(previousCategory)) {
            selectedCategory = previousCategory;
        } else {
            selectedCategory = activeCategories.get(0);
        }

        for (String category : activeCategories) {
            Button button = new Button(category);
            button.getStyleClass().add("tab");

            if (category.equals(selectedCategory)) {
                button.getStyleClass().add("active");
            }

            button.setOnAction(event -> {
                selectedCategory = category;
                updateActiveTab();

                loadMediaCards(
                        mediaDirectory.resolve(category).toString()
                );
            });

            tabs.getChildren().add(button);
        }

        loadMediaCards(
                mediaDirectory.resolve(selectedCategory).toString()
        );

        StyleA.refresh(root);
    }

    private void updateActiveTab() {
        for (Node node : tabs.getChildren()) {
            if (node instanceof Button button) {
                button.getStyleClass().remove("active");

                if (button.getText().equals(selectedCategory)) {
                    button.getStyleClass().add("active");
                }
            }
        }

        StyleA.refresh(tabs);
    }

    private void showEmptyMessage(String message) {
        cardGrid.getChildren().clear();

        Label emptyLabel = new Label(message);
        emptyLabel.getStyleClass().add("header-title");
        emptyLabel.setFont(
                javafx.scene.text.Font.font("Segoe UI", 15)
        );

        cardGrid.getChildren().add(emptyLabel);

        StyleA.refresh(root);
    }

    private void loadMediaCards(String directoryPath) {
        cardGrid.getChildren().clear();

        List<MediaScanner.MediaItem> items =
                MediaScanner.scanDirectory(directoryPath);

        if (items.isEmpty()) {
            showEmptyMessage(
                    "Nenhuma mídia encontrada em: " + directoryPath
            );
            return;
        }

        items.sort(
                Comparator
                        .comparing(
                                (MediaScanner.MediaItem item) ->
                                        !MediaConfig.isFavorite(
                                                item.getFavoriteId()
                                        )
                        )
                        .thenComparing(
                                MediaScanner.MediaItem::getTitle,
                                String.CASE_INSENSITIVE_ORDER
                        )
        );

        for (MediaScanner.MediaItem item : items) {
            boolean favorite = MediaConfig.isFavorite(
                    item.getFavoriteId()
            );

            var card = CardComponent.create(
                    item.getTitle(),
                    "0 / 0",
                    0.0,
                    item.getCoverPath(),
                    favorite,
                    newFavoriteState -> {
                        MediaConfig.setFavorite(
                                item.getFavoriteId(),
                                newFavoriteState
                        );

                        loadMediaCards(directoryPath);
                    }
            );

            cardGrid.getChildren().add(card);
        }

        StyleA.refresh(root);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
