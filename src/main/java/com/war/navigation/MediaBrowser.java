package com.war.navigation;

import com.war.CardComponent;
import com.war.MediaConfig;
import com.war.MediaScanner;
import com.war.style.StyleA;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

public class MediaBrowser {

    private final BorderPane root;
    private final HBox tabs;
    private final Consumer<Path> onPlay;

    private final TilePane cardGrid = new TilePane();
    private final Label breadcrumbLabel = new Label();
    private final Button backButton = new Button("← Voltar");
    private final VBox browserView;

    private final Deque<Path> navigationHistory = new ArrayDeque<>();

    private final String[] categories = {"Animes", "Desenhos", "Séries", "Filmes", "Mangás", "Livros", "Desejos"};

    private String selectedCategory;
    private Path currentDirectory;

    public MediaBrowser(BorderPane root, HBox tabs, Consumer<Path> onPlay) {
        this.root = root;
        this.tabs = tabs;
        this.onPlay = onPlay;

        cardGrid.getStyleClass().add("grid");
        cardGrid.setHgap(20);
        cardGrid.setVgap(20);
        cardGrid.setPadding(new Insets(20));
        cardGrid.setAlignment(Pos.TOP_CENTER);
        cardGrid.setTileAlignment(Pos.TOP_CENTER);

        breadcrumbLabel.getStyleClass().add("header-title");

        backButton.setOnAction(event -> goBack());

        HBox navigationBar = new HBox(12, backButton, breadcrumbLabel);
        navigationBar.setAlignment(Pos.CENTER_LEFT);
        navigationBar.setPadding(new Insets(12, 20, 0, 20));

        ScrollPane scrollPane = new ScrollPane(cardGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        browserView = new VBox(8, navigationBar, scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
    }

    public VBox getView() {
        return browserView;
    }

    public void reloadMedia() {
        Path mediaRoot = MediaScanner.getMediaDirectory().toAbsolutePath().normalize();

        List<String> availableCategories = new ArrayList<>();

        for (String category : categories) {
            Path path = mediaRoot.resolve(category);

            if (!MediaScanner.scanDirectory(path.toString()).isEmpty()) {
                availableCategories.add(category);
            }
        }

        tabs.getChildren().clear();

        if (availableCategories.isEmpty()) {
            selectedCategory = null;
            currentDirectory = null;
            navigationHistory.clear();

            showMessage("Nenhuma mídia encontrada em midia/.");
            updateNavigation();
            return;
        }

        if (selectedCategory == null || !availableCategories.contains(selectedCategory)) {
            selectedCategory = availableCategories.get(0);
        }

        for (String category : availableCategories) {
            Button tab = new Button(category);
            tab.getStyleClass().add("tab");

            if (category.equals(selectedCategory)) {
                tab.getStyleClass().add("active");
            }

            tab.setOnAction(event -> {
                selectedCategory = category;
                currentDirectory = mediaRoot.resolve(category);
                navigationHistory.clear();

                updateTabs();
                loadDirectory(currentDirectory);
            });

            tabs.getChildren().add(tab);
        }

        Path categoryRoot = mediaRoot.resolve(selectedCategory).toAbsolutePath().normalize();

        if (currentDirectory == null || !Files.isDirectory(currentDirectory) || !currentDirectory.startsWith(categoryRoot)) {
            currentDirectory = categoryRoot;
            navigationHistory.clear();
        }

        loadDirectory(currentDirectory);
        StyleA.refresh(root);
    }

    private void updateTabs() {
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

    private void loadDirectory(Path directory) {
        if (directory == null || !Files.isDirectory(directory)) {
            showMessage("Esta pasta não existe mais.");
            updateNavigation();
            return;
        }

        currentDirectory = directory.toAbsolutePath().normalize();
        root.setCenter(browserView);
        cardGrid.getChildren().clear();

        List<MediaScanner.MediaItem> items = MediaScanner.scanDirectory(currentDirectory.toString());

        items.sort(Comparator.comparing((MediaScanner.MediaItem item) -> !item.isDirectory() && !MediaConfig.isFavorite(item.getFavoriteId())).thenComparing(MediaScanner.MediaItem::getTitle, String.CASE_INSENSITIVE_ORDER));

        if (items.isEmpty()) {
            showMessage("Nenhuma pasta ou vídeo encontrado aqui.");
            updateNavigation();
            return;
        }

        for (MediaScanner.MediaItem item : items) {
            Path itemPath = Path.of(item.getFolderPath());

            MediaScanner.VideoProgress progress = MediaScanner.getVideoProgress(itemPath);

            String progressText = progress.getWatched() + " / " + progress.getTotal() + " vídeos assistidos";

            if (!item.isDirectory() && progress.getTotal() == 1) {
                progressText = progress.getWatched() == 1 ? "Assistido" : "Não assistido";
            }

            VBox card = CardComponent.create(item.getTitle(), progressText, progress.getProgress(), item.getCoverPath(), MediaConfig.isFavorite(item.getFavoriteId()), item.isDirectory(), () -> openItem(item), favorite -> {
                MediaConfig.setFavorite(item.getFavoriteId(), favorite);
                loadDirectory(currentDirectory);
            });

            cardGrid.getChildren().add(card);
        }

        updateNavigation();
        StyleA.refresh(root);
    }

    private void openItem(MediaScanner.MediaItem item) {
        Path path = Path.of(item.getFolderPath());

        if (item.isDirectory()) {
            if (!Files.isDirectory(path)) {
                showMessage("Esta pasta não existe mais.");
                return;
            }

            navigationHistory.push(currentDirectory);
            loadDirectory(path);
        } else {
            onPlay.accept(path);
        }
    }

    private void goBack() {
        if (!navigationHistory.isEmpty()) {
            loadDirectory(navigationHistory.pop());
            return;
        }

        if (currentDirectory == null || selectedCategory == null) {
            return;
        }

        Path categoryRoot = MediaScanner.getMediaDirectory().resolve(selectedCategory).toAbsolutePath().normalize();

        if (!currentDirectory.equals(categoryRoot)) {
            navigationHistory.clear();
            loadDirectory(categoryRoot);
        }

        updateNavigation();
    }

    public void refreshCurrentDirectory() {
        if (currentDirectory != null) {
            loadDirectory(currentDirectory);
        } else {
            reloadMedia();
        }
    }

    public void showMessage(String message) {
        cardGrid.getChildren().clear();

        Label label = new Label(message);
        label.getStyleClass().add("header-title");
        label.setWrapText(true);
        label.setPadding(new Insets(20));

        cardGrid.getChildren().add(label);
        root.setCenter(browserView);
        StyleA.refresh(root);
    }

    private void updateNavigation() {
        boolean canGoBack = !navigationHistory.isEmpty();

        if (currentDirectory != null && selectedCategory != null) {
            Path categoryRoot = MediaScanner.getMediaDirectory().resolve(selectedCategory).toAbsolutePath().normalize();

            canGoBack |= !currentDirectory.equals(categoryRoot);
        }

        backButton.setDisable(!canGoBack);

        if (currentDirectory == null) {
            breadcrumbLabel.setText("Biblioteca");
            return;
        }

        Path mediaRoot = MediaScanner.getMediaDirectory().toAbsolutePath().normalize();

        if (currentDirectory.startsWith(mediaRoot)) {
            breadcrumbLabel.setText("Biblioteca / " + mediaRoot.relativize(currentDirectory).toString().replace('\\', '/').replace("/", " / "));
        } else {
            breadcrumbLabel.setText(currentDirectory.getFileName().toString());
        }
    }
}