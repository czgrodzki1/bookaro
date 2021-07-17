package pl.sztukakodu.bookaro.order.application;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import pl.sztukakodu.bookaro.catalog.db.BookJpaRepository;
import pl.sztukakodu.bookaro.catalog.domain.Book;
import pl.sztukakodu.bookaro.order.domain.Recipient;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static pl.sztukakodu.bookaro.order.application.port.ManipulateOrderUseCase.*;

@DataJpaTest
@Import({ManipulateOrderService.class})
class ManipulateOrderServiceTest {

    @Autowired
    private BookJpaRepository bookJpaRepository;
    @Autowired
    private ManipulateOrderService manipulateOrderService;

    @Test
    public void placeAnOrderHappyPath() {
        //given
        Book effectiveJava = setUpEffectiveJava(50L);
        Book javaConcurrency = setUpJavaConcurrency(50L);
        PlaceOrderCommand command = PlaceOrderCommand.builder()
                .recipient(setUpRecipient())
                .item(new OrderItemCommand(effectiveJava.getId(), 10))
                .item(new OrderItemCommand(javaConcurrency.getId(), 10))
                .build();

        //when
        PlaceOrderResponse response = manipulateOrderService.placeOrder(command);

        //then
        assertTrue(response.isSuccess());
    }

    @Test
    public void orderingMoreBooksThanAvaliableThrowsException() {
        //given
        Book effectiveJava = setUpEffectiveJava(50L);
        PlaceOrderCommand command = PlaceOrderCommand.builder()
                .recipient(setUpRecipient())
                .item(new OrderItemCommand(effectiveJava.getId(), 60))
                .build();

        //when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> manipulateOrderService.placeOrder(command));

        //then
        assertEquals("To many copies of book " + effectiveJava.getTitle() + " requested "
                + 60 + " of " + effectiveJava.getAvailable() + " available", exception.getMessage());
    }

    private Book setUpJavaConcurrency(final Long available) {
        return bookJpaRepository.save(new Book("Java Concurrency in Practice", 2006, new BigDecimal("99.90"), available));
    }

    private Book setUpEffectiveJava(final Long available) {
        return bookJpaRepository.save(new Book("Effective Java", 2005, new BigDecimal("129.90"), available));
    }

    private Recipient setUpRecipient() {
        return Recipient.builder().email("example@example.com").build();
    }



}