package com.YD0304.sushi_shop.repository;

import com.YD0304.sushi_shop.entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatusRepository extends JpaRepository<Status, Integer> {
	java.util.Optional<Status> findByName(String name);
}
