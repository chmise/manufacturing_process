package com.u1mobis.dashboard_backend.interfaces.equipment;

import com.u1mobis.dashboard_backend.application.equipment.EquipmentApplicationService;
import com.u1mobis.dashboard_backend.domain.company.model.CompanyId;
import com.u1mobis.dashboard_backend.domain.equipment.model.EquipmentId;
import com.u1mobis.dashboard_backend.domain.manufacturing.model.ProductionLineId;
import com.u1mobis.dashboard_backend.interfaces.equipment.dto.RobotResponse;
import com.u1mobis.dashboard_backend.interfaces.equipment.dto.ConveyorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@RestController
@RequestMapping("/api/{companyName}/equipment")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
@RequiredArgsConstructor
@Slf4j
public class EquipmentController {
    
    private final EquipmentApplicationService equipmentApplicationService;
    
    @GetMapping("/robots")
    public ResponseEntity<List<RobotResponse>> getRobots(
            @PathVariable String companyName,
            @RequestParam(required = false) Long lineId) {
        
        log.info("로봇 목록 조회 - 회사: {}, 라인: {}", companyName, lineId);
        
        CompanyId companyId = new CompanyId("1"); // TODO: get actual company ID from companyName
        
        var robots = equipmentApplicationService.findRobotsByCompany(companyId);
        
        return ResponseEntity.ok(
            robots.stream()
                .map(RobotResponse::from)
                .toList()
        );
    }
    
    @PostMapping("/robots/{robotId}/start")
    public ResponseEntity<Void> startRobot(
            @PathVariable String companyName,
            @PathVariable String robotId) {
        
        log.info("로봇 시작 - 회사: {}, 로봇: {}", companyName, robotId);
        
        EquipmentId equipmentId = new EquipmentId(robotId);
        equipmentApplicationService.startRobot(equipmentId);
        
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/robots/{robotId}/stop")
    public ResponseEntity<Void> stopRobot(
            @PathVariable String companyName,
            @PathVariable String robotId,
            @RequestParam(defaultValue = "Manual stop") String reason) {
        
        log.info("로봇 정지 - 회사: {}, 로봇: {}, 사유: {}", companyName, robotId, reason);
        
        EquipmentId equipmentId = new EquipmentId(robotId);
        equipmentApplicationService.stopRobot(equipmentId, reason);
        
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/conveyors")
    public ResponseEntity<List<ConveyorResponse>> getConveyors(
            @PathVariable String companyName,
            @RequestParam(required = false) Long lineId) {
        
        log.info("컨베이어 목록 조회 - 회사: {}, 라인: {}", companyName, lineId);
        
        CompanyId companyId = new CompanyId("1"); // TODO: get actual company ID from companyName
        
        var conveyors = equipmentApplicationService.findConveyorsByCompany(companyId);
        
        return ResponseEntity.ok(
            conveyors.stream()
                .map(ConveyorResponse::from)
                .toList()
        );
    }
    
    @PostMapping("/conveyors/{conveyorId}/start")
    public ResponseEntity<Void> startConveyor(
            @PathVariable String companyName,
            @PathVariable String conveyorId,
            @RequestParam(defaultValue = "Manual start") String reason) {
        
        log.info("컨베이어 시작 - 회사: {}, 컨베이어: {}, 사유: {}", companyName, conveyorId, reason);
        
        EquipmentId equipmentId = new EquipmentId(conveyorId);
        equipmentApplicationService.startConveyor(equipmentId, reason);
        
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/conveyors/{conveyorId}/stop")
    public ResponseEntity<Void> stopConveyor(
            @PathVariable String companyName,
            @PathVariable String conveyorId,
            @RequestParam(defaultValue = "Manual stop") String reason) {
        
        log.info("컨베이어 정지 - 회사: {}, 컨베이어: {}, 사유: {}", companyName, conveyorId, reason);
        
        EquipmentId equipmentId = new EquipmentId(conveyorId);
        equipmentApplicationService.stopConveyor(equipmentId, reason);
        
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/conveyors/{conveyorId}/speed")
    public ResponseEntity<Void> changeConveyorSpeed(
            @PathVariable String companyName,
            @PathVariable String conveyorId,
            @RequestParam double speed) {
        
        log.info("컨베이어 속도 변경 - 회사: {}, 컨베이어: {}, 속도: {}", companyName, conveyorId, speed);
        
        EquipmentId equipmentId = new EquipmentId(conveyorId);
        equipmentApplicationService.changeConveyorSpeed(equipmentId, speed);
        
        return ResponseEntity.ok().build();
    }
}