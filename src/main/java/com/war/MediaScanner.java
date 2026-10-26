
package com.war;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class MediaScanner {

    private static final String[] CATEGORIES = {
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

    private static final Set<String> VIDEO_EXTENSIONS = Set.of(
            "mp4", "m4v", "mkv", "avi", "mov",
            "wmv", "webm", "mpeg", "mpg", "m2ts"
    );

    private static final Set<String> BOOK_EXTENSIONS = Set.of(
            "epub", "pdf", "mobi", "azw", "azw3"
    );

    private static final Set<String> IMAGE_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "bmp", "webp"
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
                Path parent = location.getParent();

                if (parent != null
                        && parent.getFileName() != null
                        && parent.getFileName().toString()
                        .equalsIgnoreCase("app")) {
                    Path appImageDirectory = parent.getParent();

                    if (appImageDirectory != null) {
                        return appImageDirectory;
                    }
                }

                return parent != null
                        ? parent
                        : Path.of("").toAbsolutePath().normalize();
            }

            Path folderName = location.getFileName();

            if (folderName != null
                    && folderName.toString().equalsIgnoreCase("classes")
                    && location.getParent() != null
                    && location.getParent().getFileName() != null
                    && location.getParent().getFileName().toString()
                    .equalsIgnoreCase("target")) {
                return location.getParent().getParent();
            }

            return location;

        } catch (URISyntaxException | NullPointerException exception) {
            System.err.println(
                    "Não foi possível identificar a pasta do aplicativo: "
                            + exception.getMessage()
            );

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

        } catch (IOException exception) {
            System.err.println(
                    "Erro ao inicializar as pastas de mídia: "
                            + exception.getMessage()
            );
        }
    }

    private static boolean isImageFile(Path file) {
        if (file == null
                || !Files.isRegularFile(file)
                || !Files.isReadable(file)) {
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
                } catch (IOException | RuntimeException exception) {
                    // Tenta outro leitor disponível.
                } finally {
                    reader.dispose();
                }
            }

        } catch (IOException | RuntimeException exception) {
            System.err.println(
                    "Erro ao verificar imagem " + file + ": "
                            + exception.getMessage()
            );
        }

        return false;
    }

    private static String getExtension(Path file) {
        if (file == null || file.getFileName() == null) {
            return "";
        }

        String name = file.getFileName().toString();
        int extensionIndex = name.lastIndexOf('.');

        if (extensionIndex < 0 || extensionIndex == name.length() - 1) {
            return "";
        }

        return name.substring(extensionIndex + 1)
                .toLowerCase(Locale.ROOT);
    }

    public static boolean isVideoFile(Path file) {
        return file != null
                && Files.isRegularFile(file)
                && Files.isReadable(file)
                && VIDEO_EXTENSIONS.contains(getExtension(file));
    }

    public static boolean isBookFile(Path file) {
        return file != null
                && Files.isRegularFile(file)
                && Files.isReadable(file)
                && BOOK_EXTENSIONS.contains(getExtension(file));
    }

    public static boolean isPhotoFile(Path file) {
        return file != null
                && Files.isRegularFile(file)
                && Files.isReadable(file)
                && IMAGE_EXTENSIONS.contains(getExtension(file));
    }

    private static boolean isSupportedFile(Path file, String category) {
        if ("Livros".equalsIgnoreCase(category)) {
            return isBookFile(file);
        }

        if ("Fotos".equalsIgnoreCase(category)) {
            return isPhotoFile(file);
        }

        if ("Vídeos".equalsIgnoreCase(category)) {
            return isVideoFile(file);
        }

        return isVideoFile(file);
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

        } catch (IOException | RuntimeException exception) {
            System.err.println(
                    "Erro ao contar vídeos em " + path + ": "
                            + exception.getMessage()
            );
        }

        return new VideoProgress(watched, total);
    }

    /**
     * Retorna o caminho da miniatura em cache.
     * O caminho muda quando o tamanho ou a data de modificação do vídeo muda.
     */
    public static Path getVideoThumbnailPath(Path video) {
        if (video == null || !Files.isRegularFile(video)) {
            return null;
        }

        try {
            Path cacheDirectory = getApplicationDirectory()
                    .resolve("cache")
                    .resolve("video-thumbnails");

            Files.createDirectories(cacheDirectory);

            String identity = video.toAbsolutePath().normalize()
                    + "|" + Files.size(video)
                    + "|" + Files.getLastModifiedTime(video).toMillis();

            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(
                            identity.getBytes(
                                    java.nio.charset.StandardCharsets.UTF_8
                            )
                    );

            StringBuilder name = new StringBuilder();

            for (byte value : hash) {
                name.append(String.format("%02x", value));
            }

            return cacheDirectory.resolve(name + ".jpg");

        } catch (IOException | NoSuchAlgorithmException
                 | RuntimeException exception) {
            System.err.println(
                    "Erro ao preparar miniatura: "
                            + exception.getMessage()
            );
            return null;
        }
    }

    /**
     * Gera uma miniatura JPEG a partir de um quadro do vídeo.
     *
     * Procura o FFmpeg na pasta tools/ffmpeg/bin, na pasta do aplicativo
     * ou no PATH do sistema. Deve ser executado em uma thread de fundo.
     *
     * Retorna null se não for possível gerar a miniatura.
     */
    public static Path generateVideoThumbnail(Path video) {
        if (!isVideoFile(video)) {
            return null;
        }

        Path thumbnail = getVideoThumbnailPath(video);

        if (thumbnail == null) {
            return null;
        }

        try {
            if (Files.isRegularFile(thumbnail)
                    && Files.size(thumbnail) > 0) {
                return thumbnail;
            }
        } catch (IOException exception) {
            System.err.println(
                    "Erro ao verificar miniatura: "
                            + exception.getMessage()
            );
        }

        Path temporaryFile = thumbnail.resolveSibling(
                thumbnail.getFileName() + ".tmp.jpg"
        );

        Path bundledFfmpeg = getApplicationDirectory()
                .resolve("tools")
                .resolve("ffmpeg")
                .resolve("bin")
                .resolve("ffmpeg.exe");

        Path rootFfmpeg = getApplicationDirectory()
                .resolve("ffmpeg.exe");

        String executable;

        if (Files.isRegularFile(bundledFfmpeg)) {
            executable = bundledFfmpeg.toString();
        } else if (Files.isRegularFile(rootFfmpeg)) {
            executable = rootFfmpeg.toString();
        } else {
            executable = "ffmpeg";
        }

        try {
            Files.deleteIfExists(temporaryFile);

            Process process = new ProcessBuilder(
                    executable,
                    "-hide_banner",
                    "-loglevel", "error",
                    "-ss", "00:00:03",
                    "-i", video.toAbsolutePath().toString(),
                    "-frames:v", "1",
                    "-vf", "scale=480:-2",
                    "-y",
                    temporaryFile.toString()
            )
                    .redirectErrorStream(true)
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .start();

            boolean finished = process.waitFor(25, TimeUnit.SECONDS);

            if (!finished) {
                process.destroyForcibly();
                process.waitFor(2, TimeUnit.SECONDS);
                return null;
            }

            if (process.exitValue() != 0
                    || !Files.isRegularFile(temporaryFile)
                    || Files.size(temporaryFile) == 0) {
                return null;
            }

            Files.move(
                    temporaryFile,
                    thumbnail,
                    StandardCopyOption.REPLACE_EXISTING
            );

            return thumbnail;

        } catch (IOException exception) {
            System.err.println(
                    "Não foi possível gerar miniatura para "
                            + video + ": " + exception.getMessage()
            );
            return null;

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return null;

        } finally {
            try {
                Files.deleteIfExists(temporaryFile);
            } catch (IOException ignored) {
            }
        }
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

        } catch (IOException exception) {
            System.err.println(
                    "Erro ao procurar capa em " + folder + ": "
                            + exception.getMessage()
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
                            Files.isDirectory(path)
                                    || isSupportedFile(path, category))
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

        } catch (IOException exception) {
            System.err.println(
                    "Erro ao varrer o diretório " + baseDir + ": "
                            + exception.getMessage()
            );
        }

        return items;
    }
}
