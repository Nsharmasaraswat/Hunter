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
 * Specifies the current state of the switch
 */
public class SwitchState extends EnumerationBase {
    /**
     * The instance of Not Specified
     */
    public static final SwitchState NOT_SPECIFIED = null;

    /**
     * The instance of Off
     */
    public static final SwitchState OFF = new SwitchState("off", "Switch is OFF / not pressed");

    /**
     * The instance of Single
     */
    public static final SwitchState SINGLE = new SwitchState("single", "Switch is ON in single press");

    /**
     * The instance of Double
     */
    public static final SwitchState DOUBLE = new SwitchState("double", "Switch is ON in double press");
    public static final SwitchState[] PRIVATE_VALUES =
            {
                    NOT_SPECIFIED,
                    OFF,
                    SINGLE,
                    DOUBLE,
            };
    private static HashMap<String, SwitchState> parameterLookUp;

    /**
     * Initializes a new instance of the SwitchState class
     *
     * @param argument    The argument as output to the command line
     * @param description A human-readable description of the value
     */
    private SwitchState(String argument, String description) {
        super(argument, description);
        if (parameterLookUp == null) {
            parameterLookUp = new HashMap<>();
        }
        parameterLookUp.put(argument, this);
    }

    public static SwitchState[] getValues() {
        return PRIVATE_VALUES;
    }

    public static SwitchState Parse(String parameter) {
        String trimmedParameter = parameter.trim();
        if (!parameterLookUp.containsKey(trimmedParameter)) {
            String message = String.format("'%s' is not recognised as a value of %s", parameter, SwitchState.class.getName());
            throw new IllegalArgumentException(message);
        }

        return parameterLookUp.get(trimmedParameter);
    }
}
