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
 * Specifies the target for the specified session
 */
public class QueryTarget extends EnumerationBase {
    /**
     * The instance of Not Specified
     */
    public static final QueryTarget NOT_SPECIFIED = null;

    /**
     * The instance of Target A
     */
    public static final QueryTarget TARGET_A = new QueryTarget("a", "Select target A");

    /**
     * The instance of Target B
     */
    public static final QueryTarget TARGET_B = new QueryTarget("b", "Select target B");

    /**
     * Initializes a new instance of the QueryTarget class
     *
     * @param argument The argument as output to the command line
     * @param description A human-readable description of the value
     */
    private QueryTarget(String argument, String description) {
        super(argument, description);
        if (parameterLookUp == null) {
            parameterLookUp = new HashMap<>();
        }
        parameterLookUp.put(argument, this);
    }


    public static QueryTarget[] getValues() {
        return PRIVATE_VALUES;
    }

    public static final QueryTarget[] PRIVATE_VALUES =
            {
                    NOT_SPECIFIED,
                    TARGET_A,
                    TARGET_B,
            };

    public static QueryTarget Parse(String parameter) {
        String trimmedParameter = parameter.trim();
        if (!parameterLookUp.containsKey(trimmedParameter)) {
            String message = String.format("'%s' is not recognised as a value of %s", parameter, QueryTarget.class.getName());
            throw new IllegalArgumentException(message);
        }

        return parameterLookUp.get(trimmedParameter);
    }


    private static HashMap<String, QueryTarget> parameterLookUp;
}
