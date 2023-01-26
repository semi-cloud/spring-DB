package hello.itemservice;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@Slf4j
@RequiredArgsConstructor
public class TestDataInit {

    private final ItemRepository itemRepository;

    /**
     * @EventListener : AOP 포함 스프링 컨테이너 초기화 완료 이후 애플리케이션 시작 시 확인용 초기 데이터 추가
     * @PostConstructor : AOP 가 완전히 처리되지 않은 시점에 발생할 수 있어서 @Transactional 이 동작하지 않을 수도 있음
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initData() {
        log.info("test data init");
        itemRepository.save(new Item("itemA", 10000, 10));
        itemRepository.save(new Item("itemB", 20000, 20));
    }

}
