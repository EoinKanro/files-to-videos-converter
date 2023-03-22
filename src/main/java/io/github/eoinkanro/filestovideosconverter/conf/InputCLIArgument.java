package io.github.eoinkanro.filestovideosconverter.conf;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@RequiredArgsConstructor
public class InputCLIArgument<T> {

    private final String shortName;
    private final String fullName;
    private final boolean hasArg;
    private final String description;
    private final T defaultValue;

    @Setter(value = AccessLevel.PROTECTED)
    private T assignedValue;

}
