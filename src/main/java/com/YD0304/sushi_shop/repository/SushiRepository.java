package com.YD0304.sushi_shop.repository;

import com.YD0304.sushi_shop.entity.Sushi;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SushiRepository extends JpaRepository<Sushi, Integer> {
    Optional<Sushi> findByName(String name);
}
