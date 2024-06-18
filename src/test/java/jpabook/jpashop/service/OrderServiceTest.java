package jpabook.jpashop.service;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class OrderServiceTest {
    @Autowired
    EntityManager em;

    @Autowired
    OrderService orderService;

    @Autowired
    OrderRepository orderRepository;

    @Test
    void 상품주문() throws Exception {
        // given
        Member member = createMember();

        final int BOOK_PRICE = 10000;
        final int BOOK_QUANTITY = 10;
        Book book = createBook("JPA 책", BOOK_PRICE, BOOK_QUANTITY);

        // when
        final int ORDER_COUNT = 2;
        Long orderId = orderService.order(member.getId(), book.getId(), ORDER_COUNT);

        // then
        Order order = orderRepository.findOne(orderId);
        assertEquals(OrderStatus.ORDER, order.getStatus());
        assertEquals(1, order.getOrderItems().size());
        assertEquals(BOOK_PRICE * ORDER_COUNT, order.getTotalPrice());
        assertEquals(BOOK_QUANTITY - ORDER_COUNT, book.getStockQuantity());
    }

    @Test
    void 상품주문_재고수량초과() throws Exception {
        Member member = createMember();

        final int BOOK_PRICE = 10000;
        final int BOOK_QUANTITY = 10;
        Book book = createBook("JPA 책", BOOK_PRICE, BOOK_QUANTITY);

        final int ORDER_COUNT = 11;
        assertThrows(NotEnoughStockException.class, () -> {
            Long orderId = orderService.order(member.getId(), book.getId(), ORDER_COUNT);
        });
    }


    @Test
    void 주문취소() throws Exception {
        // given
        Member member = createMember();
        final int BOOK_QUANTITY = 10;
        Book jpaBook = createBook("JPA BOOK", 10000, BOOK_QUANTITY);

        final int ORDER_COUNT = 2;
        Long orderId = orderService.order(member.getId(), jpaBook.getId(), ORDER_COUNT);

        // when
        orderService.cancelOrder(orderId);

        // then
        Order order = orderRepository.findOne(orderId);
        assertEquals(OrderStatus.CANCEL, order.getStatus());
        assertEquals(BOOK_QUANTITY, jpaBook.getStockQuantity());

    }

    private Member createMember() {
        Member member = new Member();
        member.setName("홍길동");
        member.setAddress(Address.builder()
                .city("서울").street("한강").zipcode("12345")
                .build());
        em.persist(member);
        return member;
    }

    private Book createBook( String name, int price, int quantity) {
        Book book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(quantity);
        em.persist(book);
        return book;
    }
}