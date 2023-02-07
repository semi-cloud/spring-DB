package hello.springs.propagation;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.UnexpectedRollbackException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
@SpringBootTest
class MemberServiceTest {

    @Autowired
    MemberService memberService;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    LogRepository logRepository;

    /**
     * 1.분리 트랜잭션 : 정상 커밋
     * MemberService    @Transactional:OFF
     * MemberRepository @Transactional:ON
     * LogRepository    @Transactional:ON
     */
    @Test
    void outer_tx_off_success() {
        String username = "outer_tx_off_success";
        memberService.joinV1(username);
        Assertions.assertTrue(memberRepository.find(username).isPresent());
        Assertions.assertTrue(logRepository.find(username).isPresent());
    }

    /**
     * 2.분리 트랜잭션 : 롤백
     * => 회원은 저장되지만 회원 이력 로그는 로백되므로 데이터 정합성에 문제가 발생해서 하나로 묶어야함
     * MemberService    @Transactional:OFF
     * MemberRepository @Transactional:ON
     * LogRepository    @Transactional:ON
     */
    @Test
    void outer_tx_off_fail() {
        String username = "로그예외_outer_tx_off_fail";
        assertThatThrownBy(() -> memberService.joinV1(username))
                        .isInstanceOf(RuntimeException.class);

        Assertions.assertTrue(memberRepository.find(username).isPresent());
        Assertions.assertTrue(logRepository.find(username).isEmpty());   // 로그는 단독적으로 롤백
    }

    /**
     * 3.시작부터 종료까지 모든 로직을 단일 트랜잭션으로 묶기
     * => 레파지토리만 호출하고 싶은 경우, 서비스에서 또 다른 서비스를 호출하는 등 여러 복잡한 상황이 발생하므로 효율적이지 X
     * => 따라서 트랜잭션 전파의 원칙이 필요하게 됌
     * MemberService    @Transactional:ON
     * MemberRepository @Transactional:OFF
     * LogRepository    @Transactional:OFF
     */
    @Test
    void single_Tx() {
        String username = "outer_tx_off_success";
        memberService.joinV1(username);
        Assertions.assertTrue(memberRepository.find(username).isPresent());
        Assertions.assertTrue(logRepository.find(username).isPresent());
    }

    /**
     * 4.트랜잭션 전파 : 커밋
     * MemberService    @Transactional:ON
     * MemberRepository @Transactional:ON
     * LogRepository    @Transactional:ON
     */
    @Test
    void outer_tx_on_success() {
        String username = "outer_tx_on_success";
        memberService.joinV1(username);
        Assertions.assertTrue(memberRepository.find(username).isPresent());
        Assertions.assertTrue(logRepository.find(username).isPresent());
    }


    /**
     * 5.트랜잭션 전파 : 롤백
     * => 외부 트랜잭션(물리)이 롤백되면서 모든 데이터가 롤백(단, rollbackOnly에 상관 없이 예외 발생했으므로 롤백)
     * MemberService    @Transactional:ON
     * MemberRepository @Transactional:ON
     * LogRepository    @Transactional:ON
     */
    @Test
    void outer_tx_on_fail() {
        String username = "로그예외_outer_tx_on_fail";
        assertThatThrownBy(() -> memberService.joinV1(username))  // 예외를 잡지 않고 계속 던졌으니 런타임 예외 발생
                .isInstanceOf(RuntimeException.class);

        Assertions.assertTrue(memberRepository.find(username).isEmpty());
        Assertions.assertTrue(logRepository.find(username).isEmpty());
    }

    /**
     * 6.트랜잭션 예외 복구 : 실패
     * => 요구 사항 추가 : 회원 가입 시도 로그는 실패하더라도 가입은 완료되어야 함
     * => 멤버 로직은 정상 커밋, 로그 로직에서 올라온 예외는 서비스에서 잡아서 정상흐름으로 반환했으니 멤버는 그대로 정상 커밋될 것처럼 기대
     * => 하지만 정상 흐름이니 외부 트랜잭션에서 커밋을 호출하고, 커밋 시점에 rollbackOnly 옵션을 체크하는데 이때 롤백을 수행해서 회원 데이터 X
     * => 이렇게 내부 트랜잭션 롤백 + 외부 트랜잭션 커밋의 경우 트랜잭션 매니저는 UnexpectedRollbackException 예외를 던짐
     * MemberService    @Transactional:ON
     * MemberRepository @Transactional:ON
     * LogRepository    @Transactional:ON
     */
    @Test
    void recover_exception_fail() {
        String username = "로그예외_recover_exception_fail";
        assertThatThrownBy(() -> memberService.joinV2(username))
                .isInstanceOf(UnexpectedRollbackException.class);

        Assertions.assertTrue(memberRepository.find(username).isEmpty());
        Assertions.assertTrue(logRepository.find(username).isEmpty());
    }

    /**
     * 7.트랜잭션 예외 복구 : 성공
     * => REQUIRES_NEW 는 항상 신규 트랜잭션(커넥션)을 생성해서 분리되므로, 로그 로직에서 예외가 터져도 rollbackOnly 옵션 설정 되지 X
     * => 서비스에서는 그대로 던져진 런타임 예외를 잡고 정상 흐름이므로 실제 물리 트랜잭션 커밋이 일어남
     * => Member 정상 저장, Log 예외로 인해 롤백
     * MemberService    @Transactional:ON
     * MemberRepository @Transactional:ON
     * LogRepository    @Transactional:ON(REQUIRES_NEW) Exception
     */
    @Test
    void recover_exception_success() {
        String username = "로그예외_recover_exception_success";
        memberService.joinV2(username);
        Assertions.assertTrue(memberRepository.find(username).isPresent());
        Assertions.assertTrue(logRepository.find(username).isEmpty());
    }
}