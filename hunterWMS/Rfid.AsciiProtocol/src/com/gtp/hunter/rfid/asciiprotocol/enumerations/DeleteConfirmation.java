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
 * Allows a file action to be set to delete or not specified to leave the current value unchanged
 */
public class DeleteConfirmation extends EnumerationBase {
    /**
     * The instance of Not Specified
     */
    public static final DeleteConfirmation NOT_SPECIFIED = null;

    /**
     * The instance of Yes
     */
    public static final DeleteConfirmation YES = new DeleteConfirmation("yes", "The value is specified yes");
    public static final DeleteConfirmation[] PRIVATE_VALUES =
            {
                    NOT_SPECIFIED,
                    YES,
            };
    private static HashMap<String, DeleteConfirmation> parameterLookUp;

    /**
     * Initializes a new instance of the DeleteConfirmation class
     *
     * @param argument    The argument as output to the command line
     * @param description A human-readable description of the value
     */
    private DeleteConfirmation(String argument, String description) {
        super(argument, description);
        if (parameterLookUp == null) {
            parameterLookUp = new HashMap<>();
        }
        parameterLookUp.put(argument, this);
    }

    public static DeleteConfirmation[] getValues() {
        return PRIVATE_VALUES;
    }

    public static DeleteConfirmation Parse(String parameter) {
        String trimmedParameter = parameter.trim();
        if (!parameterLookUp.containsKey(trimmedParameter)) {
            String message = String.format("'%s' is not recognised as a value of %s", parameter, DeleteConfirmation.class.getName());
            throw new IllegalArgumentException(message);
        }

        return parameterLookUp.get(trimmedParameter);
    }
}
