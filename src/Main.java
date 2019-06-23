import commands.Command;
import commands.FileQuery;
import constraints.FilenameConstraint;
import constraints.SizeConstraint;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Main {
    private final static Option deleteFlag = Option.builder("rm")
            .hasArg(false).longOpt("delete").desc("deletes the selected group of files").build();
    private final static Option minSizeOption = Option.builder("ns")
            .hasArg().longOpt("minSize").desc("sets the min size of selected files in bytes").build();
    private final static Option maxSizeOption = Option.builder("xs")
            .hasArg().longOpt("maxSize").desc("sets the max size of selected files in bytes").build();
    private final static Option exactSizeOption = Option.builder("x")
            .hasArg().longOpt("exactSize").desc("sets the exact size of selected files in bytes").build();
    private final static Option includeExtOption = Option.builder("ie")
            .hasArgs().longOpt("includeExt").desc("list of acceptable file extensions").build();
    private final static Option excludeExtOption = Option.builder("xe")
            .hasArgs().longOpt("excludeExt").desc("list of unacceptable file extensions").build();
    private final static Option matchesOption = Option.builder("m")
            .hasArg().longOpt("matches").desc("string that matches partially accepted filenames").build();
    private final static Option outfileOption = Option.builder("o")
            .hasArg().longOpt("out").desc("direct output to a specified file, folder or `./fm-<timestamp>.log` if not specified").build();
    private final static Option recurseFlag = Option.builder("r")
            .hasArg(false).longOpt("recurse").desc("recursive listing of every file in source").build();
    private final static Option verboseFlag = Option.builder("nl")
            .hasArg(false).longOpt("verbose").desc("turns on logging details").build();
    private final static Option caseInsensitiveFlag = Option.builder("ci")
            .hasArg(false).longOpt("caseInsensitive").desc("forces all string based matching in filters to be case-insensitive").build();
    private final static Option duplicatesFlag = Option.builder("d")
            .hasArg(false).longOpt("duplicates").desc("enables duplicate matching mode (slower than simple filter-based matching)").build();

    public static void main(String[] args) {
        CommandLineParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption(minSizeOption);
        options.addOption(maxSizeOption);
        options.addOption(exactSizeOption);
        options.addOption(includeExtOption);
        options.addOption(excludeExtOption);
        options.addOption(matchesOption);
        options.addOption(outfileOption);
        options.addOption(verboseFlag);
        options.addOption(deleteFlag);
        options.addOption(duplicatesFlag);
        options.addOption(recurseFlag);
        options.addOption(caseInsensitiveFlag);


        try {
            var cli = parser.parse(options, args, false);
            var cliArgs = cli.getArgs();

            // parse source
            if (cliArgs.length == 0) {
                throw new MissingArgumentException("Missing required argument: source directory");
            }
            Path source = Paths.get(cliArgs[0]);

            // set options with args
            var includedExt = cli.getOptionValues("ie");
            var excludedExt = cli.getOptionValues("xe");
            var matches = cli.getOptionValue("m");
            var minSize = Optional.ofNullable(cli.getOptionValue("ns")).map(Long::parseLong).orElse(-1L);
            var maxSize = Optional.ofNullable(cli.getOptionValue("xs")).map(Long::parseLong).orElse(-1L);
            var exactSize = Optional.ofNullable(cli.getOptionValue("x")).map(Long::parseLong).orElse(-1L);
            var outfile = cli.getOptionValue("o");

            // set flags
            var recurse = cli.hasOption("r");
            var verbose = cli.hasOption("nl");
            var caseInsensitive = cli.hasOption("ci");
            var duplicates = cli.hasOption("d");

            Logger logger = verbose ? Logger.getGlobal() : null;
            var nameConstraint = new FilenameConstraint.Builder()
                    .caseInsensitive(caseInsensitive)
                    .includeExtensions(includedExt)
                    .excludeExtensions(excludedExt)
                    .contains(matches)
                    .build();
            var sizeConstraint = new SizeConstraint.Builder()
                    .greaterThan(minSize)
                    .lessThan(maxSize)
                    .exactly(exactSize)
                    .build();
            var fileQuery = new FileQuery(source, recurse, duplicates, nameConstraint, sizeConstraint);
            Command command = Command.valueOf(cli.hasOption("rm") ? "delete" : "list", fileQuery);

            command.execute();

            if (outfile != null) {
                var fileSeparator = System.getProperty("file.separator");
                var dir = System.getProperty("user.dir");
                Path logFilePath;
                if (Files.isDirectory(Paths.get(outfile))) {
                    var filename = "fq-" + DateFormat.getInstance().format(new Date()) + ".log";
                    logger.warning(String.format("using outfile value \"%s\" as a target directory of the log file", outfile));
                    logger.fine(String.format("using filename \"%s\" for the log file", filename));
                    logFilePath = Paths.get(outfile, filename);
                }
                else if (outfile.contains(fileSeparator)) {
                    var lastIndex = outfile.lastIndexOf(fileSeparator);
                    var targetDir = outfile.substring(0, lastIndex);
                    var filename = outfile.substring(lastIndex + 1);
                    logFilePath = Paths.get(targetDir, filename);
                    logger.warning(String.format("using outfile value \"%s\" as a target directory of the log file", outfile));
                    logger.fine(String.format("using filename \"%s\" for the log file", filename));
                    if (Files.deleteIfExists(logFilePath)) {
                        logger.info("deleted file " + logFilePath.toString());
                    }
                }
                else {
                    logFilePath = Paths.get(dir, outfile);
                    command.log(Level.FINE, String.format("using filename \"%s\" for the log file", outfile));
                }
                Files.createFile(logFilePath);
                var fileHandler = new FileHandler(logFilePath.toString());
                fileHandler.setFormatter(new SimpleFormatter());
                logger.addHandler(fileHandler);
            }

        } catch (ParseException | IOException | InvalidPathException e) {
            e.printStackTrace();
        }
    }
}
