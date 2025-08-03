package com.u1mobis.dashboard_backend.application.equipment;

import com.u1mobis.dashboard_backend.domain.company.model.CompanyId;
import com.u1mobis.dashboard_backend.domain.equipment.model.*;
import com.u1mobis.dashboard_backend.domain.equipment.repository.RobotRepository;
import com.u1mobis.dashboard_backend.domain.equipment.repository.ConveyorRepository;
import com.u1mobis.dashboard_backend.domain.equipment.repository.WorkStationRepository;
import com.u1mobis.dashboard_backend.domain.manufacturing.model.Position;
import com.u1mobis.dashboard_backend.domain.manufacturing.model.ProductionLineId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EquipmentApplicationService {
    
    private final RobotRepository robotRepository;
    private final ConveyorRepository conveyorRepository;
    private final WorkStationRepository workStationRepository;
    
    public EquipmentApplicationService(RobotRepository robotRepository,
                                     ConveyorRepository conveyorRepository,
                                     WorkStationRepository workStationRepository) {
        this.robotRepository = robotRepository;
        this.conveyorRepository = conveyorRepository;
        this.workStationRepository = workStationRepository;
    }
    
    // Robot Operations
    public Robot createRobot(CompanyId companyId, ProductionLineId lineId, String name,
                           RobotType type, Position position) {
        EquipmentId robotId = EquipmentId.generate();
        Robot robot = new Robot(robotId, name, companyId, lineId, type, position);
        return robotRepository.save(robot);
    }
    
    public void startRobot(EquipmentId robotId) {
        Robot robot = findRobotById(robotId);
        robot.start();
        robotRepository.save(robot);
    }
    
    public void stopRobot(EquipmentId robotId, String reason) {
        Robot robot = findRobotById(robotId);
        robot.stop(reason);
        robotRepository.save(robot);
    }
    
    public void emergencyStopRobot(EquipmentId robotId, String reason) {
        Robot robot = findRobotById(robotId);
        robot.emergencyStop(reason);
        robotRepository.save(robot);
    }
    
    public void moveRobot(EquipmentId robotId, Position newPosition) {
        Robot robot = findRobotById(robotId);
        robot.moveTo(newPosition);
        robotRepository.save(robot);
    }
    
    public void completeRobotWork(EquipmentId robotId, String workType) {
        Robot robot = findRobotById(robotId);
        robot.completeWork(workType);
        robotRepository.save(robot);
    }
    
    public void updateRobotConfiguration(EquipmentId robotId, double cycleTime, 
                                       int motorSpeed, boolean ledEnabled) {
        Robot robot = findRobotById(robotId);
        robot.updateConfiguration(cycleTime, motorSpeed, ledEnabled);
        robotRepository.save(robot);
    }
    
    // Conveyor Operations
    public Conveyor createConveyor(CompanyId companyId, ProductionLineId lineId, String name) {
        EquipmentId conveyorId = EquipmentId.generate();
        Conveyor conveyor = new Conveyor(conveyorId, name, companyId, lineId);
        return conveyorRepository.save(conveyor);
    }
    
    public void startConveyor(EquipmentId conveyorId, String reason) {
        Conveyor conveyor = findConveyorById(conveyorId);
        conveyor.start(reason);
        conveyorRepository.save(conveyor);
    }
    
    public void stopConveyor(EquipmentId conveyorId, String reason) {
        Conveyor conveyor = findConveyorById(conveyorId);
        conveyor.stop(reason);
        conveyorRepository.save(conveyor);
    }
    
    public void emergencyStopConveyor(EquipmentId conveyorId, String reason) {
        Conveyor conveyor = findConveyorById(conveyorId);
        conveyor.emergencyStop(reason);
        conveyorRepository.save(conveyor);
    }
    
    public void changeConveyorSpeed(EquipmentId conveyorId, double speedValue) {
        Conveyor conveyor = findConveyorById(conveyorId);
        conveyor.changeSpeed(speedValue);
        conveyorRepository.save(conveyor);
    }
    
    public void changeConveyorDirection(EquipmentId conveyorId, ConveyorDirection direction) {
        Conveyor conveyor = findConveyorById(conveyorId);
        conveyor.changeDirection(direction);
        conveyorRepository.save(conveyor);
    }
    
    // WorkStation Operations
    public WorkStation createWorkStation(CompanyId companyId, ProductionLineId lineId, String name,
                                       WorkStationType type, int queueCapacity, double cycleTime) {
        EquipmentId stationId = EquipmentId.generate();
        WorkStation station = new WorkStation(stationId, name, companyId, lineId, type, queueCapacity, cycleTime);
        return workStationRepository.save(station);
    }
    
    public void startWorkStation(EquipmentId stationId) {
        WorkStation station = findWorkStationById(stationId);
        station.start();
        workStationRepository.save(station);
    }
    
    public void stopWorkStation(EquipmentId stationId, String reason) {
        WorkStation station = findWorkStationById(stationId);
        station.stop(reason);
        workStationRepository.save(station);
    }
    
    public void processItemAtStation(EquipmentId stationId) {
        WorkStation station = findWorkStationById(stationId);
        station.processItem();
        workStationRepository.save(station);
    }
    
    public void addItemToStationQueue(EquipmentId stationId) {
        WorkStation station = findWorkStationById(stationId);
        station.addItemToQueue();
        workStationRepository.save(station);
    }
    
    // Query methods
    @Transactional(readOnly = true)
    public List<Robot> findRobotsByCompany(CompanyId companyId) {
        return robotRepository.findByCompanyId(companyId);
    }
    
    @Transactional(readOnly = true)
    public List<Robot> findRobotsByProductionLine(ProductionLineId lineId) {
        return robotRepository.findByLineId(lineId);
    }
    
    @Transactional(readOnly = true)
    public List<Conveyor> findConveyorsByCompany(CompanyId companyId) {
        return conveyorRepository.findByCompanyId(companyId);
    }
    
    @Transactional(readOnly = true)
    public List<WorkStation> findWorkStationsByCompany(CompanyId companyId) {
        return workStationRepository.findByCompanyId(companyId);
    }
    
    @Transactional(readOnly = true)
    public List<Robot> findOperationalRobots(CompanyId companyId) {
        return robotRepository.findByCompanyIdAndStatus(companyId, EquipmentStatus.OPERATIONAL);
    }
    
    // Private helper methods
    private Robot findRobotById(EquipmentId robotId) {
        return robotRepository.findById(robotId)
            .orElseThrow(() -> new IllegalArgumentException("Robot not found: " + robotId.getValue()));
    }
    
    private Conveyor findConveyorById(EquipmentId conveyorId) {
        return conveyorRepository.findById(conveyorId)
            .orElseThrow(() -> new IllegalArgumentException("Conveyor not found: " + conveyorId.getValue()));
    }
    
    private WorkStation findWorkStationById(EquipmentId stationId) {
        return workStationRepository.findById(stationId)
            .orElseThrow(() -> new IllegalArgumentException("Work station not found: " + stationId.getValue()));
    }

    // MQTT compatibility method
    public void saveConveyorStatus(String companyName, Long lineId, String command, String reason) {
        // TODO: Convert to domain objects and process conveyor status
        System.out.println("Conveyor status saved for " + companyName + " line " + lineId + ": " + command + " - " + reason);
    }
}