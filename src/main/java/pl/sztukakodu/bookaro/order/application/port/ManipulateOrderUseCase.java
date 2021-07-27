package pl.sztukakodu.bookaro.order.application.port;

import lombok.*;
import pl.sztukakodu.bookaro.commons.Either;
import pl.sztukakodu.bookaro.order.domain.Delivery;
import pl.sztukakodu.bookaro.order.domain.OrderStatus;
import pl.sztukakodu.bookaro.order.domain.Recipient;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.Set;

public interface ManipulateOrderUseCase {
    PlaceOrderResponse placeOrder(PlaceOrderCommand command);

    void deleteOrderById(Long id);

    UpdateStatusResponse updateOrderStatus(UpdateStatusCommand command);

    @Builder
    @Value
    @AllArgsConstructor
    class PlaceOrderCommand {
        @Singular
        Set<OrderItemCommand> items;
        Recipient recipient;
        @Builder.Default
        Delivery delivery = Delivery.COURIER;
    }

    @Value
    class OrderItemCommand {
        Long bookId;
        int quantity;
    }

    @Value
    class UpdateStatusCommand {
        Long orderId;
        OrderStatus status;
        String email;
    }

    class PlaceOrderResponse extends Either<String, Long> {
        public PlaceOrderResponse(boolean success, String left, Long right) {
            super(success, left, right);
        }

        public static PlaceOrderResponse success(Long orderId) {
            return new PlaceOrderResponse(true, null, orderId);
        }

        public static PlaceOrderResponse failure(String error) {
            return new PlaceOrderResponse(false, error, null);
        }
    }

    class UpdateStatusResponse extends Either<String, OrderStatus> {
        public UpdateStatusResponse(boolean success, String left, OrderStatus status) {
            super(success, left, status);
        }

        public static UpdateStatusResponse success(OrderStatus status) {
            return new UpdateStatusResponse(true, null, status);
        }

        public static UpdateStatusResponse failure(String error) {
            return new UpdateStatusResponse(false, error, null);
        }
    }
}
