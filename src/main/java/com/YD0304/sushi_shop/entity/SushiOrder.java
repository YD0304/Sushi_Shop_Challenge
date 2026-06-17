package com.YD0304.sushi_shop.entity;

import jakarta.persistence.*;
import java.sql.Timestamp;
import java.time.Instant;

@Entity
@Table(name = "sushi_order")
public class SushiOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "status_id", nullable = false)
    private Status status;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sushi_id", nullable = false)
    private Sushi sushi;

    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt = Timestamp.from(Instant.now());

    @Column(name = "remaining_time", nullable = true)
    private Long remainingTime;

    public SushiOrder() {
    }

    public SushiOrder(Status status, Sushi sushi) {
        this.status = status;
        this.sushi = sushi;
        this.createdAt = Timestamp.from(Instant.now());
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Sushi getSushi() {
        return sushi;
    }

    public void setSushi(Sushi sushi) {
        this.sushi = sushi;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Long getRemainingTime() {
        return remainingTime;
    }

    public void setRemainingTime(Long remainingTime) {
        this.remainingTime = remainingTime;
    }

}
