
        package com.war;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

public class WindowBar {

    private final Stage primaryStage;
    private final BorderPane root;
    private final Runnable reloadAction;

    private final BorderPane view;
    private final HBox tabs;

    private double dragOffsetX;
    private double dragOffsetY;

    public WindowBar(
            Stage primaryStage,
            BorderPane root,
            Runnable reloadAction
    ) {
        this.primaryStage = primaryStage;
        this.root = root;
        this.reloadAction = reloadAction;

        this.tabs = new HBox(4);
        this.view = createWindowBar();
    }

    public BorderPane getView() {
        return view;
    }

    public HBox getTabs() {
        return tabs;
    }

    private BorderPane createWindowBar() {
        BorderPane bar = new BorderPane();

        bar.getStyleClass().add("header");
        bar.setPadding(new Insets(8, 16, 8, 16));

        Label windowTitle = new Label("MediaDashboard");
        windowTitle.getStyleClass().add("header-title");

        ComboBox<String> themeSelector = new ComboBox<>();

        themeSelector.getItems().addAll(
                "Cyberpunk Neon",
                "Crunchyroll",
                "Nordico"
        );

        StyleA.Theme savedTheme =
                StyleA.Theme.fromName(MediaConfig.getTheme());

        themeSelector.setValue(savedTheme.getDisplayName());
        themeSelector.getStyleClass().add("theme-selector");

        themeSelector.setOnAction(e -> {
            String selectedTheme = themeSelector.getValue();

            if (selectedTheme == null) {
                return;
            }

            StyleA.Theme theme =
                    StyleA.Theme.fromName(selectedTheme);

            StyleA.applyTheme(root, theme);
            MediaConfig.setTheme(theme.name());
        });

        HBox leftControls = new HBox(
                12,
                windowTitle,
                themeSelector
        );
        leftControls.setAlignment(Pos.CENTER_LEFT);

        tabs.getStyleClass().add("tabs");
        tabs.setAlignment(Pos.CENTER);

        tabs.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-border-color: transparent;" +
                        "-fx-border-width: 0;" +
                        "-fx-background-insets: 0;" +
                        "-fx-border-insets: 0;" +
                        "-fx-padding: 0;"
        );

        TextField searchField = new TextField();
        searchField.setPromptText("Pesquisar...");
        searchField.setId("search");
        searchField.setPrefWidth(180);
        searchField.setMinWidth(120);

        Button reloadButton = new Button("⟳ Reload");
        reloadButton.setId("reload-button");
        reloadButton.setTooltip(
                new Tooltip("Recarregar categorias e mídias")
        );
        reloadButton.setOnAction(e -> reloadAction.run());

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
                primaryStage.setMaximized(
                        !primaryStage.isMaximized()
                )
        );

        Button closeButton = new Button("✕");
        closeButton.setTooltip(new Tooltip("Fechar"));
        closeButton.getStyleClass().addAll(
                "window-button",
                "window-close"
        );
        closeButton.setOnAction(e -> primaryStage.close());

        Button[] windowButtons = {
                minimizeButton,
                maximizeButton,
                closeButton
        };

        for (Button button : windowButtons) {
            button.setMinSize(32, 28);
            button.setPrefSize(32, 28);
            button.setMaxSize(32, 28);
            button.setAlignment(Pos.CENTER);
        }

        HBox windowControls = new HBox(
                6,
                minimizeButton,
                maximizeButton,
                closeButton
        );
        windowControls.setAlignment(Pos.CENTER_RIGHT);

        HBox rightControls = new HBox(
                12,
                searchField,
                reloadButton,
                windowControls
        );
        rightControls.setAlignment(Pos.CENTER_RIGHT);

        Region leftSpacer = new Region();
        Region rightSpacer = new Region();

        HBox.setHgrow(leftSpacer, Priority.ALWAYS);
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);

        HBox leftArea = new HBox(leftControls, leftSpacer);
        leftArea.setAlignment(Pos.CENTER_LEFT);
        leftArea.setMinWidth(0);

        HBox rightArea = new HBox(rightSpacer, rightControls);
        rightArea.setAlignment(Pos.CENTER_RIGHT);
        rightArea.setMinWidth(0);

        bar.setLeft(leftArea);
        bar.setCenter(tabs);
        bar.setRight(rightArea);

        BorderPane.setAlignment(tabs, Pos.CENTER);
        BorderPane.setMargin(tabs, new Insets(0, 20, 0, 20));

        bar.setOnMousePressed((MouseEvent e) -> {
            if (e.getTarget() instanceof Button
                    || e.getTarget() instanceof ComboBox<?>
                    || e.getTarget() instanceof TextField) {
                dragOffsetX = Double.NaN;
                dragOffsetY = Double.NaN;
                return;
            }

            dragOffsetX = e.getScreenX() - primaryStage.getX();
            dragOffsetY = e.getScreenY() - primaryStage.getY();
        });

        bar.setOnMouseDragged((MouseEvent e) -> {
            if (!primaryStage.isMaximized()
                    && e.isPrimaryButtonDown()
                    && !Double.isNaN(dragOffsetX)
                    && !Double.isNaN(dragOffsetY)) {

                primaryStage.setX(
                        e.getScreenX() - dragOffsetX
                );

                primaryStage.setY(
                        e.getScreenY() - dragOffsetY
                );
            }
        });

        return bar;
    }
}