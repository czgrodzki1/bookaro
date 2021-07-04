package pl.sztukakodu.bookaro.order.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.sztukakodu.bookaro.catalog.db.BookJpaRepository;
import pl.sztukakodu.bookaro.catalog.domain.Book;
import pl.sztukakodu.bookaro.order.application.port.ManipulateOrderUseCase;
import pl.sztukakodu.bookaro.order.db.OrderJpaRepository;
import pl.sztukakodu.bookaro.order.domain.Order;
import pl.sztukakodu.bookaro.order.domain.OrderItem;
import pl.sztukakodu.bookaro.order.domain.OrderStatus;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
class ManipulateOrderService implements ManipulateOrderUseCase {
    private final OrderJpaRepository repository;
    private final BookJpaRepository bookJpaRepository;

    @Override public PlaceOrderResponse placeOrder(PlaceOrderCommand command) {
        Set<OrderItem> items = command.getItems()
                .stream()
                .map(this::toOrderItem)
                .collect(Collectors.toSet());

        Order order = Order
            .builder()
            .recipient(command.getRecipient())
            .items(items)
            .build();
        Order save = repository.save(order);

        bookJpaRepository.saveAll(updateBookAvailability(items));

        return PlaceOrderResponse.success(save.getId());
    }

    private Set<Book> updateBookAvailability(Set<OrderItem> items) {
          return items.stream().map(orderItem -> {
              Book book = orderItem.getBook();
              book.setAvailable(book.getAvailable() - orderItem.getQuantity());
              return book;
          }).collect(Collectors.toSet());
    }

    private OrderItem toOrderItem(OrderItemCommand command) {
        int quantity = command.getQuantity();
        Book book = bookJpaRepository.getOne(command.getBookId());
        if (book.getAvailable() >= quantity) {
            return new OrderItem(book, quantity);
        } else {
            throw new IllegalArgumentException("To many copies of book " + book.getTitle() + " requested "
                    + quantity + " of " + book.getAvailable() + " available");
        }
    }

    @Override
    public void deleteOrderById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public void updateOrderStatus(Long id, OrderStatus status) {
        repository.findById(id)
                  .ifPresent(order -> {
                      order.updateStatus(status);
                      repository.save(order);
                  });
    }
}
