package com.gtp.hunter.rfid.asciiprotocol.enumerations;

import java.util.HashMap;

/*
 * ---------------------------------------------------------------------------
 *
 * @author TSL Code Generator
 * <p>
 * Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved.
 * ----------------------------------------------------------------------------
 */

/**
 * Allows the write mode to be set to single, block or not specified to leave the current value unchanged
 */
public class WriteMode extends EnumerationBase {
    /**
     * The instance of Not Specified
     */
    public static final WriteMode NOT_SPECIFIED = null;

    /**
     * The instance of Single
     */
    public static final WriteMode SINGLE = new WriteMode("s", "The value is specified as Single");

    /**
     * The instance of Block
     */
    public static final WriteMode BLOCK = new WriteMode("b", "The value is specified as Block");

    /**
     * Initializes a new instance of the WriteMode class
     *
     * @param argument The argument as output to the command line
     * @param description A human-readable description of the value
     */
    private WriteMode(String argument, String description) {
        super(argument, description);
        if (parameterLookUp == null) {
            parameterLookUp = new HashMap<>();
        }
        parameterLookUp.put(argument, this);
    }


    public static WriteMode[] getValues() {
        return PRIVATE_VALUES;
    }

    public static final WriteMode[] PRIVATE_VALUES =
            {
                    NOT_SPECIFIED,
                    SINGLE,
                    BLOCK,
            };

    public static WriteMode Parse(String parameter) {
        String trimmedParameter = parameter.trim();
        if (!parameterLookUp.containsKey(trimmedParameter)) {
            String message = String.format("'%s' is not recognised as a value of %s", parameter, WriteMode.class.getName());
            throw new IllegalArgumentException(message);
        }

        return parameterLookUp.get(trimmedParameter);
    }


    private static HashMap<String, WriteMode> parameterLookUp;
}
