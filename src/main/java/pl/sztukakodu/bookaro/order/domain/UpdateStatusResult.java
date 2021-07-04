package pl.sztukakodu.bookaro.order.domain;

import lombok.Value;

@Value
public class UpdateStatusResult {

    private OrderStatus orderStatus;
    private boolean revoke;

    public static UpdateStatusResult ok(OrderStatus newStatus)  {
        return new UpdateStatusResult(newStatus, false);
    }

    public static UpdateStatusResult revoked(OrderStatus newStatus) {
        return new UpdateStatusResult(newStatus, true);

    }

}
