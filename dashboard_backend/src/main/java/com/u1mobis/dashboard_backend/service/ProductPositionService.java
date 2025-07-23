package com.u1mobis.dashboard_backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.u1mobis.dashboard_backend.entity.ProductPosition;
import com.u1mobis.dashboard_backend.repository.ProductPositionRepository;

@Service
@Transactional
public class ProductPositionService {
    
    @Autowired
    private ProductPositionRepository productPositionRepository;
    
    public List<ProductPosition> getAllProductPositions(Long companyId) {
        return productPositionRepository.findLatestByCompanyId(companyId);
    }
    
    public Optional<ProductPosition> getProductPosition(String productId, Long companyId) {
        return productPositionRepository.findByProductIdAndCompanyId(productId, companyId);
    }
    
    public Optional<ProductPosition> getLatestProductPosition(String productId) {
        return productPositionRepository.findLatestByProductId(productId);
    }
    
    public List<ProductPosition> getProductsByStation(String stationCode, Long companyId) {
        return productPositionRepository.findByCompanyIdAndStationCode(companyId, stationCode);
    }
    
    public ProductPosition updateProductPosition(String productId, Long companyId, String stationCode,
                                               Double xPosition, Double yPosition, Double zPosition, String status) {
        ProductPosition productPosition = ProductPosition.builder()
                .productId(productId)
                .companyId(companyId)
                .stationCode(stationCode)
                .xPosition(xPosition)
                .yPosition(yPosition)
                .zPosition(zPosition)
                .status(status)
                .timestamp(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        return productPositionRepository.save(productPosition);
    }
    
    public List<ProductPosition> getRecentProductPositions(Long companyId, int hours) {
        LocalDateTime fromTime = LocalDateTime.now().minusHours(hours);
        return productPositionRepository.findByCompanyIdAndTimestampAfter(companyId, fromTime);
    }
    
    public List<ProductPosition> getProductsByLine(String lineCode, Long companyId) {
        return productPositionRepository.findByCompanyIdAndStationPrefix(companyId, lineCode);
    }
    
    // 디지털 트윈용 상세 정보 조회
    public ProductPositionInfo getProductPositionInfo(String productId, Long companyId) {
        Optional<ProductPosition> positionOpt = productPositionRepository.findByProductIdAndCompanyId(productId, companyId);
        
        if (positionOpt.isEmpty()) {
            return null;
        }
        
        ProductPosition position = positionOpt.get();
        
        return ProductPositionInfo.builder()
                .productId(position.getProductId())
                .stationCode(position.getStationCode())
                .stationName(getStationName(position.getStationCode()))
                .xPosition(position.getXPosition())
                .yPosition(position.getYPosition())
                .zPosition(position.getZPosition())
                .status(position.getStatus())
                .statusText(getStatusText(position.getStatus()))
                .lastUpdate(position.getTimestamp())
                .build();
    }
    
    private String getStationName(String stationCode) {
        if (stationCode == null) return "알수없음";
        
        switch (stationCode) {
            case "A01": return "도어 탈거";
            case "A02": return "와이어링";
            case "A03": return "헤드라이너";
            case "A04": return "크래쉬패드";
            case "B01": return "연료탱크";
            case "B02": return "샤시 메리지";
            case "B03": return "머플러";
            case "C01": return "FEM";
            case "C02": return "글라스";
            case "C03": return "시트";
            case "C04": return "범퍼";
            case "C05": return "타이어";
            case "D01": return "휠 얼라이언트";
            case "D02": return "헤드램프";
            case "D03": return "수밀검사";
            default: return stationCode;
        }
    }
    
    private String getStatusText(String status) {
        if (status == null) return "알수없음";
        
        switch (status.toUpperCase()) {
            case "PROCESSING": return "가공중";
            case "WAITING": return "대기중";
            case "COMPLETED": return "완료";
            case "ERROR": return "오류";
            default: return status;
        }
    }
    
    // 내부 클래스로 위치 정보 DTO 정의
    public static class ProductPositionInfo {
        private String productId;
        private String stationCode;
        private String stationName;
        private Double xPosition;
        private Double yPosition;
        private Double zPosition;
        private String status;
        private String statusText;
        private LocalDateTime lastUpdate;
        
        // Builder 패턴
        public static ProductPositionInfoBuilder builder() {
            return new ProductPositionInfoBuilder();
        }
        
        public static class ProductPositionInfoBuilder {
            private String productId;
            private String stationCode;
            private String stationName;
            private Double xPosition;
            private Double yPosition;
            private Double zPosition;
            private String status;
            private String statusText;
            private LocalDateTime lastUpdate;
            
            public ProductPositionInfoBuilder productId(String productId) {
                this.productId = productId;
                return this;
            }
            
            public ProductPositionInfoBuilder stationCode(String stationCode) {
                this.stationCode = stationCode;
                return this;
            }
            
            public ProductPositionInfoBuilder stationName(String stationName) {
                this.stationName = stationName;
                return this;
            }
            
            public ProductPositionInfoBuilder xPosition(Double xPosition) {
                this.xPosition = xPosition;
                return this;
            }
            
            public ProductPositionInfoBuilder yPosition(Double yPosition) {
                this.yPosition = yPosition;
                return this;
            }
            
            public ProductPositionInfoBuilder zPosition(Double zPosition) {
                this.zPosition = zPosition;
                return this;
            }
            
            public ProductPositionInfoBuilder status(String status) {
                this.status = status;
                return this;
            }
            
            public ProductPositionInfoBuilder statusText(String statusText) {
                this.statusText = statusText;
                return this;
            }
            
            public ProductPositionInfoBuilder lastUpdate(LocalDateTime lastUpdate) {
                this.lastUpdate = lastUpdate;
                return this;
            }
            
            public ProductPositionInfo build() {
                return new ProductPositionInfo(productId, stationCode, stationName, 
                                             xPosition, yPosition, zPosition, status, statusText, lastUpdate);
            }
        }
        
        // 생성자 및 getter
        public ProductPositionInfo(String productId, String stationCode, String stationName,
                                  Double xPosition, Double yPosition, Double zPosition,
                                  String status, String statusText, LocalDateTime lastUpdate) {
            this.productId = productId;
            this.stationCode = stationCode;
            this.stationName = stationName;
            this.xPosition = xPosition;
            this.yPosition = yPosition;
            this.zPosition = zPosition;
            this.status = status;
            this.statusText = statusText;
            this.lastUpdate = lastUpdate;
        }
        
        // Getters
        public String getProductId() { return productId; }
        public String getStationCode() { return stationCode; }
        public String getStationName() { return stationName; }
        public Double getXPosition() { return xPosition; }
        public Double getYPosition() { return yPosition; }
        public Double getZPosition() { return zPosition; }
        public String getStatus() { return status; }
        public String getStatusText() { return statusText; }
        public LocalDateTime getLastUpdate() { return lastUpdate; }
    }
}