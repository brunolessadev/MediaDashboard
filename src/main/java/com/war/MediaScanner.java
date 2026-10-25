
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
import java.util.Set;
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

    private static final Set<String> VIDEO_EXTENSIONS = Set.of(
            "mp4", "m4v", "mkv", "avi", "mov",
            "wmv", "webm", "mpeg", "mpg", "m2ts"
    );

    public static class MediaItem {

        private final String title;
        private final String folderPath;
        private final String coverPath;
        private final String favoriteId;
        private final boolean directory;

        public MediaItem(
                String title,
                String folderPath,
                String coverPath,
                String favoriteId,
                boolean directory
        ) {
            this.title = title;
            this.folderPath = folderPath;
            this.coverPath = coverPath;
            this.favoriteId = favoriteId;
            this.directory = directory;
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

        public boolean isDirectory() {
            return directory;
        }
    }

    public static class VideoProgress {

        private final int watched;
        private final int total;

        public VideoProgress(int watched, int total) {
            this.watched = watched;
            this.total = total;
        }

        public int getWatched() {
            return watched;
        }

        public int getTotal() {
            return total;
        }

        public double getProgress() {
            return total == 0 ? 0.0 : (double) watched / total;
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
            Files.createDirectories(baseDir);

            for (String category : CATEGORIES) {
                Files.createDirectories(baseDir.resolve(category));
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

            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);

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
                    "Erro ao verificar imagem " + file + ": "
                            + e.getMessage()
            );
        }

        return false;
    }

    public static boolean isVideoFile(Path file) {
        if (!Files.isRegularFile(file) || !Files.isReadable(file)) {
            return false;
        }

        String name = file.getFileName().toString();
        int extensionIndex = name.lastIndexOf('.');

        if (extensionIndex < 0 || extensionIndex == name.length() - 1) {
            return false;
        }

        String extension = name.substring(extensionIndex + 1)
                .toLowerCase(Locale.ROOT);

        return VIDEO_EXTENSIONS.contains(extension);
    }

    public static String getMediaId(Path itemPath) {
        Path mediaRoot = getMediaDirectory().toAbsolutePath().normalize();
        Path normalizedItem = itemPath.toAbsolutePath().normalize();

        if (normalizedItem.startsWith(mediaRoot)) {
            return mediaRoot.relativize(normalizedItem)
                    .toString()
                    .replace('\\', '/');
        }

        return normalizedItem.toString().replace('\\', '/');
    }

    public static VideoProgress getVideoProgress(Path path) {
        if (path == null || !Files.exists(path)) {
            return new VideoProgress(0, 0);
        }

        if (Files.isRegularFile(path)) {
            if (!isVideoFile(path)) {
                return new VideoProgress(0, 0);
            }

            boolean watched = MediaConfig.isWatched(getMediaId(path));
            return new VideoProgress(watched ? 1 : 0, 1);
        }

        int total = 0;
        int watched = 0;

        try (Stream<Path> paths = Files.walk(path)) {
            Iterator<Path> iterator = paths.iterator();

            while (iterator.hasNext()) {
                Path candidate = iterator.next();

                if (isVideoFile(candidate)) {
                    total++;

                    if (MediaConfig.isWatched(getMediaId(candidate))) {
                        watched++;
                    }
                }
            }

        } catch (IOException | RuntimeException e) {
            System.err.println(
                    "Erro ao contar vídeos em " + path + ": "
                            + e.getMessage()
            );
        }

        return new VideoProgress(watched, total);
    }

    private static String findCoverImage(Path folder) {
        if (folder == null || !Files.isDirectory(folder)) {
            return null;
        }

        try (Stream<Path> files = Files.list(folder)) {
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
                    "Erro ao procurar capa em " + folder + ": "
                            + e.getMessage()
            );
            return null;
        }
    }

    private static String getCategory(Path baseDir) {
        Path mediaRoot = getMediaDirectory().toAbsolutePath().normalize();
        Path normalizedBase = baseDir.toAbsolutePath().normalize();

        if (normalizedBase.startsWith(mediaRoot)
                && normalizedBase.getNameCount() > mediaRoot.getNameCount()) {
            return normalizedBase.getName(mediaRoot.getNameCount()).toString();
        }

        Path name = baseDir.getFileName();
        return name == null ? "" : name.toString();
    }

    private static String createFavoriteId(
            Path baseDir,
            Path itemPath,
            String category
    ) {
        Path mediaRoot = getMediaDirectory().toAbsolutePath().normalize();
        Path normalizedItem = itemPath.toAbsolutePath().normalize();

        if (normalizedItem.startsWith(mediaRoot)) {
            return mediaRoot.relativize(normalizedItem)
                    .toString()
                    .replace('\\', '/');
        }

        return category + "/" + baseDir.relativize(itemPath)
                .toString()
                .replace('\\', '/');
    }

    public static List<MediaItem> scanDirectory(String baseDirectoryPath) {
        List<MediaItem> items = new ArrayList<>();

        Path baseDir = Path.of(baseDirectoryPath)
                .toAbsolutePath()
                .normalize();

        if (!Files.isDirectory(baseDir)) {
            return items;
        }

        String category = getCategory(baseDir);

        try (Stream<Path> paths = Files.list(baseDir)) {
            paths
                    .filter(path ->
                            Files.isDirectory(path) || isVideoFile(path))
                    .sorted(Comparator
                            .comparing(
                                    (Path path) -> !Files.isDirectory(path)
                            )
                            .thenComparing(
                                    path -> path.getFileName()
                                            .toString()
                                            .toLowerCase(Locale.ROOT)
                            ))
                    .forEach(path -> {
                        boolean directory = Files.isDirectory(path);

                        String title = path.getFileName().toString();
                        Path coverFolder = directory ? path : path.getParent();
                        String coverPath = findCoverImage(coverFolder);

                        String favoriteId = createFavoriteId(
                                baseDir,
                                path,
                                category
                        );

                        items.add(new MediaItem(
                                title,
                                path.toAbsolutePath().toString(),
                                coverPath,
                                favoriteId,
                                directory
                        ));
                    });

        } catch (IOException e) {
            System.err.println(
                    "Erro ao varrer o diretório " + baseDir + ": "
                            + e.getMessage()
            );
        }

        return items;
    }
}
