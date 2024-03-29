package pl.sztukakodu.bookaro.order.application;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.annotation.DirtiesContext;
import pl.sztukakodu.bookaro.catalog.application.port.CatalogUseCase;
import pl.sztukakodu.bookaro.catalog.db.BookJpaRepository;
import pl.sztukakodu.bookaro.catalog.domain.Book;
import pl.sztukakodu.bookaro.order.domain.Delivery;
import pl.sztukakodu.bookaro.order.domain.OrderStatus;
import pl.sztukakodu.bookaro.order.domain.Recipient;

import java.math.BigDecimal;
import java.util.List;

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
     void placeAnOrderHappyPath() {
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
     void orderingMoreBooksThanAvailableThrowsException() {
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
     void userCanRevokeOrder() {
        // given
        Book effectiveJava = setUpEffectiveJava(50L);
        Long orderId = placeOrder(effectiveJava.getId(), 15);
        assertEquals(35L, availableBooks(effectiveJava));

        // when
        // TODO: Update email
        UpdateStatusCommand command = new UpdateStatusCommand(orderId, OrderStatus.CANCELED, user("test@example.com"));
        manipulateOrderService.updateOrderStatus(command);

        // then
        assertEquals(50L, availableBooks(effectiveJava));
        assertEquals(OrderStatus.CANCELED, queryOrderService.findById(orderId).get().getStatus());
    }

    @Test
     void userCannotRevokePaidOrder() {
        //given
        Book effectiveJava = setUpEffectiveJava(50L);
        Long orderId = placeOrder(effectiveJava.getId(), 15);
        UpdateStatusCommand updateStatusToPaid = new UpdateStatusCommand(orderId, OrderStatus.PAID, user("test@example.com"));
        manipulateOrderService.updateOrderStatus(updateStatusToPaid);

        //when
        UpdateStatusCommand updateStatusToCanceled = new UpdateStatusCommand(orderId, OrderStatus.CANCELED, user("test@example.com"));
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> manipulateOrderService.updateOrderStatus(updateStatusToCanceled));

        //then
        assertThat(exception.getMessage(), containsString("Unable to mark " + OrderStatus.PAID + " order as " + OrderStatus.CANCELED));
    }

    @Test
     void userCannotRevokeShippedOrder() {
        //given
        Book effectiveJava = setUpEffectiveJava(50L);
        Long orderId = placeOrder(effectiveJava.getId(), 15);
        UpdateStatusCommand updateStatusToPaid = new UpdateStatusCommand(orderId, OrderStatus.PAID, user("test@example.com"));
        manipulateOrderService.updateOrderStatus(updateStatusToPaid);
        UpdateStatusCommand updateStatusToShipped = new UpdateStatusCommand(orderId, OrderStatus.SHIPPED, user("test@example.com"));
        manipulateOrderService.updateOrderStatus(updateStatusToShipped);

        //when
        // TODO: Update email
        UpdateStatusCommand updateStatusToCanceled = new UpdateStatusCommand(orderId, OrderStatus.CANCELED, user("test@example.com"));
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> manipulateOrderService.updateOrderStatus(updateStatusToCanceled));

        //then
        assertThat(exception.getMessage(), containsString("Unable to mark " + OrderStatus.SHIPPED + " order as " + OrderStatus.CANCELED));
    }

    @Test
     void userCannotOrderNonExistingBooks() {
        //given
        final long FAKE_ID = 10L;

        //when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> placeOrder(FAKE_ID, 10));

        //then
        assertThat(exception.getMessage(), containsString("There is no book with id " + FAKE_ID));
    }

    @Test
     void userCannotOrderNegativeNumberOfBooks() {
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
     void userCannotRevokeOtherUsersOrder() {
        // given
        Book effectiveJava = setUpEffectiveJava(50L);
        String adam = "adam@example.com";
        Long orderId = placeOrder(effectiveJava.getId(), 15, adam);
        assertEquals(35L, availableBooks(effectiveJava));

        // when
        UpdateStatusCommand command = new UpdateStatusCommand(orderId, OrderStatus.CANCELED, user("marek@example.com"));
        manipulateOrderService.updateOrderStatus(command);

        // then
        assertEquals(35L, availableBooks(effectiveJava));
        assertEquals(OrderStatus.NEW, queryOrderService.findById(orderId).get().getStatus());

    }

    @Test
     void adminCanRevokeAnyOrder() {
        // given
        Book effectiveJava = setUpEffectiveJava(50L);
        String adam = "adam@example.com";
        Long orderId = placeOrder(effectiveJava.getId(), 15, adam);
        assertEquals(35L, availableBooks(effectiveJava));

        // when
        UpdateStatusCommand command = new UpdateStatusCommand(orderId, OrderStatus.CANCELED, admin());
        manipulateOrderService.updateOrderStatus(command);

        // then
        assertEquals(50L, availableBooks(effectiveJava));
        assertEquals(OrderStatus.CANCELED, queryOrderService.findById(orderId).get().getStatus());

    }

    @Test
     void adminCanMarkOrderAsPaid() {
        // given
        Book effectiveJava = setUpEffectiveJava(50L);
        Long orderId = placeOrder(effectiveJava.getId(), 15);
        assertEquals(35L, availableBooks(effectiveJava));

        // when
        // TODO: Update email
        UpdateStatusCommand command = new UpdateStatusCommand(orderId, OrderStatus.PAID, admin());
        manipulateOrderService.updateOrderStatus(command);

        // then
        assertEquals(35L, availableBooks(effectiveJava));
        assertEquals(OrderStatus.PAID, queryOrderService.findById(orderId).get().getStatus());
    }

    @Test
     void shippingCostsAreAddedToTotalOrderPrice() {
        // given
        Book book = setUpBook(50L, "49.90");

        // when
        Long orderId = placeOrder(book.getId(), 1);

        // then
        assertEquals("59.80", orderOf(orderId).getFinalPrice().toPlainString());
    }

    @Test
     void shippingCostsAreDiscountedOver100zlotys() {
        // given
        Book book = setUpBook(50L, "49.90");

        // when
        Long orderId = placeOrder(book.getId(), 3);

        // then
        RichOrder order = orderOf(orderId);
        assertEquals("149.70", order.getFinalPrice().toPlainString());
        assertEquals("149.70", order.getPrice().getItemsPrice().toPlainString());
    }

    @Test
     void cheapestBookIsHalfPricedWhenTotalOver200zlotys() {
        // given
        Book book = setUpBook(50L, "49.90");

        // when
        Long orderId = placeOrder(book.getId(), 5);

        // then
        RichOrder order = orderOf(orderId);
        assertEquals("224.55", order.getFinalPrice().toPlainString());
    }

    @Test
     void cheapestBookIsFreeWhenTotalOver400zlotys() {
        // given
        Book book = setUpBook(50L, "49.90");

        // when
        Long orderId = placeOrder(book.getId(), 10);

        // then
        assertEquals("449.10", orderOf(orderId).getFinalPrice().toPlainString());
    }

    private User user(String email){
        return new User(email, "", List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    private User admin() {
        return new User("admin", "", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    private RichOrder orderOf(Long orderId) {
        return queryOrderService.findById(orderId).get();
    }

    private Book setUpBook(long available, String price) {
        return bookJpaRepository.save(new Book("Java Concurrency in Practice", 2006, new BigDecimal(price), available));
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
                .delivery(Delivery.COURIER)
                .build();

        return manipulateOrderService.placeOrder(command).getRight();
    }
}