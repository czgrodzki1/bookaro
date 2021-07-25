package pl.sztukakodu.bookaro.order.application;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import pl.sztukakodu.bookaro.catalog.application.port.CatalogUseCase;
import pl.sztukakodu.bookaro.catalog.db.BookJpaRepository;
import pl.sztukakodu.bookaro.catalog.domain.Book;
import pl.sztukakodu.bookaro.clock.Clock;
import pl.sztukakodu.bookaro.order.application.port.ManipulateOrderUseCase;
import pl.sztukakodu.bookaro.order.domain.OrderStatus;
import pl.sztukakodu.bookaro.order.domain.Recipient;

import java.math.BigDecimal;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        properties = "app.orders.payment-period=1H"
)
@AutoConfigureTestDatabase
class AbandonedOrdersJobTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public Clock.MockClock clock() {
            return new Clock.MockClock();
        }
    }

    @Autowired
    private AbandonedOrdersJob abandonedOrdersJob;

    @Autowired
    private BookJpaRepository bookJpaRepository;

    @Autowired
    private ManipulateOrderService manipulateOrderService;

    @Autowired
    private QueryOrderService queryOrderService;

    @Autowired
    private CatalogUseCase catalogUseCase;

    @Autowired
    private Clock.MockClock mockClock;

    @Test
    public void ShouldMarkOrdersAsAbandoned() {
        //given
        Book book = setUpEffectiveJava(50L);
        Long orderId = placeOrder(book.getId(), 20);

        //when
        mockClock.tick(Duration.ofHours(2));
        abandonedOrdersJob.run();

        //then
        assertEquals(OrderStatus.ABANDONED, queryOrderService.findById(orderId).get().getStatus());
        assertEquals(50L, availableBooks(book));

    }

    private Book setUpEffectiveJava(final Long available) {
        return bookJpaRepository.save(new Book("Effective Java", 2005, new BigDecimal("129.90"), available));
    }

    private Recipient setUpRecipient() {
        return Recipient.builder().email("recipient@example.com").build();
    }

    private Long placeOrder(Long bookId, int quantity) {
        ManipulateOrderUseCase.PlaceOrderCommand command = ManipulateOrderUseCase.PlaceOrderCommand
                .builder()
                .recipient(setUpRecipient())
                .item(new ManipulateOrderUseCase.OrderItemCommand(bookId, quantity))
                .build();

        return manipulateOrderService.placeOrder(command).getRight();
    }

    private Long availableBooks(Book book) {
        return catalogUseCase.findById(book.getId())
                .get()
                .getAvailable();
    }

}