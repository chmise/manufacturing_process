package com.u1mobis.dashboard_backend.controller;

import com.u1mobis.dashboard_backend.dto.RobotDto;
import com.u1mobis.dashboard_backend.service.RobotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class RobotController {
    
    private final RobotService robotService;
    
    @GetMapping("/robots/{robotId}")
    public ResponseEntity<RobotDto> getRobotData(@PathVariable String companyName, @PathVariable String robotId) {
        log.info("로봇 데이터 요청 - 회사: {}, 로봇ID: {}", companyName, robotId);
        RobotDto robot = robotService.getRobotDataByCompany(companyName, robotId);
        return ResponseEntity.ok(robot);
    }
    
    @GetMapping("/robots")
    public ResponseEntity<List<RobotDto>> getAllRobots() {
        List<RobotDto> robots = robotService.getAllRobots();
        return ResponseEntity.ok(robots);
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Robot API is running!");
    }
}