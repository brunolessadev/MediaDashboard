
package com.war.navigation;

import com.war.CardComponent;
import com.war.MediaConfig;
import com.war.MediaScanner;
import com.war.episode.EpisodeList;
import com.war.style.StyleA;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class MediaBrowser {

    private final BorderPane root;
    private final HBox tabs;
    private final Consumer<Path> onPlay;

    private final TilePane cardGrid = new TilePane();
    private final TilePane photoGrid = new TilePane();
    private final VBox directoryContent = new VBox(20);
    private final Label breadcrumbLabel = new Label();
    private final Button backButton = new Button("← Voltar");
    private final VBox browserView;

    private final Slider thumbnailSizeSlider = new Slider(80, 500, 150);
    private final Label thumbnailSizeLabel = new Label("150 px");
    private final HBox photoControls = new HBox(12);

    private final Deque<Path> navigationHistory = new ArrayDeque<>();

    private final String[] categories = {
            "Animes",
            "Desenhos",
            "Séries",
            "Filmes",
            "Mangás",
            "Livros",
            "Fotos",
            "Vídeos",
            "Desejos"
    };

    private String selectedCategory;
    private Path currentDirectory;

    private List<MediaScanner.MediaItem> currentPhotoItems = List.of();
    private int currentPhotoIndex = 0;

    private BorderPane photoViewer;
    private ImageView fullSizeImageView;
    private Label photoCounterLabel;
    private Label photoNameLabel;
    private Label photoErrorLabel;
    private Button previousPhotoButton;
    private Button nextPhotoButton;

    private Scene photoViewerScene;
    private Stage photoViewerStage;

    private Node previousRootTop;
    private Node previousRootBottom;
    private Node previousRootLeft;
    private Node previousRootRight;
    private Node previousRootCenter;

    private boolean stageWasFullScreen;
    private KeyCombination previousFullScreenExitKeyCombination;

    private final EventHandler<KeyEvent> photoViewerKeyHandler =
            this::handlePhotoViewerKey;

    public MediaBrowser(
            BorderPane root,
            HBox tabs,
            Consumer<Path> onPlay
    ) {
        this.root = root;
        this.tabs = tabs;
        this.onPlay = onPlay;

        cardGrid.getStyleClass().add("grid");
        cardGrid.setHgap(20);
        cardGrid.setVgap(20);
        cardGrid.setPadding(new Insets(20));
        cardGrid.setAlignment(Pos.TOP_CENTER);
        cardGrid.setTileAlignment(Pos.TOP_CENTER);

        photoGrid.getStyleClass().add("grid");
        photoGrid.setHgap(14);
        photoGrid.setVgap(18);
        photoGrid.setPadding(new Insets(20));
        photoGrid.setAlignment(Pos.TOP_LEFT);
        photoGrid.setTileAlignment(Pos.TOP_LEFT);

        breadcrumbLabel.getStyleClass().add("header-title");
        backButton.setOnAction(event -> goBack());

        HBox navigationBar = new HBox(
                12,
                backButton,
                breadcrumbLabel
        );
        navigationBar.setAlignment(Pos.CENTER_LEFT);
        navigationBar.setPadding(new Insets(12, 20, 0, 20));

        thumbnailSizeSlider.setMin(80);
        thumbnailSizeSlider.setMax(500);
        thumbnailSizeSlider.setValue(150);
        thumbnailSizeSlider.setShowTickLabels(false);
        thumbnailSizeSlider.setShowTickMarks(false);
        thumbnailSizeSlider.setBlockIncrement(10);
        thumbnailSizeSlider.setPrefWidth(180);

        Label sizeTitle = new Label("Tamanho das miniaturas:");
        sizeTitle.getStyleClass().add("meta");
        thumbnailSizeLabel.getStyleClass().add("meta");

        photoControls.getChildren().setAll(
                sizeTitle,
                thumbnailSizeSlider,
                thumbnailSizeLabel
        );
        photoControls.setAlignment(Pos.CENTER_LEFT);
        photoControls.setPadding(new Insets(16, 20, 0, 20));

        thumbnailSizeSlider.valueProperty().addListener(
                (observable, oldValue, newValue) -> {
                    int size = newValue.intValue();
                    thumbnailSizeLabel.setText(size + " px");
                    renderPhotoThumbnails();
                }
        );

        ScrollPane scrollPane = new ScrollPane(directoryContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        browserView = new VBox(8, navigationBar, scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        directoryContent.getChildren().add(cardGrid);
    }

    public VBox getView() {
        return browserView;
    }

    public void reloadMedia() {
        Path mediaRoot = MediaScanner.getMediaDirectory()
                .toAbsolutePath()
                .normalize();

        List<String> availableCategories = new ArrayList<>();

        for (String category : categories) {
            Path path = mediaRoot.resolve(category);

            if (Files.isDirectory(path)
                    && !MediaScanner.scanDirectory(path.toString()).isEmpty()) {
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

        if (selectedCategory == null
                || !availableCategories.contains(selectedCategory)) {
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

        Path categoryRoot = mediaRoot.resolve(selectedCategory)
                .toAbsolutePath()
                .normalize();

        if (currentDirectory == null
                || !Files.isDirectory(currentDirectory)
                || !currentDirectory.startsWith(categoryRoot)) {
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
        photoGrid.getChildren().clear();

        List<MediaScanner.MediaItem> items =
                MediaScanner.scanDirectory(currentDirectory.toString());

        List<MediaScanner.MediaItem> directories = items.stream()
                .filter(MediaScanner.MediaItem::isDirectory)
                .sorted(
                        Comparator.comparing(
                                MediaScanner.MediaItem::getTitle,
                                String.CASE_INSENSITIVE_ORDER
                        )
                )
                .toList();

        for (MediaScanner.MediaItem item : directories) {
            Path itemPath = Path.of(item.getFolderPath());

            String progressText;
            double progressValue;

            if ("Livros".equals(selectedCategory)) {
                int count = countBooks(itemPath);
                progressText = count + (count == 1 ? " livro" : " livros");
                progressValue = 0;
            } else if ("Fotos".equals(selectedCategory)) {
                int count = countPhotos(itemPath);
                progressText = count + (count == 1 ? " foto" : " fotos");
                progressValue = 0;
            } else {
                MediaScanner.VideoProgress progress =
                        MediaScanner.getVideoProgress(itemPath);

                progressText = progress.getWatched()
                        + " / "
                        + progress.getTotal()
                        + " vídeos";

                progressValue = progress.getProgress();
            }

            VBox card = CardComponent.create(
                    item.getTitle(),
                    progressText,
                    progressValue,
                    item.getCoverPath(),
                    MediaConfig.isFavorite(item.getFavoriteId()),
                    true,
                    () -> openItem(item),
                    favorite -> {
                        MediaConfig.setFavorite(
                                item.getFavoriteId(),
                                favorite
                        );
                        refreshCurrentDirectory();
                    }
            );

            cardGrid.getChildren().add(card);
        }

        cardGrid.setVisible(!directories.isEmpty());
        cardGrid.setManaged(!directories.isEmpty());

        if ("Livros".equals(selectedCategory)) {
            showBookFiles(items);
        } else if ("Fotos".equals(selectedCategory)) {
            showPhotos(items);
        } else {
            Consumer<Path> playbackAction;

            if ("Vídeos".equals(selectedCategory)) {
                playbackAction = this::openVideoWithWindows;
            } else {
                playbackAction = onPlay;
            }

            EpisodeList episodeList = new EpisodeList(
                    currentDirectory,
                    this::refreshCurrentDirectory,
                    playbackAction,
                    "Vídeos".equals(selectedCategory)
            );

            directoryContent.getChildren().setAll(
                    cardGrid,
                    episodeList.getView()
            );
        }

        updateNavigation();
        StyleA.refresh(root);
    }

    private void showPhotos(List<MediaScanner.MediaItem> items) {
        currentPhotoItems = items.stream()
                .filter(item -> !item.isDirectory())
                .filter(item -> MediaScanner.isPhotoFile(
                        Path.of(item.getFolderPath())
                ))
                .toList();

        List<Node> content = new ArrayList<>();

        if (!cardGrid.getChildren().isEmpty()) {
            content.add(cardGrid);
        }

        if (!currentPhotoItems.isEmpty()) {
            content.add(photoControls);
            content.add(photoGrid);
            renderPhotoThumbnails();
        } else if (cardGrid.getChildren().isEmpty()) {
            Label emptyLabel = new Label(
                    "Nenhuma foto ou subpasta encontrada nesta pasta."
            );
            emptyLabel.getStyleClass().add("header-title");
            emptyLabel.setWrapText(true);
            emptyLabel.setPadding(new Insets(20));
            content.add(emptyLabel);
        } else {
            Label emptyLabel = new Label(
                    "Esta pasta não contém fotos diretamente."
            );
            emptyLabel.getStyleClass().add("meta");
            emptyLabel.setPadding(new Insets(0, 20, 20, 20));
            content.add(emptyLabel);
        }

        directoryContent.getChildren().setAll(content);
    }

    private void renderPhotoThumbnails() {
        if (!"Fotos".equals(selectedCategory)) {
            photoGrid.getChildren().clear();
            return;
        }

        photoGrid.getChildren().clear();

        int size = (int) thumbnailSizeSlider.getValue();

        for (int index = 0; index < currentPhotoItems.size(); index++) {
            MediaScanner.MediaItem item = currentPhotoItems.get(index);
            Path imagePath = Path.of(item.getFolderPath());

            if (!Files.isRegularFile(imagePath)) {
                continue;
            }

            int photoIndex = index;

            VBox thumbnailCard = new VBox(8);
            thumbnailCard.getStyleClass().add("card");
            thumbnailCard.setAlignment(Pos.TOP_CENTER);
            thumbnailCard.setPadding(new Insets(8));
            thumbnailCard.setPrefWidth(size + 24);
            thumbnailCard.setMinWidth(size + 24);
            thumbnailCard.setMaxWidth(size + 24);
            thumbnailCard.setCursor(Cursor.HAND);

            StackPane imageContainer = new StackPane();
            imageContainer.setPrefSize(size, size);
            imageContainer.setMinSize(size, size);
            imageContainer.setMaxSize(size, size);
            imageContainer.setAlignment(Pos.CENTER);

            try {
                Image image = new Image(
                        imagePath.toUri().toString(),
                        size,
                        size,
                        true,
                        true,
                        false
                );

                if (!image.isError()
                        && image.getWidth() > 0
                        && image.getHeight() > 0) {
                    ImageView imageView = new ImageView(image);
                    imageView.setFitWidth(size);
                    imageView.setFitHeight(size);
                    imageView.setPreserveRatio(true);
                    imageView.setSmooth(true);

                    imageContainer.getChildren().add(imageView);
                } else {
                    addImagePlaceholder(imageContainer);
                }
            } catch (RuntimeException exception) {
                addImagePlaceholder(imageContainer);
            }

            Label nameLabel = new Label(item.getTitle());
            nameLabel.getStyleClass().add("title");
            nameLabel.setWrapText(true);
            nameLabel.setMaxWidth(size);
            nameLabel.setAlignment(Pos.CENTER);

            thumbnailCard.getChildren().addAll(
                    imageContainer,
                    nameLabel
            );

            thumbnailCard.setOnMouseClicked(event -> {
                if (event.getClickCount() == 1) {
                    openPhotoViewer(photoIndex);
                }
            });

            photoGrid.getChildren().add(thumbnailCard);
        }
    }

    private void addImagePlaceholder(StackPane container) {
        Label placeholder = new Label("🖼️");
        placeholder.setStyle("-fx-font-size: 32px;");
        container.getChildren().setAll(placeholder);
    }

    private void openPhotoViewer(int index) {
        if (currentPhotoItems.isEmpty()
                || index < 0
                || index >= currentPhotoItems.size()) {
            return;
        }

        currentPhotoIndex = index;

        if (photoViewer != null) {
            updatePhotoViewer();
            return;
        }

        previousRootTop = root.getTop();
        previousRootBottom = root.getBottom();
        previousRootLeft = root.getLeft();
        previousRootRight = root.getRight();
        previousRootCenter = root.getCenter();

        photoViewerScene = root.getScene();
        photoViewerStage = null;

        if (photoViewerScene != null
                && photoViewerScene.getWindow() instanceof Stage stage) {
            photoViewerStage = stage;
            stageWasFullScreen = stage.isFullScreen();
            previousFullScreenExitKeyCombination =
                    stage.getFullScreenExitKeyCombination();

            stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        }

        createPhotoViewer();

        root.setTop(null);
        root.setBottom(null);
        root.setLeft(null);
        root.setRight(null);
        root.setCenter(photoViewer);

        if (photoViewerStage != null) {
            photoViewerStage.setFullScreen(true);
        }

        if (photoViewerScene != null) {
            photoViewerScene.addEventFilter(
                    KeyEvent.KEY_PRESSED,
                    photoViewerKeyHandler
            );
        }

        updatePhotoViewer();
        StyleA.refresh(root);
        photoViewer.requestFocus();
    }

    private void createPhotoViewer() {
        photoViewer = new BorderPane();
        photoViewer.setFocusTraversable(true);
        photoViewer.setBackground(
                new Background(
                        new BackgroundFill(
                                Color.BLACK,
                                CornerRadii.EMPTY,
                                Insets.EMPTY
                        )
                )
        );

        fullSizeImageView = new ImageView();
        fullSizeImageView.setPreserveRatio(true);
        fullSizeImageView.setSmooth(true);

        fullSizeImageView.fitWidthProperty().bind(
                photoViewer.widthProperty().multiply(0.85)
        );
        fullSizeImageView.fitHeightProperty().bind(
                photoViewer.heightProperty().multiply(0.78)
        );

        StackPane imageArea = new StackPane();
        imageArea.setAlignment(Pos.CENTER);
        imageArea.getChildren().add(fullSizeImageView);

        photoErrorLabel = new Label();
        photoErrorLabel.setTextFill(Color.WHITE);
        photoErrorLabel.setWrapText(true);
        photoErrorLabel.setAlignment(Pos.CENTER);
        photoErrorLabel.setVisible(false);
        photoErrorLabel.setManaged(false);
        imageArea.getChildren().add(photoErrorLabel);

        previousPhotoButton = createViewerButton("❮");
        nextPhotoButton = createViewerButton("❯");

        previousPhotoButton.setOnAction(event -> showPreviousPhoto());
        nextPhotoButton.setOnAction(event -> showNextPhoto());

        photoViewer.setCenter(imageArea);
        photoViewer.setLeft(previousPhotoButton);
        photoViewer.setRight(nextPhotoButton);

        Button closeButton = createViewerButton("✕");
        closeButton.setOnAction(event -> closePhotoViewer());

        photoCounterLabel = new Label();
        photoCounterLabel.setTextFill(Color.WHITE);

        BorderPane topBar = new BorderPane();
        topBar.setLeft(photoCounterLabel);
        topBar.setRight(closeButton);
        topBar.setPadding(new Insets(16, 20, 12, 20));

        photoNameLabel = new Label();
        photoNameLabel.setTextFill(Color.WHITE);
        photoNameLabel.setWrapText(true);

        HBox bottomBar = new HBox(photoNameLabel);
        bottomBar.setAlignment(Pos.CENTER);
        bottomBar.setPadding(new Insets(12, 20, 18, 20));

        photoViewer.setTop(topBar);
        photoViewer.setBottom(bottomBar);
    }

    private Button createViewerButton(String text) {
        Button button = new Button(text);
        button.setFocusTraversable(false);
        button.setMinSize(48, 48);
        button.setTextFill(Color.WHITE);
        button.setBackground(Background.EMPTY);
        button.setCursor(Cursor.HAND);
        return button;
    }

    private void handlePhotoViewerKey(KeyEvent event) {
        if (photoViewer == null) {
            return;
        }

        if (event.getCode() == KeyCode.RIGHT) {
            showNextPhoto();
            event.consume();
        } else if (event.getCode() == KeyCode.LEFT) {
            showPreviousPhoto();
            event.consume();
        } else if (event.getCode() == KeyCode.ESCAPE) {
            closePhotoViewer();
            event.consume();
        }
    }

    private void showPreviousPhoto() {
        if (currentPhotoIndex > 0) {
            currentPhotoIndex--;
            updatePhotoViewer();
        }
    }

    private void showNextPhoto() {
        if (currentPhotoIndex < currentPhotoItems.size() - 1) {
            currentPhotoIndex++;
            updatePhotoViewer();
        }
    }

    private void updatePhotoViewer() {
        if (photoViewer == null
                || currentPhotoIndex < 0
                || currentPhotoIndex >= currentPhotoItems.size()) {
            return;
        }

        MediaScanner.MediaItem item = currentPhotoItems.get(currentPhotoIndex);
        Path imagePath = Path.of(item.getFolderPath());

        photoCounterLabel.setText(
                (currentPhotoIndex + 1) + " / " + currentPhotoItems.size()
        );
        photoNameLabel.setText(item.getTitle());

        previousPhotoButton.setDisable(currentPhotoIndex == 0);
        nextPhotoButton.setDisable(
                currentPhotoIndex >= currentPhotoItems.size() - 1
        );

        if (!Files.isRegularFile(imagePath)) {
            showPhotoError("O arquivo não existe mais.");
            return;
        }

        try {
            Image image = new Image(
                    imagePath.toUri().toString(),
                    0,
                    0,
                    true,
                    true,
                    false
            );

            if (image.isError()
                    || image.getWidth() <= 0
                    || image.getHeight() <= 0) {
                showPhotoError("Não foi possível abrir esta imagem.");
                return;
            }

            fullSizeImageView.setImage(image);
            photoErrorLabel.setText("");
            photoErrorLabel.setVisible(false);
            photoErrorLabel.setManaged(false);
        } catch (RuntimeException exception) {
            showPhotoError("Erro ao abrir a imagem.");
        }
    }

    private void showPhotoError(String message) {
        fullSizeImageView.setImage(null);
        photoErrorLabel.setText(message);
        photoErrorLabel.setVisible(true);
        photoErrorLabel.setManaged(true);
    }

    private void closePhotoViewer() {
        if (photoViewer == null) {
            return;
        }

        if (photoViewerScene != null) {
            photoViewerScene.removeEventFilter(
                    KeyEvent.KEY_PRESSED,
                    photoViewerKeyHandler
            );
        }

        root.setTop(previousRootTop);
        root.setBottom(previousRootBottom);
        root.setLeft(previousRootLeft);
        root.setRight(previousRootRight);
        root.setCenter(previousRootCenter);

        if (photoViewerStage != null) {
            photoViewerStage.setFullScreen(stageWasFullScreen);

            if (previousFullScreenExitKeyCombination != null) {
                photoViewerStage.setFullScreenExitKeyCombination(
                        previousFullScreenExitKeyCombination
                );
            }
        }

        photoViewer = null;
        fullSizeImageView = null;
        photoCounterLabel = null;
        photoNameLabel = null;
        photoErrorLabel = null;
        previousPhotoButton = null;
        nextPhotoButton = null;
        photoViewerScene = null;
        photoViewerStage = null;

        previousRootTop = null;
        previousRootBottom = null;
        previousRootLeft = null;
        previousRootRight = null;
        previousRootCenter = null;

        StyleA.refresh(root);
    }

    private void showBookFiles(List<MediaScanner.MediaItem> items) {
        VBox bookList = new VBox(10);
        bookList.setPadding(new Insets(0, 20, 20, 20));

        Label sectionTitle = new Label("Livros nesta pasta");
        sectionTitle.getStyleClass().add("header-title");
        bookList.getChildren().add(sectionTitle);

        List<MediaScanner.MediaItem> books = items.stream()
                .filter(item -> !item.isDirectory())
                .sorted(
                        Comparator.comparing(
                                MediaScanner.MediaItem::getTitle,
                                String.CASE_INSENSITIVE_ORDER
                        )
                )
                .toList();

        for (MediaScanner.MediaItem book : books) {
            Button bookButton = new Button("📘  " + book.getTitle());
            bookButton.setMaxWidth(Double.MAX_VALUE);
            bookButton.setAlignment(Pos.CENTER_LEFT);
            bookButton.setOnAction(event ->
                    openContainingFolder(Path.of(book.getFolderPath()))
            );

            bookList.getChildren().add(bookButton);
        }

        if (books.isEmpty() && !items.isEmpty()) {
            Label emptyLabel = new Label(
                    "Abra uma coleção para visualizar os livros."
            );
            emptyLabel.setWrapText(true);
            bookList.getChildren().add(emptyLabel);
        }

        directoryContent.getChildren().setAll(cardGrid, bookList);
    }

    private int countBooks(Path directory) {
        if (directory == null || !Files.isDirectory(directory)) {
            return 0;
        }

        int count = 0;

        List<MediaScanner.MediaItem> items =
                MediaScanner.scanDirectory(directory.toString());

        for (MediaScanner.MediaItem item : items) {
            if (item.isDirectory()) {
                count += countBooks(Path.of(item.getFolderPath()));
            } else if (MediaScanner.isBookFile(
                    Path.of(item.getFolderPath())
            )) {
                count++;
            }
        }

        return count;
    }

    private int countPhotos(Path directory) {
        if (directory == null || !Files.isDirectory(directory)) {
            return 0;
        }

        try (Stream<Path> paths = Files.walk(directory)) {
            return (int) paths
                    .filter(Files::isRegularFile)
                    .filter(MediaScanner::isPhotoFile)
                    .count();
        } catch (IOException | RuntimeException exception) {
            System.err.println(
                    "Erro ao contar fotos em " + directory + ": "
                            + exception.getMessage()
            );
            return 0;
        }
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
            return;
        }

        if ("Livros".equals(selectedCategory)) {
            openContainingFolder(path);
        } else if ("Fotos".equals(selectedCategory)) {
            for (int i = 0; i < currentPhotoItems.size(); i++) {
                Path photoPath = Path.of(
                        currentPhotoItems.get(i).getFolderPath()
                );

                if (photoPath.equals(path)) {
                    openPhotoViewer(i);
                    return;
                }
            }
        } else if ("Vídeos".equals(selectedCategory)) {
            openVideoWithWindows(path);
        } else {
            onPlay.accept(path);
        }
    }

    private void openVideoWithWindows(Path file) {
        if (file == null || !Files.isRegularFile(file)) {
            showMessage("O vídeo não existe mais.");
            return;
        }

        if (!MediaScanner.isVideoFile(file)) {
            showMessage("Este arquivo não é um vídeo compatível.");
            return;
        }

        try {
            if (!Desktop.isDesktopSupported()
                    || !Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                showMessage(
                        "Não foi possível abrir o reprodutor padrão do Windows."
                );
                return;
            }

            Desktop.getDesktop().open(file.toFile());

        } catch (IOException | UnsupportedOperationException exception) {
            showMessage(
                    "Não foi possível abrir o vídeo: "
                            + exception.getMessage()
            );
        }
    }

    private void openContainingFolder(Path file) {
        if (file == null || !Files.isRegularFile(file)) {
            showMessage("O arquivo não existe mais.");
            return;
        }

        Path parent = file.toAbsolutePath().normalize().getParent();

        if (parent == null || !Files.isDirectory(parent)) {
            showMessage("Não foi possível localizar a pasta do arquivo.");
            return;
        }

        try {
            if (!Desktop.isDesktopSupported()
                    || !Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                showMessage(
                        "Não foi possível abrir o Explorador do Windows neste sistema."
                );
                return;
            }

            Desktop.getDesktop().open(parent.toFile());
        } catch (IOException | UnsupportedOperationException exception) {
            showMessage(
                    "Não foi possível abrir a pasta: "
                            + exception.getMessage()
            );
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

        Path categoryRoot = MediaScanner.getMediaDirectory()
                .resolve(selectedCategory)
                .toAbsolutePath()
                .normalize();

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
        Label label = new Label(message);
        label.getStyleClass().add("header-title");
        label.setWrapText(true);
        label.setPadding(new Insets(20));

        directoryContent.getChildren().setAll(label);
        root.setCenter(browserView);
        StyleA.refresh(root);
    }

    private void updateNavigation() {
        boolean canGoBack = !navigationHistory.isEmpty();

        if (currentDirectory != null && selectedCategory != null) {
            Path categoryRoot = MediaScanner.getMediaDirectory()
                    .resolve(selectedCategory)
                    .toAbsolutePath()
                    .normalize();

            canGoBack |= !currentDirectory.equals(categoryRoot);
        }

        backButton.setDisable(!canGoBack);

        if (currentDirectory == null) {
            breadcrumbLabel.setText("Biblioteca");
            return;
        }

        Path mediaRoot = MediaScanner.getMediaDirectory()
                .toAbsolutePath()
                .normalize();

        if (currentDirectory.startsWith(mediaRoot)) {
            breadcrumbLabel.setText(
                    "Biblioteca / "
                            + mediaRoot.relativize(currentDirectory)
                            .toString()
                            .replace('\\', '/')
                            .replace("/", " / ")
            );
        } else {
            breadcrumbLabel.setText(
                    currentDirectory.getFileName().toString()
            );
        }
    }
}
