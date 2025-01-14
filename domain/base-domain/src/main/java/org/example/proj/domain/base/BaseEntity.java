package org.example.proj.domain.base;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * @author rival
 * @since 2024-12-02
 */



@MappedSuperclass
@EntityListeners(value= AuditingEntityListener.class)
@Getter
@Setter
@ToString
public abstract class BaseEntity {

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column
    private Instant updatedAt;

    @Column(columnDefinition = "timestamp")
    private Instant deletedAt;





    @Column
    private boolean deleted = false;

    public void softDelete(){
        if(!deleted) {
            deleted = true;
            deletedAt = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant();
        }
    }
}
