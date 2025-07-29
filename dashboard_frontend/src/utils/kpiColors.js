/**
 * KPI 성과 구간별 색상 체계
 * 우수(90% 이상): 연초록
 * 양호(80-89%): 파랑  
 * 보통(70-79%): 노랑
 * 저조(60-69%): 주황
 * 미달(60% 미만): 빨강
 */

export const getKPIColor = (value, type = 'general') => {
  // KPI 타입별 임계값 설정
  const thresholds = {
    oee: { excellent: 85, good: 75, average: 65, poor: 60, critical: 50 },
    fty: { excellent: 90, good: 80, average: 70, poor: 60, critical: 50 },
    otd: { excellent: 95, good: 85, average: 75, poor: 60, critical: 50 },
    general: { excellent: 85, good: 75, average: 65, poor: 60, critical: 50 }
  };

  const threshold = thresholds[type] || thresholds.general;

  if (value >= threshold.excellent) {
    return {
      color: '#28a745',    // 연초록 (Bootstrap success)
      grade: '우수',
      level: 'excellent'
    };
  } else if (value >= threshold.good) {
    return {
      color: '#007bff',    // 파랑 (Bootstrap primary)
      grade: '양호',
      level: 'good'
    };
  } else if (value >= threshold.average) {
    return {
      color: '#ffc107',    // 노랑 (Bootstrap warning)
      grade: '보통',
      level: 'average'
    };
  } else if (value >= threshold.poor) {
    return {
      color: '#fd7e14',    // 주황 (Bootstrap orange)
      grade: '저조',
      level: 'poor'
    };
  } else if (value >= (threshold.critical || 50)) {
    return {
      color: '#dc3545',    // 빨강 (Bootstrap danger)
      grade: '미달',
      level: 'critical'
    };
  } else {
    return {
      color: '#6c757d',    // 회색 (Bootstrap secondary) - 심각한 수준
      grade: '심각',
      level: 'severe'
    };
  }
};

// 성과 구간별 색상 헥스 코드
export const KPI_COLORS = {
  excellent: '#28a745',  // 연초록
  good: '#007bff',       // 파랑
  average: '#ffc107',    // 노랑
  poor: '#fd7e14',       // 주황
  critical: '#dc3545',   // 빨강
  neutral: '#e9ecef'     // 회색 (배경용)
};

// 성과 구간별 텍스트
export const KPI_GRADES = {
  excellent: '우수',
  good: '양호', 
  average: '보통',
  poor: '저조',
  critical: '미달'
};