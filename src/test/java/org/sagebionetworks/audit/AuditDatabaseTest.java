package org.sagebionetworks.audit;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration("classpath:/database.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class AuditDatabaseTest {

    @Resource
    private NamedParameterJdbcTemplate dbTemplate;

    @Before
    public void before() {
        execute("DROP TABLE IF EXISTS " + getClass().getSimpleName() + ";");
    }

    @After
    public void after() {
        execute("DROP TABLE IF EXISTS " + getClass().getSimpleName() + ";");
    }

    @Test
    public void test() throws IOException {

        // Create table
        final String table = getClass().getSimpleName();
        String createTable =
                "CREATE TABLE IF NOT EXISTS " + table + " (" +
                "    test_id     char(37)        NOT NULL," +
                "    test_text   varchar(255)    NOT NULL," +
                "    test_blob   mediumblob,              " +
                "    PRIMARY KEY (test_id));              ";
        execute(createTable);

        // Insert plain text
        final String testText = "test test text for testing test text";
        String insertInto = "INSERT INTO " + table + "(test_id, test_text)" +
                " VALUES (1, '" + testText + "');";
        execute(insertInto);

        // Read the text
        String selectText = "SELECT test_text FROM " + table + " WHERE test_id = 1;";
        String text = dbTemplate.query(selectText, new ResultSetExtractor<String>() {
            @Override
            public String extractData(ResultSet rs) throws SQLException, DataAccessException {
                if (rs.next()) {
                    return rs.getString("test_text");
                } else {
                    return null;
                }
            }
        });

        // Gzip the text and write it back as a blob
        String update = "UPDATE " + table + " SET test_blob = :test_blob WHERE test_id = 1";
        Map<String, byte[]> namedParameters = new HashMap<String, byte[]>();
        namedParameters.put("test_blob", GzipUtils.compress(text));
        dbTemplate.update(update, namedParameters);

        // Read the blob and unzip it
        String selectBlob = "SELECT test_blob FROM " + table + " WHERE test_id = 1;";
        byte[] blob = dbTemplate.query(selectBlob, new ResultSetExtractor<byte[]>() {
            @Override
            public byte[] extractData(ResultSet rs) throws SQLException, DataAccessException {
                if (rs.next()) {
                    return rs.getBytes("test_blob");
                } else {
                    return null;
                }
            }
        });
        assertEquals(testText, GzipUtils.decompress(blob));
    }

    private Boolean execute(String sql) {
        return dbTemplate.execute(sql, new PreparedStatementCallback<Boolean>() {
            @Override
            public Boolean doInPreparedStatement(PreparedStatement ps)
                    throws SQLException, DataAccessException {
                return ps.execute();
            }});
    }
}
