package org.sagebionetworks.audit.dao;

import org.sagebionetworks.audit.dao.SynapseDao;
import org.sagebionetworks.audit.http.client.SynapseClient;
import org.sagebionetworks.dashboard.parse.CuPassingRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("synapseDao")
public class SynapseDaoImpl implements SynapseDao {

    @Autowired
    public SynapseDaoImpl(SynapseClient synapseClient) {
        // Get the team of dashboard users
        this.synapseClient = synapseClient;
        final String session = synapseClient.login();
        dashboardTeamId = synapseClient.getTeamId(TEAM_NAME, session);
        if (dashboardTeamId == null) {
            throw new RuntimeException("Cannot find the team for " + TEAM_NAME);
        }
    }

    @Override
    public CuPassingRecord getCuPassingRecord(String userId) {
        String session = getSession();
        return synapseClient.getCuPassingRecord(userId, session);
    }
    private final SynapseClient synapseClient;

    private final Long dashboardTeamId;

    // Synapse team name for the list of dashboard users
    private static final String TEAM_NAME = "SageBioEmployees"; 

    private String getSession() {
        String session = login();
        return session;
    }

    private String login() {
        String session = synapseClient.login();
        if (session != null) {
            return session;
        }
        throw new RuntimeException("Failed to log in to Synapse.");
    }
}
