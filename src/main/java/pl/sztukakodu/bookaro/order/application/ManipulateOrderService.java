package pl.sztukakodu.bookaro.order.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.sztukakodu.bookaro.catalog.db.BookJpaRepository;
import pl.sztukakodu.bookaro.catalog.domain.Book;
import pl.sztukakodu.bookaro.order.application.port.ManipulateOrderUseCase;
import pl.sztukakodu.bookaro.order.db.OrderJpaRepository;
import pl.sztukakodu.bookaro.order.db.RecipientRepository;
import pl.sztukakodu.bookaro.order.domain.*;
import pl.sztukakodu.bookaro.security.UserSecurity;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
class ManipulateOrderService implements ManipulateOrderUseCase {
    private final OrderJpaRepository orderJpaRepository;
    private final BookJpaRepository bookJpaRepository;
    private final RecipientRepository recipientJpaRepository;
    private final UserSecurity userSecurity;

    @Override
    public PlaceOrderResponse placeOrder(PlaceOrderCommand command) {
        Set<OrderItem> items = command.getItems()
                .stream()
                .map(this::toOrderItem)
                .collect(Collectors.toSet());

        Order order = Order
            .builder()
            .recipient(getOrCreateRecipient(command.getRecipient()))
            .items(items)
            .build();
        Order save = orderJpaRepository.save(order);

        bookJpaRepository.saveAll(updateBookAvailability(items));

        return PlaceOrderResponse.success(save.getId());
    }

    private Recipient getOrCreateRecipient(Recipient recipient) {
        return recipientJpaRepository.findByEmailIgnoreCase(recipient.getEmail()).orElse(recipient);
    }

    private Set<Book> updateBookAvailability(Set<OrderItem> items) {
          return items.stream().map(orderItem -> {
              Book book = orderItem.getBook();
              book.setAvailable(book.getAvailable() - orderItem.getQuantity());
              return book;
          }).collect(Collectors.toSet());
    }

    private Set<Book> revokeBookAvailability(Set<OrderItem> items) {
        return items.stream().map(orderItem -> {
            Book book = orderItem.getBook();
            book.setAvailable(book.getAvailable() + orderItem.getQuantity());
            return book;
        }).collect(Collectors.toSet());
    }

    private OrderItem toOrderItem(OrderItemCommand command) {
        int quantity = command.getQuantity();
        Book book = getBook(command.getBookId());
        if (book.getAvailable() >= quantity) {
            return new OrderItem(book, quantity);
        } else {
            throw new IllegalArgumentException("To many copies of book " + book.getTitle() + " requested "
                    + quantity + " of " + book.getAvailable() + " available");
        }
    }

    private Book getBook(Long id) {
        Optional<Book> book = bookJpaRepository.findById(id);
        if (book.isEmpty()) {
            throw new IllegalArgumentException("There is no book with id " + id);
        }

        return book.get();
    }

    @Override
    public void deleteOrderById(Long id) {
        orderJpaRepository.deleteById(id);
    }

    @Override
    @Transactional
    public UpdateStatusResponse updateOrderStatus(UpdateStatusCommand command) {
        return orderJpaRepository
                .findById(command.getOrderId())
                .map(order -> {
                    if (userSecurity.isOwnerOrAdmin(order.getRecipient().getEmail(), command.getUser())) {
                        UpdateStatusResult result = order.updateStatus(command.getStatus());
                        if (result.isRevoke()) {
                            bookJpaRepository.saveAll(revokeBookAvailability(order.getItems()));
                        }
                        orderJpaRepository.save(order);
                        return UpdateStatusResponse.success(order.getStatus());
                    }
                    return UpdateStatusResponse.failure(Error.FORBIDDEN);
                })
                .orElse(UpdateStatusResponse.failure(Error.NOT_FOUND));
    }
}
