package pl.sztukakodu.bookaro.order.price;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import pl.sztukakodu.bookaro.catalog.domain.Book;
import pl.sztukakodu.bookaro.order.domain.Order;
import pl.sztukakodu.bookaro.order.domain.OrderItem;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@AutoConfigureTestDatabase
class PriceServiceTest {

    PriceService priceService = new PriceService();

    @Test
    void calculatesTotalPriceForEmptyOrder() {
        //givenR
        Order order = Order
                .builder()
                .build();

        //when
        OrderPrice price = priceService.calculatePrice(order);

        //then
        assertEquals(BigDecimal.ZERO, price.finalPrice());

    }

    @Test
    void calculatesTotalPrice() {
        //given
        Book book1 = new Book();
        book1.setPrice(new BigDecimal("12.00"));
        Book book2 = new Book();
        book2.setPrice(new BigDecimal("20.00"));

        Order order = Order
                .builder()
                .item(new OrderItem(book1, 2))
                .item(new OrderItem(book2, 1))
                .build();

        //when
        OrderPrice price = priceService.calculatePrice(order);

        //then
        assertEquals("53.90", price.finalPrice().toPlainString());
        assertEquals("44.00", price.getItemsPrice().toPlainString());

    }

    @Test
    void calculatesTotalPriceWithDeliveryDiscount() {
        //given
        Book book1 = new Book();
        book1.setPrice(new BigDecimal("12.00"));
        Book book2 = new Book();
        book2.setPrice(new BigDecimal("20.00"));

        Order order = Order
                .builder()
                .item(new OrderItem((book1), 3))
                .item(new OrderItem(book2, 5))
                .build();

        //when
        OrderPrice price = priceService.calculatePrice(order);

        //then
        assertEquals("136.00", price.finalPrice().toPlainString());
        assertEquals("136.00", price.getItemsPrice().toPlainString());

    }

    @Test
    void calculatesTotalPriceWithDeliveryAndHalfPriceDiscount() {
        //given
        Book book1 = new Book();
        book1.setPrice(new BigDecimal("50.00"));
        Book book2 = new Book();
        book2.setPrice(new BigDecimal("20.00"));

        Order order = Order
                .builder()
                .item(new OrderItem((book1), 3))
                .item(new OrderItem(book2, 5))
                .build();

        //when
        OrderPrice price = priceService.calculatePrice(order);

        //then
        assertEquals("240.00", price.finalPrice().toPlainString());
        assertEquals("250.00", price.getItemsPrice().toPlainString());

    }

    @Test
    void calculatesTotalPriceWithDeliveryAndOneFreeBookDiscount() {
        //given
        Book book1 = new Book();
        book1.setPrice(new BigDecimal("100.00"));
        Book book2 = new Book();
        book2.setPrice(new BigDecimal("20.00"));

        Order order = Order
                .builder()
                .item(new OrderItem((book1), 5))
                .item(new OrderItem(book2, 5))
                .build();

        //when
        OrderPrice price = priceService.calculatePrice(order);

        //then
        assertEquals("580.00", price.finalPrice().toPlainString());
        assertEquals("600.00", price.getItemsPrice().toPlainString());

    }

}