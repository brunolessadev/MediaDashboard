
package com.war;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.ArrayList;
import java.util.List;

public class Main extends Application {

    private TilePane cardGrid;
    private BorderPane root;
    private HBox tabs;
    private String selectedCategory;
    private Stage primaryStage;

    private double dragOffsetX;
    private double dragOffsetY;

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

        root.setTop(createWindowBar());

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

    private HBox createWindowBar() {
        HBox bar = new HBox(12);
        bar.getStyleClass().add("header");
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(8, 12, 8, 16));

        Label windowTitle = new Label("MediaDashboard");
        windowTitle.getStyleClass().add("header-title");

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

        tabs = new HBox(4);
        tabs.getStyleClass().add("tabs");
        tabs.setAlignment(Pos.CENTER);

        TextField searchField = new TextField();
        searchField.setPromptText("Pesquisar...");
        searchField.setId("search");
        searchField.setPrefWidth(180);

        Button reloadButton = new Button("⟳ Reload");
        reloadButton.setId("reload-button");
        reloadButton.setTooltip(
                new Tooltip("Recarregar categorias e mídias")
        );
        reloadButton.setOnAction(e -> reloadMedia());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button minimizeButton = new Button("—");
        minimizeButton.setTooltip(new Tooltip("Minimizar"));
        minimizeButton.getStyleClass().add("window-button");
        minimizeButton.setOnAction(e ->
                primaryStage.setIconified(true)
        );

        Button maximizeButton = new Button("□");
        maximizeButton.setTooltip(
                new Tooltip("Maximizar / Restaurar")
        );
        maximizeButton.getStyleClass().add("window-button");
        maximizeButton.setOnAction(e ->
                primaryStage.setMaximized(!primaryStage.isMaximized())
        );

        Button closeButton = new Button("✕");
        closeButton.setTooltip(new Tooltip("Fechar"));
        closeButton.getStyleClass().addAll(
                "window-button",
                "window-close"
        );
        closeButton.setOnAction(e -> primaryStage.close());

        HBox windowControls = new HBox(
                6,
                minimizeButton,
                maximizeButton,
                closeButton
        );
        windowControls.setAlignment(Pos.CENTER_RIGHT);

        bar.getChildren().addAll(
                windowTitle,
                themeSelector,
                tabs,
                searchField,
                reloadButton,
                spacer,
                windowControls
        );

        bar.setOnMousePressed((MouseEvent e) -> {
            if (e.getTarget() instanceof Button
                    || e.getTarget() instanceof ComboBox<?>
                    || e.getTarget() instanceof TextField) {
                return;
            }

            dragOffsetX = e.getScreenX() - primaryStage.getX();
            dragOffsetY = e.getScreenY() - primaryStage.getY();
        });

        bar.setOnMouseDragged((MouseEvent e) -> {
            if (!primaryStage.isMaximized()
                    && e.isPrimaryButtonDown()) {
                primaryStage.setX(e.getScreenX() - dragOffsetX);
                primaryStage.setY(e.getScreenY() - dragOffsetY);
            }
        });

        return bar;
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
