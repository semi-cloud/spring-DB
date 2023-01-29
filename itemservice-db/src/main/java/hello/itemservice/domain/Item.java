package hello.itemservice.domain;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity   // JPA 사용 객체로 등록
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_name", length = 10)  // 자동으로 언더스코어 <-> 카멜 케이스 변환 가능
    private String itemName;
    private Integer price;
    private Integer quantity;

    public Item() {   // 기본 생성자 필수(지연 로딩에 의한 프록시 기술을 사용할 때 필요)
    }

    public Item(String itemName, Integer price, Integer quantity) {
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
    }
}
