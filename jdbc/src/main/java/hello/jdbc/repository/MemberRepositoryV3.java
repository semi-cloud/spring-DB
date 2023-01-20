package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

/**
 * 트랜잭션 : 트랜잭션 메니저
 * DataSourceUtils.getConnection()
 * DataSourceUtils.releaseConnection()
 */
@Slf4j
public class MemberRepositoryV3 {

    private final DataSource dataSource;

    public MemberRepositoryV3(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Member save(Member member) throws SQLException {
        String sql = "insert into member(member_id, money) values(?, ?)";

        Connection con = null;
        PreparedStatement pState = null;

        try {
            con = getConnection();
            pState = con.prepareStatement(sql);
            pState.setString(1, member.getMemberId());
            pState.setInt(2, member.getMoney());
            pState.executeUpdate();  // 데이터 변경
            return member;
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            close(con, pState, null);   // 모든 자원의 반납을 보장
        }
    }

    public Member findById(String memberId) throws SQLException {
        String sql = "select * from member where member_id = ?";

        Connection con = null;
        PreparedStatement pState = null;
        ResultSet rs = null;

        try {
            con = getConnection();
            pState = con.prepareStatement(sql);
            pState.setString(1, memberId);
            rs = pState.executeQuery();// 데이터 조회

            if (rs.next()) {
                Member member = new Member();
                member.setMemberId(rs.getString("member_id"));
                member.setMoney(rs.getInt("money"));
                return member;
            } else {  // 데이터가 존재하지 않는다면
                throw new NoSuchElementException("member not found from memberId = " + memberId);
            }

        } catch (SQLException ex) {
            log.error("db error", ex);
            throw ex;
        } finally {
            close(con, pState, rs);
        }
    }

    public void update(String memberId, int money) throws SQLException {
        String sql = "update member set money=? where member_id=?";

        Connection con = null;
        PreparedStatement pState = null;

        try {
            con = getConnection();
            pState = con.prepareStatement(sql);
            pState.setInt(1, money);
            pState.setString(2, memberId);
            int resultSize = pState.executeUpdate();  // 쿼리 실행 후 영향받은 row 개수 반환
            log.info("resultSize={}", resultSize);
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            close(con, pState, null);
        }
    }

    public void delete(String memberId) throws SQLException {
        String sql = "delete from member where member_id=?";

        Connection con = null;
        PreparedStatement pState = null;

        try {
            con = getConnection();
            pState = con.prepareStatement(sql);
            pState.setString(1, memberId);
            int resultSize = pState.executeUpdate();  // 쿼리 실행 후 영향받은 row 개수 반환
            log.info("resultSize={}", resultSize);
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            close(con, pState, null);
        }
    }

    private void close(Connection con, Statement stmt, ResultSet rs) {
        JdbcUtils.closeResultSet(rs);
        JdbcUtils.closeStatement(stmt);
        // 주의! 트랜잭션 동기화를 사용하려면 DataSourceUtils를 사용해야 함
        DataSourceUtils.releaseConnection(con, dataSource);
    }

    private Connection getConnection() throws SQLException {
        // 주의! 트랜잭션 동기화를 사용하려면 DataSourceUtils를 사용해야 함
        Connection connection = DataSourceUtils.getConnection(dataSource);// 트랜잭션 동기화 매니저릁 통해 보관된 커넥션 반환
        log.info("get connection={} class={}", connection, connection.getClass());
        return connection;
    }
}
