package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 트랜잭션 - 파라미터 연동, 풀을 고려한 종료
 * 단점 : 서비스 계층의 코드가 지저분해지고 복잡함 + 커넥션을 인자로 넘기지 않는 함수도 존재
 */
@RequiredArgsConstructor
@Slf4j
public class MemberServiceV2 {

    private final DataSource dataSource;
    private final MemberRepositoryV2 memberRepository;

    public void accountTransfer(String fromId, String toId, int money) throws SQLException {   // 계좌 이체
        // 1. 커넥션 생성
        Connection con = dataSource.getConnection();
        try {
            con.setAutoCommit(false);  // 트랜잭션 시작
            // 2. 비즈니스 로직 수행
            bizLogic(fromId, toId, money, con);
            // 3. 로직 성공 시 커밋
            con.commit();
        } catch (Exception ex) {
            // 4. 로직 실패 시 롤백
            con.rollback();
            throw new IllegalStateException(ex);
        } finally {
            // 5. 사용한 커넥션 릴리즈
            releaseConnection(con);
        }
    }

    private void bizLogic(String fromId, String toId, int money, Connection con) throws SQLException {
        Member fromMember = memberRepository.findById(con, fromId);
        Member toMember = memberRepository.findById(con, toId);

        memberRepository.update(con, fromId, fromMember.getMoney() - money);
        validation(toMember);
        memberRepository.update(con, toId, toMember.getMoney() + money);
    }

    private void releaseConnection(Connection con) {
        if (con != null) {
            try {
                con.setAutoCommit(true);  // 주의 : 기본이 자동 커밋 모드이므로 커넥션 풀에 반납할때는 true로 변경
                con.close();
            } catch (Exception ex) {
                log.info("error", ex);
            }
        }
    }

    private void validation(Member toMember) {
        if (toMember.getMemberId().equals("ex")) {
            throw new IllegalStateException("이체중 예외 발생");
        }
    }
}
