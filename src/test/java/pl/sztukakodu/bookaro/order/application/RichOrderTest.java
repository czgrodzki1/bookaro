package pl.sztukakodu.bookaro.order.application;

import org.junit.jupiter.api.Test;
import pl.sztukakodu.bookaro.catalog.domain.Book;
import pl.sztukakodu.bookaro.order.domain.OrderItem;
import pl.sztukakodu.bookaro.order.domain.OrderStatus;
import pl.sztukakodu.bookaro.order.domain.Recipient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RichOrderTest {

    @Test
    void calculatesTotalPriceForEmptyOrder() {
        //given
        RichOrder order = new RichOrder(1L, OrderStatus.NEW, Collections.emptySet(), Recipient.builder().build(), LocalDateTime.now());

        //when
        BigDecimal price = order.totalPrice();

        //then
        assertEquals(BigDecimal.ZERO, price);

    }

    @Test
    void calculatesTotalPrice() {
        //given
        Book book1 = new Book();
        book1.setPrice(new BigDecimal("12.00"));
        Book book2 = new Book();
        book2.setPrice(new BigDecimal("20.00"));
        Set<OrderItem> books = new HashSet<>(
                Arrays.asList(
                        new OrderItem(book1, 3),
                        new OrderItem(book2, 5)

                )
        );

        RichOrder order = new RichOrder(1L, OrderStatus.NEW, books, Recipient.builder().build(), LocalDateTime.now());

        //when
        BigDecimal price = order.totalPrice();

        //then
        assertEquals(new BigDecimal("136.00"), price);

    }

}