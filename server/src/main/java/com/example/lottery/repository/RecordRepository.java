package com.example.lottery.repository;

import com.example.lottery.domain.Record;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecordRepository extends JpaRepository<Record, Long> {
    List<Record> findTop200ByOrderByTimeDesc();
}
