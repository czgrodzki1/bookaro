package pl.sztukakodu.bookaro.order.application;

import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.sztukakodu.bookaro.order.application.port.ManipulateOrderUseCase;
import pl.sztukakodu.bookaro.order.db.OrderJpaRepository;
import pl.sztukakodu.bookaro.order.domain.Order;
import pl.sztukakodu.bookaro.order.domain.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

@Component
@AllArgsConstructor
public class AbandonedOrdersJob {

    private OrderJpaRepository orderJpaRepository;
    private ManipulateOrderUseCase orderUseCase;

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void run() {
        LocalDateTime timeStamp = LocalDateTime.now().minusMinutes(1);
        List<Order> orders = orderJpaRepository.findOrderByStatusAndCreatedAtLessThanEqual(OrderStatus.NEW, timeStamp);
        orders.forEach(order -> orderUseCase.updateOrderStatus(order.getId(), OrderStatus.ABANDONED));

        orderJpaRepository.saveAll(orders);

    }
}
