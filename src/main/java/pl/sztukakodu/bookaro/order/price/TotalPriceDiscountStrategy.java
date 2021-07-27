package pl.sztukakodu.bookaro.order.price;

import pl.sztukakodu.bookaro.order.domain.Order;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class TotalPriceDiscountStrategy implements DiscountStrategy {

    private static final int CHEAPEST_BOOK_FOR_FREE_THRESHOLD = 400;
    private static final int CHEAPEST_BOOK_FOR_HALF_PRICE_THRESHOLD = 200;

    @Override
    public BigDecimal calculate(Order order) {
        if (isGreaterOrEqual(order, CHEAPEST_BOOK_FOR_FREE_THRESHOLD)) {
            return getCheapestBookPrice(order);
        } else if (isGreaterOrEqual(order, CHEAPEST_BOOK_FOR_HALF_PRICE_THRESHOLD)) {
            return getCheapestBookPrice(order).divide(new BigDecimal("2"), RoundingMode.HALF_UP);
        }

        return BigDecimal.ZERO;
    }

    private boolean isGreaterOrEqual(Order order, int value) {
        return order.getItemsPrice().compareTo(new BigDecimal(value)) >= 0;
    }

    private BigDecimal getCheapestBookPrice(Order order) {
        return order.getItems()
                .stream()
                .map(item -> item.getBook().getPrice())
                .sorted()
                .findFirst()
                .orElse(BigDecimal.ZERO);
    }
}
