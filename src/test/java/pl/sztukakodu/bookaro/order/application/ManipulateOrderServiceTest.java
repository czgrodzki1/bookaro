package pl.sztukakodu.bookaro.order.application;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import pl.sztukakodu.bookaro.catalog.application.port.CatalogUseCase;
import pl.sztukakodu.bookaro.catalog.db.BookJpaRepository;
import pl.sztukakodu.bookaro.catalog.domain.Book;
import pl.sztukakodu.bookaro.order.domain.OrderStatus;
import pl.sztukakodu.bookaro.order.domain.Recipient;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static pl.sztukakodu.bookaro.order.application.port.ManipulateOrderUseCase.*;
import static pl.sztukakodu.bookaro.order.application.port.ManipulateOrderUseCase.OrderItemCommand;
import static pl.sztukakodu.bookaro.order.application.port.ManipulateOrderUseCase.PlaceOrderCommand;
import static pl.sztukakodu.bookaro.order.application.port.ManipulateOrderUseCase.PlaceOrderResponse;

@SpringBootTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ManipulateOrderServiceTest {

    @Autowired
    private BookJpaRepository bookJpaRepository;
    @Autowired
    private ManipulateOrderService manipulateOrderService;
    @Autowired
    private CatalogUseCase catalogUseCase;
    @Autowired
    private QueryOrderService queryOrderService;

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
    public void orderingMoreBooksThanAvailableThrowsException() {
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

    @Test
    public void userCanRevokeOrder() {
        // given
        Book effectiveJava = setUpEffectiveJava(50L);
        Long orderId = placeOrder(effectiveJava.getId(), 15);
        assertEquals(35L, availableBooks(effectiveJava));

        // when
        // TODO: Update email
        UpdateStatusCommand command = new UpdateStatusCommand(orderId, OrderStatus.CANCELED, "test@example.com");
        manipulateOrderService.updateOrderStatus(command);

        // then
        assertEquals(50L, availableBooks(effectiveJava));
        assertEquals(OrderStatus.CANCELED, queryOrderService.findById(orderId).get().getStatus());
    }

    @Test
    public void userCannotRevokePaidOrder() {
        //given
        Book effectiveJava = setUpEffectiveJava(50L);
        Long orderId = placeOrder(effectiveJava.getId(), 15);
        // TODO: Update email
        UpdateStatusCommand updateStatusToPaid = new UpdateStatusCommand(orderId, OrderStatus.PAID, "test@example.com");
        manipulateOrderService.updateOrderStatus(updateStatusToPaid);

        //when
        // TODO: Update email
        UpdateStatusCommand updateStatusToCanceled = new UpdateStatusCommand(orderId, OrderStatus.CANCELED, "test@example.com");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> manipulateOrderService.updateOrderStatus(updateStatusToCanceled));

        //then
        assertThat(exception.getMessage(), containsString("Unable to mark " + OrderStatus.PAID + " order as " + OrderStatus.CANCELED));
    }

    @Test
    public void userCannotRevokeShippedOrder() {
        //given
        Book effectiveJava = setUpEffectiveJava(50L);
        Long orderId = placeOrder(effectiveJava.getId(), 15);
        // TODO: Update email
        UpdateStatusCommand updateStatusToPaid = new UpdateStatusCommand(orderId, OrderStatus.PAID, "test@example.com");
        manipulateOrderService.updateOrderStatus(updateStatusToPaid);
        // TODO: Update email
        UpdateStatusCommand updateStatusToShipped = new UpdateStatusCommand(orderId, OrderStatus.SHIPPED, "test@example.com");
        manipulateOrderService.updateOrderStatus(updateStatusToShipped);

        //when
        // TODO: Update email
        UpdateStatusCommand updateStatusToCanceled = new UpdateStatusCommand(orderId, OrderStatus.CANCELED, "test@example.com");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> manipulateOrderService.updateOrderStatus(updateStatusToCanceled));

        //then
        assertThat(exception.getMessage(), containsString("Unable to mark " + OrderStatus.SHIPPED + " order as " + OrderStatus.CANCELED));
    }

    @Test
    public void userCannotOrderNonExistingBooks() {
        //given
        final long FAKE_ID = 10L;

        //when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> placeOrder(FAKE_ID, 10));

        //then
        assertThat(exception.getMessage(), containsString("There is no book with id " + FAKE_ID));
    }

    @Test
    public void userCannotOrderNegativeNumberOfBooks() {
        //given
        final long AVAILABLE = 10L;
        final int REQUESTED = 15;
        Book effectiveJava = setUpEffectiveJava(AVAILABLE);

        //when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> placeOrder(effectiveJava.getId(), REQUESTED));

        //then
        assertThat(exception.getMessage(), containsString("To many copies of book " + effectiveJava.getTitle()
                + " requested " + REQUESTED + " of " + AVAILABLE + " available"));
    }

    @Test
    public void userCannotRevokeOtherUsersOrder() {
        // given
        Book effectiveJava = setUpEffectiveJava(50L);
        String adam = "adam@example.com";
        Long orderId = placeOrder(effectiveJava.getId(), 15, adam);
        assertEquals(35L, availableBooks(effectiveJava));

        // when
        UpdateStatusCommand command = new UpdateStatusCommand(orderId, OrderStatus.CANCELED, "marek@example.com");
        manipulateOrderService.updateOrderStatus(command);

        // then
        assertEquals(35L, availableBooks(effectiveJava));
        assertEquals(OrderStatus.NEW, queryOrderService.findById(orderId).get().getStatus());

    }

    @Test
    // TODO: fix with security
    public void adminCanRevokeAnyOrder() {
        // given
        Book effectiveJava = setUpEffectiveJava(50L);
        String adam = "adam@example.com";
        Long orderId = placeOrder(effectiveJava.getId(), 15, adam);
        assertEquals(35L, availableBooks(effectiveJava));

        // when
        UpdateStatusCommand command = new UpdateStatusCommand(orderId, OrderStatus.CANCELED, "admin@example.com");
        manipulateOrderService.updateOrderStatus(command);

        // then
        assertEquals(50L, availableBooks(effectiveJava));
        assertEquals(OrderStatus.CANCELED, queryOrderService.findById(orderId).get().getStatus());

    }

    @Test
    public void adminCanMarkOrderAsPaid() {
        // given
        Book effectiveJava = setUpEffectiveJava(50L);
        Long orderId = placeOrder(effectiveJava.getId(), 15);
        assertEquals(35L, availableBooks(effectiveJava));

        // when
        // TODO: Update email
        UpdateStatusCommand command = new UpdateStatusCommand(orderId, OrderStatus.PAID, "admin@example.com");
        manipulateOrderService.updateOrderStatus(command);

        // then
        assertEquals(35L, availableBooks(effectiveJava));
        assertEquals(OrderStatus.PAID, queryOrderService.findById(orderId).get().getStatus());
    }

    private Book setUpJavaConcurrency(final Long available) {
        return bookJpaRepository.save(new Book("Java Concurrency in Practice", 2006, new BigDecimal("99.90"), available));
    }

    private Book setUpEffectiveJava(final Long available) {
        return bookJpaRepository.save(new Book("Effective Java", 2005, new BigDecimal("129.90"), available));
    }

    private Recipient setUpRecipient() {
        return setUpRecipient("example@example.com");
    }

    private Recipient setUpRecipient(String email) {
        return Recipient.builder().email(email).build();
    }

    private Long availableBooks(Book book) {
        return catalogUseCase.findById(book.getId())
                .get()
                .getAvailable();
    }

    private Long placeOrder(Long bookId, int quantity) {
        return placeOrder(bookId, quantity, "test@example.com");
    }

    private Long placeOrder(Long bookId, int quantity, String recipient) {
        PlaceOrderCommand command = PlaceOrderCommand
                .builder()
                .recipient(setUpRecipient(recipient))
                .item(new OrderItemCommand(bookId, quantity))
                .build();

        return manipulateOrderService.placeOrder(command).getRight();
    }
}