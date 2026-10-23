package com.war;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.stage.Stage;

import java.util.List;

public class Main extends Application {

    private TilePane cardGrid;

    @Override
    public void start(Stage primaryStage) {
        // Garante que a estrutura de pastas 'midia' e categorias é criada
        MediaScanner.initializeCategories();

        BorderPane root = new BorderPane();
        root.getStyleClass().add("theme-cyberpunk");

        // Cabeçalho
        root.setTop(createHeader());

        // Grelha de Cartões
        cardGrid = new TilePane();
        cardGrid.getStyleClass().add("grid");
        cardGrid.setHgap(20);
        cardGrid.setVgap(20);
        cardGrid.setPadding(new Insets(20));

        // Carrega as mídias da categoria "Animes"
        loadMediaCards("midia/Animes");

        ScrollPane scrollPane = new ScrollPane(cardGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        root.setCenter(scrollPane);

        Scene scene = new Scene(root, 1200, 800);
        loadStyles(scene);

        primaryStage.setTitle("MediaDashboard - JavaFX");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void loadMediaCards(String directoryPath) {
        cardGrid.getChildren().clear();

        List<MediaScanner.MediaItem> items = MediaScanner.scanDirectory(directoryPath);

        if (items.isEmpty()) {
            Label emptyLabel = new Label("Nenhuma mídia encontrada em: " + directoryPath);
            emptyLabel.getStyleClass().add("header-title");
            emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: var(--text-muted);");
            cardGrid.getChildren().add(emptyLabel);
            return;
        }

        for (MediaScanner.MediaItem item : items) {
            // Passa o caminho da capa detetada (.jpg, .png, etc.) para o componente do card
            var card = CardComponent.create(item.getTitle(), "0 / 0", 0.0, item.getCoverPath());
            cardGrid.getChildren().add(card);
        }
    }

    private HBox createHeader() {
        HBox header = new HBox(20);
        header.getStyleClass().add("header");
        header.setPadding(new Insets(20));

        Label titleLabel = new Label("MediaDashboard");
        titleLabel.getStyleClass().add("header-title");

        HBox tabs = new HBox(4);
        tabs.getStyleClass().add("tabs");
        Button tabAnimes = new Button("Animes");
        tabAnimes.getStyleClass().addAll("tab", "active");
        Button tabSeries = new Button("Séries");
        tabSeries.getStyleClass().add("tab");
        Button tabFilmes = new Button("Filmes");
        tabFilmes.getStyleClass().add("tab");
        tabs.getChildren().addAll(tabAnimes, tabSeries, tabFilmes);

        TextField searchField = new TextField();
        searchField.setPromptText("Pesquisar mídias...");
        searchField.setId("search");

        header.getChildren().addAll(titleLabel, tabs, searchField);
        return header;
    }

    private void loadStyles(Scene scene) {
        String[] cssFiles = {
                "/styles/01-variables.css",
                "/styles/02-themes.css",
                "/styles/03-base.css",
                "/styles/04-header.css",
                "/styles/05-grid-cards.css",
                "/styles/06-details.css",
                "/styles/07-episodes.css",
                "/styles/08-breadcrumb.css"
        };

        for (String cssFile : cssFiles) {
            try {
                String cssPath = getClass().getResource(cssFile).toExternalForm();
                scene.getStylesheets().add(cssPath);
            } catch (Exception e) {
                System.out.println("Aviso: Ficheiro não encontrado -> " + cssFile);
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}