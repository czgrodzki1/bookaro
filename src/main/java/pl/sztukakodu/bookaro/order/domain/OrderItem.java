package pl.sztukakodu.bookaro.order.domain;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.sztukakodu.bookaro.jpa.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class OrderItem extends BaseEntity {

    @Id
    @GeneratedValue
    private Long id;
    private Long bookId;
    private int quantity;

    public OrderItem(Long bookId, int quantity){
        this.bookId = bookId;
        this.quantity = quantity;
    }
}
