package commands;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.logging.Level;

import static utils.Formatters.formatInterval;

public class ListCommand extends Command {

    ListCommand(@NotNull FileQuery query) {
        super(query);
    }

    @Override
    public void execute() {
        var start = System.nanoTime();
        try {
            if (fileQuery.isDuplicatesModeActive()) {
                fileQuery.getDuplicates().entrySet().stream()
                    .forEach(this::logDuplicateRecord);
            } else {
                fileQuery.getStream().forEach(this::logFileRecord);
            }
        } catch (IOException e) {
            log(Level.SEVERE, e.getMessage());
        } finally {
            var elapsedTime = (System.nanoTime() - start) / 1000000;
            log(Level.INFO, "elapsed time: " + formatInterval(elapsedTime));
        }
    }
}
