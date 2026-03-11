package com.example.lottery.api;

import com.example.lottery.domain.DrawRequest;
import com.example.lottery.domain.DrawResult;
import com.example.lottery.domain.Prize;
import com.example.lottery.domain.Record;
import com.example.lottery.service.AuthService;
import com.example.lottery.service.LotteryService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api")
@Validated
public class LotteryController {
    private final LotteryService lotteryService;
    private final AuthService authService;

    public LotteryController(LotteryService lotteryService, AuthService authService) {
        this.lotteryService = lotteryService;
        this.authService = authService;
    }

    @GetMapping("/prizes")
    public List<Prize> prizes() {
        return lotteryService.getPrizes();
    }

    @GetMapping("/records")
    public List<Record> records() {
        return lotteryService.getRecords();
    }

    @PostMapping("/draw")
    public DrawResult draw(@RequestHeader("Authorization") String authorization, @Valid @RequestBody DrawRequest request) {
        authService.requireUser(authorization);
        return lotteryService.draw(request.getName().trim());
    }
}
