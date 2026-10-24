
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

import java.util.ArrayList;
import java.util.List;

public class Main extends Application {

    private TilePane cardGrid;
    private BorderPane root;
    private WindowBar windowBar;
    private javafx.scene.layout.HBox tabs;
    private String selectedCategory;
    private Stage primaryStage;

    private final String[] allCategories = {
            "Animes", "Desenhos", "Séries", "Filmes",
            "Mangás", "Livros", "Desejos"
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

        // Inicializa a barra da janela
        windowBar = new WindowBar(
                primaryStage,
                root,
                this::reloadMedia
        );

        root.setTop(windowBar.getView());
        tabs = windowBar.getTabs();

        // Área de conteúdo
        ScrollPane scrollPane = new ScrollPane(cardGrid);
        scrollPane.setFitToWidth(true);
        root.setCenter(scrollPane);

        Scene scene = new Scene(root, 1250, 800);

        StyleA.applyTheme(root, StyleA.Theme.CYBERPUNK);

        primaryStage.setTitle("MediaDashboard - JavaFX");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();

        reloadMedia();
    }

    private void reloadMedia() {
        String previousCategory = selectedCategory;
        List<String> activeCategories = new ArrayList<>();

        for (String category : allCategories) {
            List<MediaScanner.MediaItem> items =
                    MediaScanner.scanDirectory("midia/" + category);

            if (!items.isEmpty()) {
                activeCategories.add(category);
            }
        }

        tabs.getChildren().clear();

        if (activeCategories.isEmpty()) {
            selectedCategory = null;

            showEmptyMessage(
                    "Nenhuma mídia encontrada. Adicione pastas " +
                            "nas categorias dentro de 'midia/'."
            );

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

            button.setOnAction(e -> {
                selectedCategory = category;
                updateActiveTab();
                loadMediaCards("midia/" + category);
            });

            tabs.getChildren().add(button);
        }

        loadMediaCards("midia/" + selectedCategory);

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

        for (MediaScanner.MediaItem item : items) {
            var card = CardComponent.create(
                    item.getTitle(),
                    "0 / 0",
                    0.0,
                    item.getCoverPath()
            );

            cardGrid.getChildren().add(card);
        }

        StyleA.refresh(root);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
