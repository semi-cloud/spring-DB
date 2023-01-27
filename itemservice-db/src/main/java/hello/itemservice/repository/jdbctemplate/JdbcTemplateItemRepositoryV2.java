package hello.itemservice.repository.jdbctemplate;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * NamedParameterJdbcTemplate : 이름 지정 파라미터 바인딩
 *
 * 파라 미터 종류
 * 1. BeanPropertySqlParameterSource : 자바 프로퍼티 규약을 통해 파라미터 객체 생성(getXXX -> XXX)
 * 2. MapSqlParameterSource : DTO에 없는 필드를 바인딩 해야 하는 경우 메서드 체이닝을 통해 간편하게 가능
 * 3. Map<Key, Value>
 */
@Slf4j
public class JdbcTemplateItemRepositoryV2 implements ItemRepository {
    private final NamedParameterJdbcTemplate template;

    public JdbcTemplateItemRepositoryV2(DataSource dataSource) {
        this.template = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public Item save(Item item) {
        String sql = "insert into item(item_name, price, quantity) values (:itemName, :price, :quantity)";
        BeanPropertySqlParameterSource param = new BeanPropertySqlParameterSource(item);

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(sql, param, keyHolder);

        long key = keyHolder.getKey().longValue();   // DB에서 자동 생성된 키 가져오기
        item.setId(key);
        return item;
    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        String sql = "update item set item_name=:itemName, price=:price, quantity=:quantity where id=:id";
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("itemName", updateParam.getItemName())
                .addValue("price", updateParam.getPrice())
                .addValue("quantity", updateParam.getQuantity());
        template.update(sql, param);
    }

    @Override
    public Optional<Item> findById(Long id) {
        String sql = "select id, item_name as itemName, price, quantity from item where id=:id";

        try {
            Map<String, Long> param = Map.of("id", id);
            Item item = template.queryForObject(sql, param, itemRowMapper());  // 반환값이 하나일 때 사용
            return Optional.of(item);
        } catch (EmptyResultDataAccessException e) {   // 결과가 없을 때 예외(null)
            return Optional.empty();     // 빈 옵셔널 객체 반환
        }
    }

    /**
     * 검색 조건에 따른 모든 상황에 SQL들을 동적으로 생성해야 하는 문제 발생
     */
    @Override
    public List<Item> findAll(ItemSearchCond cond) {
        String itemName = cond.getItemName();
        Integer maxPrice = cond.getMaxPrice();
        String sql = "select id, item_name, price, quantity from item";
        BeanPropertySqlParameterSource param = new BeanPropertySqlParameterSource(cond);

        //동적 쿼리
        if (StringUtils.hasText(itemName) || maxPrice != null) {
            sql += " where";
        }

        boolean andFlag = false;
        if (StringUtils.hasText(itemName)) {
            sql += " item_name like concat('%', :itemName,'%')";
            andFlag = true;
        }

        if (maxPrice != null) {
            if (andFlag) {
                sql += " and";
            }
            sql += " price <= :maxPrice";
        }

        log.info("sql={}", sql);
        return template.query(sql, param, itemRowMapper());   // 결과가 하나 이상일 때 사용
    }

    /**
     *  BeanPropertyRowMapper
     *  : 데이터베이스의 ResultSet -> 자바빈 규약에 맞춰서 데이터를 변환
     *  : ex) select id -> setId(값)
     */
    private RowMapper<Item> itemRowMapper() {
        return BeanPropertyRowMapper.newInstance(Item.class);  // RDB의 언더스코어(_) -> 카멜 표기법으로 자동 변환
    }
}
