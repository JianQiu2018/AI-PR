package com.example.lottery.config;

import com.example.lottery.domain.Prize;
import com.example.lottery.repository.PrizeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class DataInitializer {
    @Bean
    public CommandLineRunner seedPrizes(PrizeRepository prizeRepository) {
        return args -> {
            if (prizeRepository.count() > 0) {
                return;
            }
            List<Prize> prizes = Arrays.asList(
                    new Prize("p1", "一等奖 · MacBook Air", 2, "#FF6B6B"),
                    new Prize("p2", "二等奖 · iPad", 4, "#FFD93D"),
                    new Prize("p3", "三等奖 · 机械键盘", 8, "#6BCB77"),
                    new Prize("p4", "四等奖 · 蓝牙耳机", 12, "#4D96FF"),
                    new Prize("p5", "纪念奖 · 定制杯", 20, "#9D4EDD"),
                    new Prize("p6", "谢谢参与", 54, "#ADB5BD")
            );
            prizeRepository.saveAll(prizes);
        };
    }
}