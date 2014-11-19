package org.sagebionetworks.audit;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class App {

    private static String TABLE_NAME = "TEST"; // for testing
    //private static STring TABLE_NAME = "ACCESS_REQUIREMENT";
    private static String ADD_COLUMN = "ALTER TABLE " + TABLE_NAME + " ADD COLUMN CONCRETE_TYPE VARCHAR(256);";
    private static String GET_ALL_ID = "SELECT ID FROM " + TABLE_NAME + ";";
    private static String GET_A_BLOB = "SELECT SERIALIZED_ENTITY FROM " + TABLE_NAME + " WHERE ID=:id;";
    private static String UPDATE = "UPDATE " + TABLE_NAME + " SET CONCRETE_TYPE=:concreteType WHERE ID=:id;";

    @Resource
    private static NamedParameterJdbcTemplate dbTemplate;

    public static void main(String[] args) {
        final ConfigurableApplicationContext context = new ClassPathXmlApplicationContext("/database.xml");
        context.registerShutdownHook();

        final Logger logger = org.slf4j.LoggerFactory.getLogger(App.class);
        int rowAffected = 0;

        /* Step 1: Add new column */
        try {
            rowAffected = dbTemplate.update(ADD_COLUMN, new HashMap<String, Object>());
        /*} catch (DataAccessException e) {
            logger.error("Failed to add column.", e);
        }*/
        } catch (Throwable e){
            logger.error("Failed to add column.", e);
        }

        assert(rowAffected != 0);

        /* Step 2: Get all ids */
        List<Long> idList = dbTemplate.queryForList(GET_ALL_ID, new HashMap<String, Object>(), Long.class);
        if (idList != null) {
            for (Long id : idList) {

                Map<String, Long> parameter = new HashMap<>();
                parameter.put("id", id);
                /* Step 3: Get the Blob */
                byte[] blob = dbTemplate.query(GET_A_BLOB, parameter, new ResultSetExtractor<byte[]>() {
                    @Override
                    public byte[] extractData(ResultSet rs) throws SQLException, DataAccessException {
                        if (rs.next()) {
                            return rs.getBytes("SERIALIZED_ENTITY");
                        } else {
                            return null;
                        }
                    }
                });

                /* Step 4: decompress the blob and print it*/
                try {
                    String text = GzipUtils.decompress(blob);
                    System.out.println(text);
                } catch (IOException e) {
                    logger.error("Failed to decompress a blob.", e);
                }
            }
        }
        context.close();
    }

}
