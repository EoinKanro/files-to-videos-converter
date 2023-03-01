package io.github.eoinkanro.filestoimage.conf;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InputCLIArgument<T> {

    private final String shortName;
    private final String fullName;
    private final boolean hasArg;
    private final String description;
    private final T defaultValue;

}
