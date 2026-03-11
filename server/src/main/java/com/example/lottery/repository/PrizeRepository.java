package com.example.lottery.repository;

import com.example.lottery.domain.Prize;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrizeRepository extends JpaRepository<Prize, String> {
}
