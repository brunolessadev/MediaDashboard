
package com.war;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public final class StyleB {

    private StyleB() {
    }

    public static void apply(Node node, StyleA.Theme theme) {
        if (node == null || theme == null) {
            return;
        }

        if (node instanceof Region region) {
            styleRegion(region, theme);
        }

        if (node instanceof Label label) {
            styleLabel(label, theme);
        }

        if (node instanceof Button button) {
            styleButton(button, theme);
        }

        if (node instanceof TextField textField) {
            styleTextField(textField, theme);
        }

        if (node instanceof ComboBox<?> comboBox) {
            styleComboBox(comboBox, theme);
        }

        if (node instanceof ProgressBar progressBar) {
            progressBar.setPrefHeight(6);
        }

        if (node instanceof ScrollPane scrollPane) {
            scrollPane.setBackground(Background.EMPTY);
            scrollPane.setBorder(Border.EMPTY);
        }

        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                apply(child, theme);
            }
        }
    }

    private static void styleRegion(Region region, StyleA.Theme theme) {
        if (region.getParent() == null) {
            setBackground(region, theme.getBackground(), 0);
        }

        if (hasClass(region, "header")
                || hasClass(region, "grid")
                || hasClass(region, "details")
                || hasClass(region, "detail-container")
                || hasClass(region, "breadcrumb")) {
            setBackground(region, theme.getBackground(), 0);
        }

        if (hasClass(region, "tabs")) {
            setBackground(region, theme.getSurface(), 12);
            setBorder(region, theme.getBorder(), 12);
        }

        if (hasClass(region, "card")
                || hasClass(region, "media-card")) {
            setBackground(region, theme.getSurface(), 12);
            setBorder(region, theme.getBorder(), 12);
        }

        if (hasClass(region, "cover-container")) {
            setBackground(region, theme.getSurface(), 4);
        }

        if (hasClass(region, "card-info")) {
            setBackground(region, Color.TRANSPARENT, 0);
        }
    }

    private static void styleLabel(Label label, StyleA.Theme theme) {
        if (hasClass(label, "header-title")) {
            label.setTextFill(theme.getText());
            label.setFont(Font.font(
                    "Segoe UI",
                    FontWeight.BOLD,
                    24
            ));
            return;
        }

        if (hasClass(label, "title")
                || hasClass(label, "card-title")
                || hasClass(label, "media-title")
                || hasClass(label, "detail-title")) {
            label.setTextFill(theme.getText());
            label.setFont(Font.font(
                    "Segoe UI",
                    FontWeight.BOLD,
                    hasClass(label, "detail-title") ? 26 : 15
            ));
            return;
        }

        if (hasClass(label, "meta")
                || hasClass(label, "card-meta")
                || hasClass(label, "media-meta")
                || hasClass(label, "breadcrumb")
                || hasClass(label, "detail-description")
                || hasClass(label, "detail-label")) {
            label.setTextFill(theme.getSecondaryText());
            return;
        }

        label.setTextFill(theme.getText());
    }


    private static void styleButton(Button button, StyleA.Theme theme) {
        Color backgroundColor = theme.getSurface();
        Color textColor = theme.getText();
        double radius = 8;

        if (hasClass(button, "tab")) {
            boolean active = hasClass(button, "active");

            backgroundColor = active ? theme.getAccent() : theme.getSurface();
            textColor = active
                    ? contrastColor(theme.getAccent())
                    : theme.getSecondaryText();
            radius = 8;

        } else if ("reload-button".equals(button.getId())) {
            backgroundColor = theme.getSurface();
            textColor = theme.getAccent();
            radius = 10;

        } else if (hasClass(button, "btn-org")) {
            backgroundColor = theme.getAccent();
            textColor = contrastColor(theme.getAccent());

        } else if (hasClass(button, "episode")
                || hasClass(button, "episode-button")) {
            backgroundColor = theme.getSurface();
            textColor = theme.getText();

        } else if (hasClass(button, "breadcrumb")) {
            button.setBackground(Background.EMPTY);
            button.setBorder(Border.EMPTY);
            button.setTextFill(theme.getAccent());
            return;
        }

        button.setStyle(null);
        button.setBackground(new Background(
                new BackgroundFill(
                        backgroundColor,
                        new CornerRadii(radius),
                        Insets.EMPTY
                )
        ));

        button.setBorder(new Border(
                new BorderStroke(
                        theme.getBorder(),
                        BorderStrokeStyle.SOLID,
                        new CornerRadii(radius),
                        new BorderWidths(1)
                )
        ));

        button.setTextFill(textColor);
    }


    private static void styleTextField(
            TextField textField,
            StyleA.Theme theme
    ) {
        setBackground(textField, theme.getSurface(), 10);
        setBorder(textField, theme.getBorder(), 10);
        textField.setFont(Font.font("Segoe UI", 14));
    }


    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void styleComboBox(
            ComboBox<?> comboBox,
            StyleA.Theme theme
    ) {
        setBackground(comboBox, theme.getSurface(), 10);
        setBorder(comboBox, theme.getBorder(), 10);

        ComboBox rawComboBox = comboBox;

        rawComboBox.setCellFactory(listView -> new ListCell<Object>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);

                setText(empty || item == null ? null : item.toString());
                setTextFill(theme.getText());

                setBackground(new Background(
                        new BackgroundFill(
                                theme.getSurface(),
                                CornerRadii.EMPTY,
                                Insets.EMPTY
                        )
                ));
            }
        });

        rawComboBox.setButtonCell(new ListCell<Object>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);

                setText(empty || item == null ? null : item.toString());
                setTextFill(theme.getText());
                setBackground(Background.EMPTY);
            }
        });
    }


    private static void setBackground(
            Region region,
            Color color,
            double radius
    ) {
        region.setBackground(new Background(
                new BackgroundFill(
                        color,
                        new CornerRadii(radius),
                        Insets.EMPTY
                )
        ));
    }

    private static void setBorder(
            Region region,
            Color color,
            double radius
    ) {
        region.setBorder(new Border(
                new BorderStroke(
                        color,
                        BorderStrokeStyle.SOLID,
                        new CornerRadii(radius),
                        new BorderWidths(1)
                )
        ));
    }

    private static Color contrastColor(Color color) {
        double luminance =
                0.2126 * color.getRed()
                        + 0.7152 * color.getGreen()
                        + 0.0722 * color.getBlue();

        return luminance > 0.55
                ? Color.web("#0b0813")
                : Color.WHITE;
    }

    private static boolean hasClass(Node node, String className) {
        return node.getStyleClass().contains(className);
    }
}
