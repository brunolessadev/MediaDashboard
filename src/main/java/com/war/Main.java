
package com.war;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class Main extends Application {

    private TilePane cardGrid;
    private BorderPane root;
    private HBox tabs;
    private String selectedCategory;

    private final String[] allCategories = {
            "Animes", "Desenhos", "Séries", "Filmes",
            "Mangás", "Livros", "Desejos"
    };

    @Override
    public void start(Stage primaryStage) {
        MediaScanner.initializeCategories();

        root = new BorderPane();

        cardGrid = new TilePane();
        cardGrid.getStyleClass().add("grid");
        cardGrid.setHgap(20);
        cardGrid.setVgap(20);
        cardGrid.setPadding(new Insets(20));

        root.setTop(createHeader());

        ScrollPane scrollPane = new ScrollPane(cardGrid);
        scrollPane.setFitToWidth(true);
        root.setCenter(scrollPane);

        Scene scene = new Scene(root, 1250, 800);

        StyleA.applyTheme(root, StyleA.Theme.CYBERPUNK);

        primaryStage.setTitle("MediaDashboard - JavaFX");
        primaryStage.setScene(scene);
        primaryStage.show();

        reloadMedia();
    }

    private HBox createHeader() {
        HBox header = new HBox(15);
        header.getStyleClass().add("header");
        header.setPadding(new Insets(20));

        Label titleLabel = new Label("Meu Hub");
        titleLabel.getStyleClass().add("header-title");

        tabs = new HBox(4);
        tabs.getStyleClass().add("tabs");

        ComboBox<String> themeSelector = new ComboBox<>();
        themeSelector.getItems().addAll(
                "Cyberpunk Neon",
                "Crunchyroll",
                "Nordico"
        );
        themeSelector.setValue("Cyberpunk Neon");
        themeSelector.getStyleClass().add("theme-selector");

        themeSelector.setOnAction(e ->
                StyleA.applyTheme(root, themeSelector.getValue())
        );

        TextField searchField = new TextField();
        searchField.setPromptText("Pesquisar...");
        searchField.setId("search");

        Button reloadButton = new Button("⟳ Reload");
        reloadButton.setId("reload-button");
        reloadButton.setTooltip(
                new Tooltip("Recarregar categorias e mídias")
        );
        reloadButton.setOnAction(e -> reloadMedia());

        header.getChildren().addAll(
                titleLabel,
                tabs,
                themeSelector,
                searchField,
                reloadButton
        );

        return header;
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
