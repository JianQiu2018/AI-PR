package com.example.lottery.service;

import com.example.lottery.domain.DrawResult;
import com.example.lottery.domain.Prize;
import com.example.lottery.domain.Record;
import com.example.lottery.repository.PrizeRepository;
import com.example.lottery.repository.RecordRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class LotteryService {
    private final PrizeRepository prizeRepository;
    private final RecordRepository recordRepository;

    public LotteryService(PrizeRepository prizeRepository, RecordRepository recordRepository) {
        this.prizeRepository = prizeRepository;
        this.recordRepository = recordRepository;
    }

    public List<Prize> getPrizes() {
        return prizeRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    public List<Record> getRecords() {
        return recordRepository.findTop200ByOrderByTimeDesc();
    }

    @Transactional
    public DrawResult draw(String name) {
        List<Prize> prizes = getPrizes();
        Prize prize = pickPrize(prizes);
        Record record = new Record(name, prize.getId(), prize.getName(), Instant.now());
        recordRepository.save(record);
        return new DrawResult(prize, record);
    }

    private Prize pickPrize(List<Prize> prizes) {
        if (prizes == null || prizes.isEmpty()) {
            throw new IllegalStateException("baocuole");
        }
        int total = 0;
        for (Prize p : prizes) {
            total += p.getWeight();
        }
        int r = ThreadLocalRandom.current().nextInt(total);
        int cumulative = 0;
        for (Prize p : prizes) {
            cumulative += p.getWeight();
            if (r < cumulative) {
                return p;
            }
        }
        return prizes.get(prizes.size() - 1);
    }
}
