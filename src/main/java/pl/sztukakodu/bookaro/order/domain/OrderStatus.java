package pl.sztukakodu.bookaro.order.domain;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Optional;

public enum OrderStatus {
    NEW {
        @Override
        public UpdateStatusResult updateStatus(OrderStatus status) {
            return switch (status) {
                case PAID -> new UpdateStatusResult(PAID, false);
                case CANCELED -> new UpdateStatusResult(CANCELED, true);
                case ABANDONED -> new UpdateStatusResult(ABANDONED, true);
                default -> super.updateStatus(status);
            };
        }

    },
    PAID {
        @Override
        public UpdateStatusResult updateStatus(OrderStatus status) {
            if (status == SHIPPED) {
                return new UpdateStatusResult(SHIPPED, false);
            }
            return super.updateStatus(status);
        }
    },
    CANCELED,
    ABANDONED,
    SHIPPED;

    public static Optional<OrderStatus> parseString(String value) {
        return Arrays.stream(values())
                     .filter(it -> StringUtils.equalsIgnoreCase(it.name(), value))
                     .findFirst();
    }

    public UpdateStatusResult updateStatus(OrderStatus status) {
        throw new IllegalArgumentException("Unable to mark " + this.name() + " order as " + status.name());
    }
}
