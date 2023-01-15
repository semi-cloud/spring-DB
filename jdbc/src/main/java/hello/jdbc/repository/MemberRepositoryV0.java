package hello.jdbc.repository;

import hello.jdbc.connection.DBConnectionUtil;
import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.NoSuchElementException;

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
