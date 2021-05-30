package com.gtp.hunter.rfid.asciiprotocol.parameters;

import com.gtp.hunter.rfid.asciiprotocol.enumerations.QuerySelect;
import com.gtp.hunter.rfid.asciiprotocol.enumerations.QuerySession;
import com.gtp.hunter.rfid.asciiprotocol.enumerations.QueryTarget;

//-----------------------------------------------------------------------
//     Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved. 
//
//     Authors: Brian Painter & Robin Stone
//-----------------------------------------------------------------------

/**
 * Specifies properties to select transponders into distinct groups
 * <p>
 * {@link QueryParameters}
 */
public interface IQueryParameters {
    /**
     * Gets or sets the transponders to include based on the select flag state
     */
    QuerySelect getQuerySelect();

    void setQuerySelect(QuerySelect value);

    /**
     * Gets or sets the transponders to include based on the select flag state
     */
    QuerySession getQuerySession();

    void setQuerySession(QuerySession value);

    /**
     * Gets or sets the session state of the transponders to be included in this operation
     */
    QueryTarget getQueryTarget();

    void setQueryTarget(QueryTarget value);
}