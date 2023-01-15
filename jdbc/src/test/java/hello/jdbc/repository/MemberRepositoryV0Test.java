package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
class MemberRepositoryV0Test {

    MemberRepositoryV0 repository = new MemberRepositoryV0();

    /**
     * 테스트는 반복 테스트가 매우 중요
     * 하지만 만약 테스트 중간에 오류가 발생해서 삭제 로직이 실행이 안된다면, 반복 테스트 불가능
     * 트랜잭션을 활용하면 해당 문제를 해결 가능
     */
    @Test
    void crud() throws SQLException {
        // 저장
        Member member = new Member("memberV0", 10000);
        repository.save(member);

        // 조회
        Member findMember = repository.findById(member.getMemberId());
        log.info("findMember={}", findMember);
        assertThat(findMember).isEqualTo(member);   // @Data는 equals/hashCode 자동 생성

        // 수정: money: 10000 -> 20000
        repository.update(member.getMemberId(), 20000);
        Member updatedMember = repository.findById(member.getMemberId());
        assertThat(updatedMember.getMoney()).isEqualTo(20000);

        // 삭제
        repository.delete(member.getMemberId());
        assertThatThrownBy(() -> repository.findById(member.getMemberId()))
                .isInstanceOf(NoSuchElementException.class);
    }
}