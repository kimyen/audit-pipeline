package org.sagebionetworks.audit;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class Worker {

    //private static String TABLE_NAME = "TEST"; // for testing
    private static String TABLE_NAME = "ACCESS_REQUIREMENT";
    private static String ADD_COLUMN = "ALTER TABLE " + TABLE_NAME + " ADD COLUMN CONCRETE_TYPE VARCHAR(256);";
    private static String GET_ALL_ID = "SELECT ID FROM " + TABLE_NAME + ";";
    private static String GET_A_BLOB = "SELECT SERIALIZED_ENTITY FROM " + TABLE_NAME + " WHERE ID=:id;";
    private static String UPDATE = "UPDATE " + TABLE_NAME + " SET CONCRETE_TYPE=:concreteType WHERE ID=:id;";

    final Logger logger = org.slf4j.LoggerFactory.getLogger(Worker.class);

    public Worker(NamedParameterJdbcTemplate dbTemplate) {
        doWork(dbTemplate);
    }

    public void doWork(NamedParameterJdbcTemplate dbTemplate) {

        @SuppressWarnings("unused")
        int rowAffected = 0; // for debug

        /* Step 1: Add new column */
        try {
            rowAffected = dbTemplate.update(ADD_COLUMN, new HashMap<String, Object>());
        } catch (Throwable e){
            System.out.println("Failed to add column.");
            e.printStackTrace();
        }

        /* Step 2: Get all ids */
        List<Long> idList = new ArrayList<Long>();
        try {
             idList = dbTemplate.queryForList(GET_ALL_ID, new HashMap<String, Object>(), Long.class);
        } catch (Throwable e){
            System.out.println("Failed to get list of IDs.");
            e.printStackTrace();
        }

        if (idList != null) {
            for (Long id : idList) {

                Map<String, Object> parameters = new HashMap<>();
                parameters.put("id", id);
                /* Step 3: Get the Blob */
                byte[] blob = dbTemplate.query(GET_A_BLOB, parameters, new ResultSetExtractor<byte[]>() {
                    @Override
                    public byte[] extractData(ResultSet rs) throws SQLException, DataAccessException {
                        if (rs.next()) {
                            return rs.getBytes("SERIALIZED_ENTITY");
                        } else {
                            return null;
                        }
                    }
                });

                /* Step 4: decompress the blob and update it*/
                try {
                    String text = GzipUtils.decompress(blob);
                    String concreteType = getConcreteType(text);
                    parameters.put("concreteType", concreteType);
                    try {
                        rowAffected = dbTemplate.update(UPDATE, parameters);
                    } catch (Throwable e) {
                        System.out.println("Failed to update " + id + " : " + concreteType);
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    System.out.println("Failed to decompress a blob.");
                    e.printStackTrace();
                }
            }
        }
    }

    private String getConcreteType(String text) {
        Pattern pattern = Pattern.compile("<concreteType>(.*?)</concreteType>");
        Matcher matcher = pattern.matcher(text);
        String concreteType = "";
        if (matcher.find()) {
            concreteType = matcher.group(1);
        }
        return concreteType;
    }

}
