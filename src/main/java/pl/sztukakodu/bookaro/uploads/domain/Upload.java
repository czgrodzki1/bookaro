package pl.sztukakodu.bookaro.uploads.domain;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Upload {

    @Id
    @GeneratedValue
    private Long id;
    private byte[] file;
    private String contentType;
    private String filename;
    @CreatedDate
    private LocalDateTime createdAt;

    public Upload(String filename, byte[] file, String contentType) {
        this.filename = filename;
        this.file = file;
        this.contentType = contentType;
    }
}
