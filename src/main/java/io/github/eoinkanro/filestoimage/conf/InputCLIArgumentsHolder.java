package io.github.eoinkanro.filestoimage.conf;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.cli.*;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

import static io.github.eoinkanro.filestoimage.conf.InputCLIArguments.HELP;

@Component
@Log4j2
public class InputCLIArgumentsHolder {

    private CommandLine cmd;

    public boolean init(String... args) throws Exception {
        Options options = new Options();
        for (Field field : InputCLIArguments.class.getDeclaredFields()) {
            InputCLIArgument<?> inputCLIArgument = (InputCLIArgument<?>) field.get(InputCLIArguments.class);
            options.addOption(inputCLIArgument.getShortName(),
                    inputCLIArgument.getFullName(),
                    inputCLIArgument.isHasArg(),
                    inputCLIArgument.getDescription());
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
    public <T> T getArgument(InputCLIArgument<T> inputCLIArgument) {
        T result;
        if (inputCLIArgument.getDefaultValue() instanceof Boolean) {
            result = cmd.hasOption(inputCLIArgument.getShortName()) ? (T) Boolean.TRUE : (T) Boolean.FALSE;
        } else if (inputCLIArgument.getDefaultValue() instanceof String) {
            result = (T) cmd.getOptionValue(inputCLIArgument.getShortName());
        } else {
            result = (T) Integer.getInteger(cmd.getOptionValue(inputCLIArgument.getShortName()));
        }
        return result == null ? inputCLIArgument.getDefaultValue() : result;
    }

}
