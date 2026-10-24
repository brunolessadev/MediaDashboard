
package com.war;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public class MediaScanner {

    private static final String[] CATEGORIES = {
            "Animes",
            "Desenhos",
            "Séries",
            "Filmes",
            "Mangás",
            "Livros",
            "Desejos"
    };

    public static class MediaItem {

        private final String title;
        private final String folderPath;
        private final String coverPath;
        private final String favoriteId;

        public MediaItem(
                String title,
                String folderPath,
                String coverPath,
                String favoriteId
        ) {
            this.title = title;
            this.folderPath = folderPath;
            this.coverPath = coverPath;
            this.favoriteId = favoriteId;
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

        public String getFavoriteId() {
            return favoriteId;
        }
    }

    private static Path getApplicationDirectory() {
        try {
            Path location = Path.of(
                    MediaScanner.class.getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI()
            ).toAbsolutePath().normalize();

            if (Files.isRegularFile(location)) {
                return location.getParent();
            }

            Path folderName = location.getFileName();

            if (folderName != null
                    && folderName.toString().equalsIgnoreCase("classes")
                    && location.getParent() != null
                    && location.getParent().getFileName() != null
                    && location.getParent().getFileName()
                    .toString().equalsIgnoreCase("target")) {
                return location.getParent().getParent();
            }

            return location;

        } catch (URISyntaxException | NullPointerException e) {
            return Path.of("").toAbsolutePath().normalize();
        }
    }

    public static Path getMediaDirectory() {
        return getApplicationDirectory().resolve("midia");
    }

    public static void initializeCategories() {
        Path baseDir = getMediaDirectory();

        try {
            if (Files.notExists(baseDir)) {
                Files.createDirectories(baseDir);
            }

            for (String category : CATEGORIES) {
                Path categoryPath = baseDir.resolve(category);

                if (Files.notExists(categoryPath)) {
                    Files.createDirectories(categoryPath);
                }
            }

            MediaConfig.initialize();

        } catch (IOException e) {
            System.err.println(
                    "Erro ao inicializar as pastas de mídia: "
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
                    // Tenta outro leitor disponível.
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
                                    .toLowerCase(Locale.ROOT)
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
        Path baseDir = Path.of(baseDirectoryPath);

        if (!Files.isDirectory(baseDir)) {
            return items;
        }

        String category = baseDir.getFileName().toString();

        try (Stream<Path> paths = Files.list(baseDir)) {
            paths
                    .filter(Files::isDirectory)
                    .sorted(Comparator.comparing(
                            path -> path.getFileName()
                                    .toString()
                                    .toLowerCase(Locale.ROOT)
                    ))
                    .forEach(path -> {
                        String folderName =
                                path.getFileName().toString();

                        String coverPath = findCoverImage(path);
                        String favoriteId = category + "/" + folderName;

                        items.add(new MediaItem(
                                folderName,
                                path.toAbsolutePath().toString(),
                                coverPath,
                                favoriteId
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
