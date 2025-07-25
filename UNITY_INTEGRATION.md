# Unity 디지털 트윈 연동 가이드

현재 Unity 프로젝트에는 **ClickableObject.cs** 스크립트가 구현되어 있어, 이를 활용한 연동 방법입니다.

## 기존 Unity 스크립트 활용

### ClickableObject.cs 사용법

현재 프로젝트의 `ClickableObject.cs`가 자동으로 React와 연동됩니다:

```csharp
// ClickableObject 컴포넌트 설정
ClickableObject clickable = gameObject.AddComponent<ClickableObject>();
clickable.objectId = "ROBOT_001";      // 고유 ID
clickable.objectType = "robot";        // 타입: robot, station, product
```

### 지원되는 objectType

1. **robot** - 로봇 오브젝트
   - objectId 예시: `"ROBOT_001"`, `"ARM_ROBOT_A01_001"`
   - React에서 robotId로 변환하여 API 호출

2. **station** - 공정 오브젝트  
   - objectId 예시: `"STATION_A01"`, `"ASSEMBLY_A01"`
   - React에서 stationCode 추출하여 API 호출

3. **product** - 제품/차량 오브젝트
   - objectId 예시: `"CAR_001"`, `"CAR_005"`
   - React에서 `A01_PROD_XXX` 형태로 변환하여 API 호출

## 차량(제품) 자동 설정

**CarSpawner.cs**가 자동으로 차량에 ClickableObject를 추가합니다:

```csharp
// CarSpawner.cs에서 자동 처리
clickable.objectId = $"CAR_{carCounter:000}";  // CAR_001, CAR_002, ...
clickable.objectType = "product";
```

## React 연동 테스트

브라우저 개발자 도구에서 테스트 가능:

```javascript
// Unity ClickableObject 시뮬레이션
window.TestRobotClick();     // ROBOT_001 클릭 테스트
window.TestStationClick();   // STATION_A01 클릭 테스트  
window.TestProductClick();   // CAR_001 클릭 테스트
window.TestCarClick('CAR_005'); // 특정 차량 클릭 테스트
```

## React에서 Unity로 데이터 전송

```javascript
// React에서 Unity로 메시지 전송
window.SendToUnity("GameObjectName", "MethodName", "parameter");
```

## 사용 가능한 JavaScript 함수들

React 컴포넌트가 로드되면 다음 함수들이 `window` 객체에 등록됩니다:

- `window.OnRobotClicked(robotData)` - 로봇 클릭 처리
- `window.OnStationClicked(stationData)` - 공정 클릭 처리  
- `window.OnProductClicked(productData)` - 제품 클릭 처리
- `window.SendToUnity(gameObject, method, param)` - Unity로 메시지 전송

## 테스트 함수들

Unity 없이도 테스트할 수 있는 함수들:

```javascript
// 브라우저 콘솔에서 실행 가능
window.TestRobotClick();    // 로봇 클릭 테스트
window.TestStationClick();  // 공정 클릭 테스트  
window.TestProductClick();  // 제품 클릭 테스트
```

## Unity ClickableObject 데이터 형식

Unity에서 React로 전송되는 실제 데이터 형식:

```json
{
  "type": "objectClicked",
  "payload": {
    "objectId": "CAR_001",
    "objectType": "product"
  }
}
```

### React에서의 ID 변환

| Unity objectType | Unity objectId | React 변환 | API 호출 |
|------------------|----------------|-----------|----------|
| robot | `ROBOT_001` | robotId: 1 | `/digital-twin/robot-status/1` |
| station | `STATION_A01` | stationCode: A01 | `/digital-twin/station/A01/status` |  
| product | `CAR_001` | productId: A01_PROD_001 | `/digital-twin/product-position/A01_PROD_001` |

## 로봇과 공정 오브젝트 설정 가이드

Unity에서 로봇과 공정에 ClickableObject를 추가하는 방법:

### 로봇 오브젝트 설정
```csharp
// QuadDoorAssemblyStation의 로봇들에 ClickableObject 추가
void SetupRobotClickable(Transform robotTransform, int robotNumber)
{
    ClickableObject clickable = robotTransform.gameObject.AddComponent<ClickableObject>();
    clickable.objectId = $"ROBOT_{robotNumber:000}";
    clickable.objectType = "robot";
}
```

### 공정 오브젝트 설정  
```csharp
// 공정 스테이션에 ClickableObject 추가
void SetupStationClickable(GameObject stationObject, string stationCode)
{
    ClickableObject clickable = stationObject.AddComponent<ClickableObject>();
    clickable.objectId = $"STATION_{stationCode}";
    clickable.objectType = "station";
}
```

## 주의사항

1. **자동 연동**: ClickableObject만 설정하면 React 오버레이가 자동으로 나타남
2. **ID 규칙**: objectId는 고유해야 하며, 일관된 명명 규칙 사용 권장  
3. **충돌 감지**: ClickableObject는 Raycast 방식으로 클릭을 감지

## API 연동

오버레이가 표시될 때 자동으로 다음 API를 호출하여 실시간 데이터를 가져옵니다:

- `GET /api/digital-twin/robot-status/{robotId}`
- `GET /api/digital-twin/station/{stationCode}/status`  
- `GET /api/digital-twin/product-position/{productId}`