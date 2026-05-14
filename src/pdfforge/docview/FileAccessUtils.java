package pdfforge.docview;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public final class FileAccessUtils {

    private FileAccessUtils() {
    }

    public static InputStream openForRead(File file) throws IOException {
        Objects.requireNonNull(file, "file");
        Path path = file.toPath();
        return Files.newInputStream(path);
    }
}
