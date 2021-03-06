package com.gtp.hunter.rfid.asciiprotocol.parameters;

import com.gtp.hunter.rfid.asciiprotocol.enumerations.QAlgorithm;

//-----------------------------------------------------------------------
//     Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved. 
//
//     Authors: Brian Painter & Robin Stone
//-----------------------------------------------------------------------

/**
 * Provides properties to control the Q algorithm and value
 * <p>
 * {@link QAlgorithmParameters}
 */
public interface IQAlgorithmParameters {
    /**
     * Gets or sets the Q algorithm type
     */
    QAlgorithm getQAlgorithm();

    void setQAlgorithm(QAlgorithm value);

    /**
     * Gets or sets the Q value for fixed Q operations (0-15)
     */
    int getQValue();

    void setQValue(int value);
}