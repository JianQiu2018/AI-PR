package com.example.lottery.domain;

public class DrawResult {
    private final Prize prize;
    private final Record record;

    public DrawResult(Prize prize, Record record) {
        this.prize = prize;
        this.record = record;
    }

    public Prize getPrize() {
        return prize;
    }

    public Record getRecord() {
        return record;
    }
}
