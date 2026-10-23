package com.war;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class CardComponent {

    public static VBox create(String titleText, String progressText, double progressValue, String coverPath) {
        VBox card = new VBox();
        card.getStyleClass().add("card");
        card.setPrefSize(600, 1200);
        card.setMaxSize(1000, 1200);

        // Container superior para a imagem de capa com ajuste automático estilo 'cover'
        Pane coverContainer = new Pane();
        coverContainer.setPrefSize(350, 1000);
        coverContainer.setMaxSize(100, 1000);

        if (coverPath != null && !coverPath.isEmpty()) {
            // Aplica a imagem como fundo com 'cover' e centralizada, evitando distorções/esticamentos
            coverContainer.setStyle(
                    "-fx-background-image: url('" + coverPath + "'); " +
                            "-fx-background-position: center; " +
                            "-fx-background-repeat: no-repeat; " +
                            "-fx-background-size: cover; " +
                            "-fx-background-radius: 14px 14px 0 0;"
            );
        } else {
            coverContainer.setStyle("-fx-background-color: #1a1530; -fx-background-radius: 14px 14px 0 0;");
        }

        // Wrapper para permitir sobrepor o botão de marcação por cima da imagem
        StackPane coverWrapper = new StackPane(coverContainer);
        coverWrapper.setPrefSize(200, 200);

        // Botão de marcação (✓) no canto superior direito da capa
        Button btnOrg = new Button("✓");
        btnOrg.getStyleClass().add("btn-org");
        StackPane.setAlignment(btnOrg, Pos.TOP_RIGHT);
        StackPane.setMargin(btnOrg, new Insets(10, 10, 0, 0));

        coverWrapper.getChildren().add(btnOrg);

        // Título da mídia
        Label title = new Label(titleText);
        title.getStyleClass().add("title");
        title.setWrapText(true);

        // Seção de informações e barra de progresso inferior
        VBox infoBox = new VBox(4);
        infoBox.getStyleClass().add("info");
        infoBox.setPadding(new Insets(6, 10, 10, 10));

        Label meta = new Label(progressText);
        meta.getStyleClass().add("meta");

        VBox progressBarBg = new VBox();
        progressBarBg.getStyleClass().add("progress");

        VBox progressBarFill = new VBox();
        progressBarFill.getStyleClass().add("bar");
        progressBarFill.prefWidthProperty().bind(progressBarBg.widthProperty().multiply(progressValue));

        progressBarBg.getChildren().add(progressBarFill);
        infoBox.getChildren().addAll(meta, progressBarBg);

        card.getChildren().addAll(coverWrapper, title, infoBox);
        return card;
    }
}