package pl.sztukakodu.bookaro.order.application;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.sztukakodu.bookaro.order.application.port.ManipulateOrderUseCase;
import pl.sztukakodu.bookaro.order.db.OrderJpaRepository;
import pl.sztukakodu.bookaro.order.domain.Order;
import pl.sztukakodu.bookaro.order.domain.OrderStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
public class AbandonedOrdersJob {

    private final OrderJpaRepository orderJpaRepository;
    private final ManipulateOrderUseCase orderUseCase;
    private final OrdersProperties properties;

    @Scheduled(cron = "${app.orders.abandon-cron}")
    @Transactional
    public void run() {
        Duration paymentPeriod = properties.getPaymentPeriod();
        LocalDateTime timeStamp = LocalDateTime.now().minus(paymentPeriod);
        List<Order> orders = orderJpaRepository.findOrderByStatusAndCreatedAtLessThanEqual(OrderStatus.NEW, timeStamp);
        log.info("Found orders to be marked as abandoned: {} ", orders.size());
        orders.forEach(order -> orderUseCase.updateOrderStatus(order.getId(), OrderStatus.ABANDONED));

        orderJpaRepository.saveAll(orders);

    }
}
