package org.sagebionetworks.audit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.sagebionetworks.audit.dao.SynapseDao;
import org.sagebionetworks.dashboard.parse.CuPassingRecord;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

@Component("certifiedUser")
public class CertifiedUser {

    private String GET_ACTIVE_USERS = "SELECT USER_ID FROM AUDIT_ACTIVE_USERS;";
    private String ADD_COLUMN = "ALTER TABLE AUDIT_ACTIVE_USERS ADD COLUMN CERTIFIED boolean;";
    private String UPDATE = "UPDATE AUDIT_ACTIVE_USERS SET CERTIFIED=TRUE WHERE USER_ID=:id;";

    @Resource
    private NamedParameterJdbcTemplate dbTemplate;

    @Resource
    private SynapseDao synapseDao;

    public void doWork() {
        @SuppressWarnings("unused")
        int rowAffected = 0; // for debug

        /* Step 1: Add a new column */
        try {
            rowAffected = dbTemplate.update(ADD_COLUMN, new HashMap<String, Object>());
        } catch (Throwable e){
            System.out.println("Failed to add column.");
            e.printStackTrace();
        }

        /* Step 2: Get all active users */
        List<Long> idList = new ArrayList<Long>();
        try {
             idList = dbTemplate.queryForList(GET_ACTIVE_USERS, new HashMap<String, Object>(), Long.class);
        } catch (Throwable e){
            System.out.println("Failed to get list of IDs.");
            e.printStackTrace();
        }

        /* Step 3: Update the certified status for each user */
        if (idList != null) {
            for (Long id : idList) {
                if (id != null) {
                    CuPassingRecord passingRecord = synapseDao.getCuPassingRecord(id.toString());
                    if (passingRecord != null && passingRecord.isPassed()) {
                        Map<String, Long> parameter = new HashMap<>();
                        parameter.put("id", id);
                        try {
                            rowAffected = dbTemplate.update(UPDATE, parameter);
                        } catch (Throwable e) {
                            System.out.println("Failed to update " + id);
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
