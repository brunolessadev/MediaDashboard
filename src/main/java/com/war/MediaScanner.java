
package com.war;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class MediaScanner {

    public static class MediaItem {
        private final String title;
        private final String folderPath;
        private final String coverPath;

        public MediaItem(String title, String folderPath, String coverPath) {
            this.title = title;
            this.folderPath = folderPath;
            this.coverPath = coverPath;
        }

        public String getTitle() {
            return title;
        }

        public String getFolderPath() {
            return folderPath;
        }

        public String getCoverPath() {
            return coverPath;
        }
    }

    public static void initializeCategories() {
        String[] categories = {
                "Animes",
                "Desenhos",
                "Séries",
                "Filmes",
                "Mangás",
                "Livros",
                "Desejos"
        };

        Path baseDir = Paths.get("midia");

        try {
            Files.createDirectories(baseDir);

            for (String category : categories) {
                Files.createDirectories(baseDir.resolve(category));
            }
        } catch (IOException e) {
            System.err.println(
                    "Erro ao criar as pastas de categorias: "
                            + e.getMessage()
            );
        }
    }

    private static boolean isImageFile(Path file) {
        if (!Files.isRegularFile(file) || !Files.isReadable(file)) {
            return false;
        }

        try (ImageInputStream input =
                     ImageIO.createImageInputStream(file.toFile())) {

            if (input == null) {
                return false;
            }

            Iterator<ImageReader> readers =
                    ImageIO.getImageReaders(input);

            while (readers.hasNext()) {
                ImageReader reader = readers.next();

                try {
                    reader.setInput(input, true, true);

                    return reader.getWidth(0) > 0
                            && reader.getHeight(0) > 0;

                } catch (IOException | RuntimeException e) {
                    // Tenta outro leitor disponível para o arquivo.
                } finally {
                    reader.dispose();
                }
            }

        } catch (IOException | RuntimeException e) {
            System.err.println(
                    "Não foi possível verificar o arquivo "
                            + file.getFileName()
                            + ": "
                            + e.getMessage()
            );
        }

        return false;
    }

    private static String findCoverImage(Path mediaFolder) {
        try (Stream<Path> files = Files.list(mediaFolder)) {
            return files
                    .filter(Files::isRegularFile)
                    .sorted(Comparator.comparing(
                            path -> path.getFileName()
                                    .toString()
                                    .toLowerCase()
                    ))
                    .filter(MediaScanner::isImageFile)
                    .map(path -> path.toAbsolutePath().toString())
                    .findFirst()
                    .orElse(null);

        } catch (IOException e) {
            System.err.println(
                    "Erro ao procurar capa na pasta "
                            + mediaFolder
                            + ": "
                            + e.getMessage()
            );
            return null;
        }
    }

    public static List<MediaItem> scanDirectory(
            String baseDirectoryPath
    ) {
        List<MediaItem> items = new ArrayList<>();
        Path baseDir = Paths.get(baseDirectoryPath);

        if (!Files.isDirectory(baseDir)) {
            return items;
        }

        try (Stream<Path> paths = Files.list(baseDir)) {
            paths
                    .filter(Files::isDirectory)
                    .sorted(Comparator.comparing(
                            path -> path.getFileName()
                                    .toString()
                                    .toLowerCase()
                    ))
                    .forEach(path -> {
                        String folderName =
                                path.getFileName().toString();

                        String coverPath = findCoverImage(path);

                        items.add(new MediaItem(
                                folderName,
                                path.toAbsolutePath().toString(),
                                coverPath
                        ));
                    });

        } catch (IOException e) {
            System.err.println(
                    "Erro ao varrer o diretório de mídias: "
                            + e.getMessage()
            );
        }

        return items;
    }
}
