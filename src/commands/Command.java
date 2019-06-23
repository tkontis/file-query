package commands;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Command {
    protected final FileQuery fileQuery;
    protected Logger logger = null;

    Command(@NotNull FileQuery fileQuery) {
        this.fileQuery = fileQuery;
    }

    void setLogger(Logger logger) {
        this.logger = logger;
    }

    private void log(String record) {
        log(Level.FINE, record);
    }

    public void log(Level level, String record) {
        if (logger == null) {
            System.out.println(record);
        } else {
            logger.log(level, record);
        }
    }

    public void logFileRecord(File file) {
        String record = String.format("%-30s %-15d", file.getName(), file.length());
        log(Level.FINER, record);
    }

    void logDuplicateRecord(Map.Entry<String, List<File>> entry) {
        var checksum = entry.getKey();
        List<File> sortedFiles = new ArrayList<>(entry.getValue());
        sortedFiles.sort(Comparator.comparingLong(File::lastModified));
        log(Level.FINE, String.format("checksum: %s", checksum));
        for (File f: sortedFiles) {
            var lastModified = DateFormat.getInstance().format(f.lastModified());
            log(String.format("%-30s last modified: %s", f.getAbsolutePath(), lastModified));
        }
    }

    public static Command valueOf(String commandName, FileQuery query) {
        switch (commandName) {
            case "list":
                return new ListCommand(query);
            case "delete":
                return new DeleteCommand(query);
            default:
                throw new IllegalArgumentException("invalid command name: " + commandName);
        }
    }

    public abstract void execute() throws IOException;
}
