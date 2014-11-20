package org.sagebionetworks.audit.dao;

import org.sagebionetworks.dashboard.parse.CuPassingRecord;

/**
 * Caches data retrieved directly from Synapse.
 */
public interface SynapseDao {

    /** Gets the PassingRecord of a user's Certified Quiz */
    CuPassingRecord getCuPassingRecord(String userId);

}
