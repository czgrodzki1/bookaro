package pl.sztukakodu.bookaro.order.price;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.sztukakodu.bookaro.order.domain.Order;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PriceService {

    private final List<DiscountStrategy> strategyList = List.of(
            new TotalPriceDiscountStrategy(),
            new DeliveryDiscountStrategy()
    );


    @Transactional
    public OrderPrice calculatePrice(Order order) {
        return new OrderPrice(
                order.getItemsPrice(),
                order.getDeliveryPrice(),
                discounts(order)
        );
    }

    private BigDecimal discounts(Order order) {
        return strategyList
                .stream()
                .map(strategyList -> strategyList.calculate(order))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

    }

}
