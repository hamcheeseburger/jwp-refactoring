package kitchenpos.application;

import static kitchenpos.domain.OrderStatus.COMPLETION;
import static kitchenpos.domain.OrderStatus.COOKING;
import static kitchenpos.domain.OrderStatus.MEAL;
import static kitchenpos.support.MenuGroupFixture.MENU_GROUP_1;
import static kitchenpos.support.OrderFixture.ORDER_COOKING_1;
import static kitchenpos.support.OrderLineItemFixture.ORDER_LINE_ITEM_1;
import static kitchenpos.support.OrderTableFixture.ORDER_TABLE_EMPTY_1;
import static kitchenpos.support.OrderTableFixture.ORDER_TABLE_NOT_EMPTY_1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import kitchenpos.domain.Menu;
import kitchenpos.domain.MenuGroup;
import kitchenpos.domain.Order;
import kitchenpos.domain.OrderLineItem;
import kitchenpos.domain.OrderMenu;
import kitchenpos.domain.OrderMenuProduct;
import kitchenpos.domain.Product;
import kitchenpos.dto.request.OrderMenuRequest;
import kitchenpos.dto.request.OrderRequest;
import kitchenpos.dto.request.OrderStatusRequest;
import kitchenpos.dto.response.OrderLineItemResponse;
import kitchenpos.dto.response.OrderResponse;
import kitchenpos.exceptions.EntityNotExistException;
import kitchenpos.exceptions.OrderAlreadyCompletionException;
import kitchenpos.exceptions.OrderLineItemsEmptyException;
import kitchenpos.exceptions.OrderTableEmptyException;
import kitchenpos.support.ProductFixture;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NonAsciiCharacters")
class OrderServiceTest extends ServiceTest {

    @Test
    void 주문을_저장한다() {
        // given
        final MenuGroup menuGroup = 메뉴그룹을_저장한다(MENU_GROUP_1.생성());
        final Long menuId = 상품과_함께_메뉴를_저장한다(menuGroup).getId();

        final Long orderTableId = 주문테이블을_저장한다(ORDER_TABLE_NOT_EMPTY_1.생성()).getId();
        final OrderRequest orderRequest = new OrderRequest(orderTableId,
                List.of(new OrderMenuRequest(menuId, 1)));

        // when
        final OrderResponse orderResponse = orderService.create(orderRequest);

        //then
        assertThat(orderResponse.getId()).isEqualTo(1L);
    }

    @Test
    void 주문을_저장할_때_주문할_메뉴가_1개_이상이_아니면_예외를_반환한다() {
        // given
        final Long orderTableId = 주문테이블을_저장한다(ORDER_TABLE_NOT_EMPTY_1.생성()).getId();
        final OrderRequest orderRequest = new OrderRequest(orderTableId, List.of());

        // when, then
        assertThatThrownBy(() -> orderService.create(orderRequest))
                .isInstanceOf(OrderLineItemsEmptyException.class);
    }

    @Test
    void 주문을_저장할_때_존재하지_않는_메뉴를_주문하면_예외를_발생한다() {
        // given
        final long notExistMenuId = Long.MAX_VALUE;
        final Long orderTableId = 주문테이블을_저장한다(ORDER_TABLE_NOT_EMPTY_1.생성()).getId();
        final OrderRequest orderRequest = new OrderRequest(orderTableId,
                List.of(new OrderMenuRequest(notExistMenuId, 1)));

        // when, then
        assertThatThrownBy(() -> orderService.create(orderRequest))
                .isInstanceOf(EntityNotExistException.class);
    }

    @Test
    void 주문을_저장할_때_존재하지_않는_테이블_번호로_주문하면_예외를_발생한다() {
        // given
        final MenuGroup menuGroup = 메뉴그룹을_저장한다(MENU_GROUP_1.생성());
        final Long menuId = 상품과_함께_메뉴를_저장한다(menuGroup).getId();

        final long notExistOrderTableId = Long.MAX_VALUE;
        final OrderRequest orderRequest = new OrderRequest(notExistOrderTableId,
                List.of(new OrderMenuRequest(menuId, 1)));

        // when, then
        assertThatThrownBy(() -> orderService.create(orderRequest))
                .isInstanceOf(EntityNotExistException.class);
    }

    @Test
    void 주문을_저장할_때_테이블이_비어있다면_예외를_발생한다() {
        // given
        final MenuGroup menuGroup = 메뉴그룹을_저장한다(MENU_GROUP_1.생성());
        final Long menuId = 상품과_함께_메뉴를_저장한다(menuGroup).getId();

        final Long orderTableId = 주문테이블을_저장한다(ORDER_TABLE_EMPTY_1.생성()).getId();
        final OrderRequest orderRequest = new OrderRequest(orderTableId,
                List.of(new OrderMenuRequest(menuId, 1)));

        // when, then
        assertThatThrownBy(() -> orderService.create(orderRequest))
                .isInstanceOf(OrderTableEmptyException.class);
    }

    @Test
    void 모든_주문을_조회할_때_주문한_메뉴도_함께_조회한다() {
        // given
        final MenuGroup menuGroup = 메뉴그룹을_저장한다(MENU_GROUP_1.생성());
        final Menu menu = 상품과_함께_메뉴를_저장한다(menuGroup);
        final Product product = ProductFixture.PRODUCT_PRICE_10000.생성();

        final OrderMenuProduct orderMenuProduct = new OrderMenuProduct(null, null, product.getName(),
                product.getPrice().getValue(), 1);
        final OrderMenu orderMenu = new OrderMenu(menu.getName(), menu.getPrice().getValue(), menuGroup.getName(),
                List.of(orderMenuProduct));
        주문메뉴를_저장한다(orderMenu);

        final Long orderTableId = 주문테이블을_저장한다(ORDER_TABLE_NOT_EMPTY_1.생성()).getId();
        final OrderLineItem orderLineItem = ORDER_LINE_ITEM_1.생성(orderMenu);
        final Order savedOrder = 주문을_저장한다(ORDER_COOKING_1.생성(orderTableId, List.of(orderLineItem)));

        final OrderResponse expectedOrderResponse = OrderResponse.from(savedOrder);
        final OrderLineItemResponse orderLineItemResponse = OrderLineItemResponse.from(orderLineItem);

        // when
        final List<OrderResponse> orderResponses = orderService.list();

        // then
        assertAll(
                () -> assertThat(orderResponses).usingRecursiveFieldByFieldElementComparator()
                        .usingElementComparatorIgnoringFields("orderLineItems")
                        .containsOnly(expectedOrderResponse),
                () -> assertThat(orderResponses.get(0).getOrderLineItems())
                        .usingRecursiveFieldByFieldElementComparator()
                        .usingComparatorForType(Comparator.comparingInt(BigDecimal::intValue), BigDecimal.class)
                        .containsOnly(orderLineItemResponse)
        );
    }

    @Test
    void 주문의_상태를_변경할_수_있다() {
        // given
        final Long orderTableId = 주문테이블을_저장한다(ORDER_TABLE_NOT_EMPTY_1.생성()).getId();
        final Order savedOrder = 주문항목과_함께_주문을_저장한다(orderTableId, COOKING);

        // when
        final OrderResponse orderResponse = orderService.changeOrderStatus(savedOrder.getId(),
                new OrderStatusRequest(COMPLETION.name()));

        // then
        assertThat(orderResponse.getOrderStatus()).isEqualTo(COMPLETION.name());
    }

    @Test
    void 주문을_변경할_때_존재하지_않는_주문이면_예외를_반환한다() {
        // given
        final long notExistOrderId = Long.MAX_VALUE;

        // when, then
        assertThatThrownBy(
                () -> orderService.changeOrderStatus(notExistOrderId, new OrderStatusRequest(COMPLETION.name())))
                .isInstanceOf(EntityNotExistException.class);
    }

    @Test
    void 주문의_상태를_변경할_때_이미_완료된_주문이면_예외를_반환한다() {
        // given
        final Long orderTableId = 주문테이블을_저장한다(ORDER_TABLE_NOT_EMPTY_1.생성()).getId();
        final Order savedOrder = 주문항목과_함께_주문을_저장한다(orderTableId, COMPLETION);

        // when, then
        assertThatThrownBy(
                () -> orderService.changeOrderStatus(savedOrder.getId(), new OrderStatusRequest(MEAL.name())))
                .isInstanceOf(OrderAlreadyCompletionException.class);
    }
}
