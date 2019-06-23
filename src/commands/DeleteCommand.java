package commands;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;

public class DeleteCommand extends Command {

    DeleteCommand(@NotNull FileQuery query) {
        super(query);
    }

    @Override
    public void execute() throws IOException {
        fileQuery.getStream().forEach(f -> {
            try {
                Files.delete(f.toPath());
                logFileRecord(f);
            } catch (IOException e) {
                if (logger == null) {
                    System.err.println(e.getMessage());
                } else {
                    logger.severe(e.getMessage());
                }
            }
        });
    }
}
