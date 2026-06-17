package com.YD0304.sushi_shop.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "sushi")
public class Sushi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", length = 30)
    private String name;

    @Column(name = "time_to_make", nullable = true)
    private Integer timeToMake;

    public Sushi() {
    }

    public Sushi(String name, Integer timeToMake) {
        this.name = name;
        this.timeToMake = timeToMake;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getTimeToMake() {
        return timeToMake;
    }

    public void setTimeToMake(Integer timeToMake) {
        this.timeToMake = timeToMake;
    }
}
