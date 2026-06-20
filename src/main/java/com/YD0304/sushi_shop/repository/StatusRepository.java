package com.YD0304.sushi_shop.repository;

import com.YD0304.sushi_shop.entity.Status;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StatusRepository extends JpaRepository<Status, Integer> {
	Optional<Status> findByName(String name);

}
