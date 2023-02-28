package io.github.eoinkanro.filestoimage.conf;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.cli.*;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

import static io.github.eoinkanro.filestoimage.conf.CommandLineArguments.HELP;

@Component
@Log4j2
public class CommandLineArgumentsHolder {

    private CommandLine cmd;

    public boolean init(String... args) throws Exception {
        Options options = new Options();
        for (Field field : CommandLineArguments.class.getDeclaredFields()) {
            CommandLineArgument<?> commandLineArgument = (CommandLineArgument<?>) field.get(CommandLineArguments.class);
            options.addOption(commandLineArgument.getShortName(),
                    commandLineArgument.getFullName(),
                    commandLineArgument.isHasArg(),
                    commandLineArgument.getDescription());
        }

        if (args.length == 0) {
            printHelp(options);
            return false;
        }

        CommandLineParser parser = new DefaultParser();
        cmd = parser.parse(options, args);

        if (log.isDebugEnabled()) {
            log.debug(cmd.getArgList());
        }
        if (cmd.hasOption(HELP.getShortName())) {
            printHelp(options);
        }
        return true;
    }

    private void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("Files to Images Transformer", options);
    }

    @SuppressWarnings("unchecked")
    public <T> T getArgument(CommandLineArgument<T> commandLineArgument) {
        T result;
        if (commandLineArgument.getDefaultValue() instanceof Boolean) {
            result = cmd.hasOption(commandLineArgument.getShortName()) ? (T) Boolean.TRUE : (T) Boolean.FALSE;
        } else if (commandLineArgument.getDefaultValue() instanceof String) {
            result = (T) cmd.getOptionValue(commandLineArgument.getShortName());
        } else {
            result = (T) Integer.getInteger(cmd.getOptionValue(commandLineArgument.getShortName()));
        }
        return result == null ? commandLineArgument.getDefaultValue() : result;
    }

}
