package com.rounak.consistent_hashing.repository;

import com.rounak.consistent_hashing.model.DataEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DataRepository extends JpaRepository<DataEntity, String> {
}