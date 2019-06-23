package commands;

import constraints.FilenameConstraint;
import constraints.SizeConstraint;
import utils.Checksums;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileQuery {
    private final Path source;
    private final boolean recurse, duplicates;
    private FilenameConstraint name;
    private SizeConstraint size;
    private Map<String, List<File>> duplicatesResults;

    public FileQuery(Path source, boolean recurse, boolean duplicates, FilenameConstraint name, SizeConstraint size) {
        this.source = source;
        this.recurse = recurse;
        this.duplicates = duplicates;
        this.name = name;
        this.size = size;
    }

    public Stream<File> getStream() throws IOException {
        return Files.walk(source, recurse ? Integer.MAX_VALUE : 1)
            .map(Path::toFile)
            .filter(File::isFile)
            .filter(f -> name == null || name.satisfyConstraint(f))
            .filter(f -> size == null || size.satisfyConstraint(f));
    }

    public boolean isDuplicatesModeActive() {
        return duplicates;
    }

    private void calculateDuplicates() throws IOException {
        duplicatesResults = getStream().collect(Collectors.groupingBy(File::length))
                .entrySet().parallelStream()
                .filter(entry -> entry.getValue().size() > 1)
                .flatMap(entry -> entry.getValue().stream()
                        .collect(Collectors.groupingBy(file -> Checksums.calculateMD5(file.getAbsolutePath())))
                        .entrySet().stream()
                )
                .filter(entry -> entry.getValue().size() > 1)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Map<String, List<File>> getDuplicates() throws IOException {
        if (duplicatesResults == null) {
            calculateDuplicates();
        }
        return Collections.unmodifiableMap(duplicatesResults);
    }
}
