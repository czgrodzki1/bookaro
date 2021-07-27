package pl.sztukakodu.bookaro.order.price;

import pl.sztukakodu.bookaro.order.domain.Order;

import java.math.BigDecimal;

public class DeliveryDiscountStrategy implements DiscountStrategy {

    private static final BigDecimal FREE_DELIVERY_THRESHOLD = new BigDecimal("100");

    @Override
    public BigDecimal calculate(Order order) {
        if (order.getItemsPrice().compareTo(FREE_DELIVERY_THRESHOLD) >= 0 ) {
            return order.getDeliveryPrice();
        }

        return BigDecimal.ZERO;
    }
}
