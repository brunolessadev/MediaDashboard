
package com.war;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.paint.Color;

public final class StyleA {

    private StyleA() {
    }

    public enum Theme {

        CYBERPUNK(
                "Cyberpunk Neon",
                "#0b0813",
                "#140f26",
                "#2c1e4d",
                "#00f2fe",
                "#e0dafa",
                "#a99acb",
                "#8375a3",
                "#302942"
        ),

        CRUNCHYROLL(
                "Crunchyroll",
                "#141519",
                "#202126",
                "#38393f",
                "#f47521",
                "#ffffff",
                "#c5c5c8",
                "#99999f",
                "#303136"
        ),

        NORDICO(
                "Nordico",
                "#1a1c23",
                "#252933",
                "#414754",
                "#88c0d0",
                "#eceff4",
                "#c3cad8",
                "#aab4c5",
                "#343a46"
        );

        private final String displayName;
        private final Color background;
        private final Color surface;
        private final Color border;
        private final Color accent;
        private final Color text;
        private final Color secondaryText;
        private final Color mutedText;
        private final Color progressTrack;

        Theme(
                String displayName,
                String background,
                String surface,
                String border,
                String accent,
                String text,
                String secondaryText,
                String mutedText,
                String progressTrack
        ) {
            this.displayName = displayName;
            this.background = Color.web(background);
            this.surface = Color.web(surface);
            this.border = Color.web(border);
            this.accent = Color.web(accent);
            this.text = Color.web(text);
            this.secondaryText = Color.web(secondaryText);
            this.mutedText = Color.web(mutedText);
            this.progressTrack = Color.web(progressTrack);
        }

        public String getDisplayName() {
            return displayName;
        }

        public Color getBackground() {
            return background;
        }

        public Color getSurface() {
            return surface;
        }

        public Color getBorder() {
            return border;
        }

        public Color getAccent() {
            return accent;
        }

        public Color getText() {
            return text;
        }

        public Color getSecondaryText() {
            return secondaryText;
        }

        public Color getMutedText() {
            return mutedText;
        }

        public Color getProgressTrack() {
            return progressTrack;
        }

        public static Theme fromName(String name) {
            if (name != null) {
                for (Theme theme : values()) {
                    if (theme.displayName.equalsIgnoreCase(name)) {
                        return theme;
                    }
                }
            }

            return CYBERPUNK;
        }
    }

    private static Theme currentTheme = Theme.CYBERPUNK;

    public static Theme getCurrentTheme() {
        return currentTheme;
    }

    public static void applyTheme(Parent root, String themeName) {
        applyTheme(root, Theme.fromName(themeName));
    }

    public static void applyTheme(Parent root, Theme theme) {
        if (root == null || theme == null) {
            return;
        }

        currentTheme = theme;

        root.getStyleClass().removeAll(
                "theme-cyberpunk",
                "theme-crunchyroll",
                "theme-nordico"
        );

        switch (theme) {
            case CYBERPUNK ->
                    root.getStyleClass().add("theme-cyberpunk");
            case CRUNCHYROLL ->
                    root.getStyleClass().add("theme-crunchyroll");
            case NORDICO ->
                    root.getStyleClass().add("theme-nordico");
        }

        StyleB.apply(root, theme);
    }

    public static void refresh(Node node) {
        if (node == null) {
            return;
        }

        StyleB.apply(node, currentTheme);
    }
}
