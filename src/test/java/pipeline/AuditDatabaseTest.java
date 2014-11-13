package pipeline;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration("classpath:/database.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class AuditDatabaseTest {

    @Resource
    private NamedParameterJdbcTemplate dbTemplate;

    @Test
    public void testCreateDropTable() {
        final String table = "test_table";
        String createTable =
                "CREATE TABLE IF NOT EXISTS " + table + " (" +
                "    test_id     char(37)    NOT NULL," +
                "    test_type   char(1)     NOT NULL," +
                "    PRIMARY KEY (test_id));";
        execute(createTable);
        execute("SELECT * FROM " + table + ";");
        execute("DROP TABLE " + table + ";");
    }

    private void execute(String sql) {
        dbTemplate.execute(sql, new PreparedStatementCallback<Boolean>() {
            @Override
            public Boolean doInPreparedStatement(PreparedStatement ps)
                    throws SQLException, DataAccessException {
                return ps.execute();
            }});
    }
}
