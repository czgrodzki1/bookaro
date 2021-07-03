package pl.sztukakodu.bookaro.jpa;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Version;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.util.UUID;

@Getter
@Setter
@MappedSuperclass
@EqualsAndHashCode(of = "uuid")
public abstract class BaseEntity {


    @Id
    @GeneratedValue
    private Long id;
    @Version
    private Long version;


    private String uuid = UUID.randomUUID().toString();

}
