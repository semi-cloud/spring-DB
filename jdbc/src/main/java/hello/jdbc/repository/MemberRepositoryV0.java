package hello.jdbc.repository;

import hello.jdbc.connection.DBConnectionUtil;
import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;

/**
 * JDBC : DriverManager 사용
 */
@Slf4j
public class MemberRepositoryV0 {

    public Member save(Member member) throws SQLException {
        String sql = "insert into member(member_id, money) values(?, ?)";

        Connection con = null;
        PreparedStatement pState = null;

        try {
            con = getConnection();
            pState = con.prepareStatement(sql);
            pState.setString(1, member.getMemberId());
            pState.setInt(2, member.getMoney());
            pState.executeUpdate();
            return member;
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            close(con, pState, null);   // 모든 자원의 반납을 보장
        }
    }

    private void close(Connection c, Statement s, ResultSet r) {
        // 리소스 정리는 생성의 역순으로 진행
        if (s != null) {
            try {
                s.close();      // 앞쪽에서 예외가 발생해도 뒤쪽 코드에 영향을 주지 않아야함
            } catch (SQLException e) {
                log.error("error", e);
            }
        }

        if (c != null) {
            try {
                c.close();
            } catch (SQLException e) {
                log.error("error", e);
            }
        }

        if (r != null) {
            try {
                r.close();
            } catch (SQLException e) {
                log.error("error", e);
            }
        }
    }

    private Connection getConnection() {
        return DBConnectionUtil.getConnection();
    }
}
