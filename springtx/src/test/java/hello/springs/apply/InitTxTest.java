package hello.springs.apply;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.PostConstruct;

@SpringBootTest
@Slf4j
public class InitTxTest {
    @Autowired Hello hello;

    @Test
    void go() {
        // 초기화 코드는 스프링이 초기화 시점에 호출한다.
    }

    @TestConfiguration
    static class InitTxTextConfig {

        @Bean
        public Hello hello() {
            return new Hello();
        }
    }

    static class Hello {
        /**
         * 빈 초기화가 먼저 진행되므로 트랜잭션 적용 X
         */
        @PostConstruct
        @Transactional
        public void initV1() {
            boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("Hello init @PostConstruct txActive={}", txActive);
        }

        /**
         * 스프링 컨테이너 모두 준비 완료 이후에 호출하므로 트랜잭션 적용 O
         */
        @EventListener(ApplicationReadyEvent.class)
        @Transactional
        public void initV2() {
            boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("Hello init ApplicationReady txActive={}", txActive);
        }
    }
}
