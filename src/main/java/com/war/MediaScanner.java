package com.war;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class MediaScanner {

    public static class MediaItem {
        private String title;
        private String folderPath;
        private String coverPath;

        public MediaItem(String title, String folderPath, String coverPath) {
            this.title = title;
            this.folderPath = folderPath;
            this.coverPath = coverPath;
        }

        public String getTitle() { return title; }
        public String getFolderPath() { return folderPath; }
        public String getCoverPath() { return coverPath; }
    }

    /**
     * Cria automaticamente a pasta 'midia' e as subpastas de categorias padrão se não existirem.
     */
    public static void initializeCategories() {
        String[] categories = {"Animes", "Séries", "Filmes", "Desenhos", "Mangás", "Livros"};
        Path baseDir = Paths.get("midia");

        try {
            if (!Files.exists(baseDir)) {
                Files.createDirectories(baseDir);
            }

            for (String category : categories) {
                Path categoryDir = baseDir.resolve(category);
                if (!Files.exists(categoryDir)) {
                    Files.createDirectories(categoryDir);
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao criar as pastas de categorias: " + e.getMessage());
        }
    }

    /**
     * Procura por um ficheiro de imagem na pasta da mídia com as extensões suportadas.
     */
    private static String findCoverImage(Path mediaFolder) {
        String[] allowedExtensions = {".jpg", ".jpeg", ".png", ".bmp", ".gif", ".tif", ".tiff"};

        try (Stream<Path> files = Files.list(mediaFolder)) {
            java.util.Optional<Path> imagePath = files.filter(Files::isRegularFile)
                    .filter(p -> {
                        String name = p.getFileName().toString().toLowerCase();
                        for (String ext : allowedExtensions) {
                            if (name.endsWith(ext)) return true;
                        }
                        return false;
                    })
                    .findFirst();

            if (imagePath.isPresent()) {
                return imagePath.get().toUri().toString();
            }
        } catch (IOException e) {
            System.err.println("Erro ao procurar capa na pasta: " + e.getMessage());
        }
        return null; // Retorna null se nenhuma imagem for encontrada
    }

    /**
     * Varre um diretório local e retorna todas as subpastas encontradas com respetivas capas.
     */
    public static List<MediaItem> scanDirectory(String baseDirectoryPath) {
        List<MediaItem> items = new ArrayList<>();
        Path baseDir = Paths.get(baseDirectoryPath);

        if (!Files.exists(baseDir) || !Files.isDirectory(baseDir)) {
            return items;
        }

        try (Stream<Path> paths = Files.list(baseDir)) {
            paths.filter(Files::isDirectory).forEach(path -> {
                String folderName = path.getFileName().toString();
                String coverUri = findCoverImage(path);
                items.add(new MediaItem(folderName, path.toAbsolutePath().toString(), coverUri));
            });
        } catch (IOException e) {
            System.err.println("Erro ao varrer o diretório de mídias: " + e.getMessage());
        }

        return items;
    }
}