package kitchenpos.application;

import static kitchenpos.support.MenuFixture.MENU_PRICE_10000;
import static kitchenpos.support.MenuGroupFixture.MENU_GROUP_1;
import static kitchenpos.support.MenuProductFixture.MENU_PRODUCT_1;
import static kitchenpos.support.OrderFixture.ORDER_COOKING_1;
import static kitchenpos.support.OrderLineItemFixture.ORDER_LINE_ITEM_1;
import static kitchenpos.support.OrderTableFixture.ORDER_TABLE_EMPTY_1;
import static kitchenpos.support.OrderTableFixture.ORDER_TABLE_NOT_EMPTY_1;
import static kitchenpos.support.ProductFixture.PRODUCT_PRICE_10000;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import kitchenpos.domain.Order;
import kitchenpos.domain.OrderLineItem;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NonAsciiCharacters")
class OrderServiceTest extends ServiceTest {

    @Test
    void 주문을_저장한다() {
        // given
        final Long productId = 제품을_저장한다(PRODUCT_PRICE_10000.생성()).getId();
        final Long menuGroupId = 메뉴그룹을_저장한다(MENU_GROUP_1.생성()).getId();
        final Long menuId = 메뉴를_저장한다(MENU_PRICE_10000.생성(menuGroupId, List.of(MENU_PRODUCT_1.생성(productId))))
                .getId();
        final OrderLineItem orderLineItem = ORDER_LINE_ITEM_1.생성(menuId);

        final Long orderTableId = 주문테이블을_저장한다(ORDER_TABLE_NOT_EMPTY_1.생성()).getId();
        final Order order = ORDER_COOKING_1.생성(orderTableId, List.of(orderLineItem));

        // when
        final Order savedOrder = orderService.create(order);

        //then
        assertThat(savedOrder.getId()).isNotNull();
    }

    @Test
    void 주문을_저장할_때_주문할_메뉴가_1개_이상이_아니면_예외를_반환한다() {
        // given
        final Long orderTableId = 주문테이블을_저장한다(ORDER_TABLE_NOT_EMPTY_1.생성()).getId();
        final Order order = ORDER_COOKING_1.생성(orderTableId);

        // when, then
        assertThatThrownBy(() -> orderService.create(order))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 주문을_저장할_때_존재하지_않는_메뉴를_주문하면_예외를_발생한다() {
        // given
        final long notExistMenuId = Long.MAX_VALUE;
        final OrderLineItem orderLineItem = ORDER_LINE_ITEM_1.생성(notExistMenuId);

        final Long orderTableId = 주문테이블을_저장한다(ORDER_TABLE_NOT_EMPTY_1.생성()).getId();
        final Order order = ORDER_COOKING_1.생성(orderTableId, List.of(orderLineItem));

        // when, then
        assertThatThrownBy(() -> orderService.create(order))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 주문을_저장할_때_존재하지_않는_테이블_번호로_주문하면_예외를_발생한다() {
        // given
        final Long productId = 제품을_저장한다(PRODUCT_PRICE_10000.생성()).getId();
        final Long menuGroupId = 메뉴그룹을_저장한다(MENU_GROUP_1.생성()).getId();
        final Long menuId = 메뉴를_저장한다(MENU_PRICE_10000.생성(menuGroupId, List.of(MENU_PRODUCT_1.생성(productId))))
                .getId();
        final OrderLineItem orderLineItem = ORDER_LINE_ITEM_1.생성(menuId);

        final long notExistOrderTableId = Long.MAX_VALUE;
        final Order order = ORDER_COOKING_1.생성(notExistOrderTableId, List.of(orderLineItem));

        // when, then
        assertThatThrownBy(() -> orderService.create(order))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 주문을_저장할_때_테이블이_비어있다면_예외를_발생한다() {
        // given
        final Long productId = 제품을_저장한다(PRODUCT_PRICE_10000.생성()).getId();
        final Long menuGroupId = 메뉴그룹을_저장한다(MENU_GROUP_1.생성()).getId();
        final Long menuId = 메뉴를_저장한다(MENU_PRICE_10000.생성(menuGroupId, List.of(MENU_PRODUCT_1.생성(productId))))
                .getId();
        final OrderLineItem orderLineItem = ORDER_LINE_ITEM_1.생성(menuId);

        final Long orderTableId = 주문테이블을_저장한다(ORDER_TABLE_EMPTY_1.생성()).getId();
        final Order order = ORDER_COOKING_1.생성(orderTableId, List.of(orderLineItem));

        // when, then
        assertThatThrownBy(() -> orderService.create(order))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
