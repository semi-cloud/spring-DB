package hello.springs.propagation;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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
}