package com.YD0304.sushi_shop.repository;

import com.YD0304.sushi_shop.entity.SushiOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SushiOrderRepository extends JpaRepository<SushiOrder, Integer> {
}
