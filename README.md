# BaegaePro Android WebView API

## JavaScript WiFi API

### WiFi 스캔
```javascript
const result = Android.scanWiFi();
const networks = JSON.parse(result);
```

### WiFi 일반 연결
```javascript
const result = Android.connectToWiFi("SSID", "password");
const response = JSON.parse(result);
```

### WiFi 보조 연결 (Android 10+)
```javascript
const result = Android.connectToWiFiAsSecondary("SSID", "password");
const response = JSON.parse(result);
```

## JavaScript 진동 API

### 단일 진동
```javascript
// 기본 진동
Android.vibrateOnce(500, false); // 500ms

// Fade 효과 진동 (지원 기기)
Android.vibrateOnce(500, true);
```

### 패턴 진동
```javascript
// 기본 패턴
Android.vibrate("0,200,100,200", false);

// Fade 효과 패턴
Android.vibrate("0,300,100,300", true);
```

### 진동 중지
```javascript
Android.stopVibration();
```

## 주요 특징

- 안드로이드 Q (API 29) 이후 새로운 WiFi API 지원
- 보조 WiFi 연결 (Android Auto 등에서 사용)
- 진동 Fade in/out 효과 (지원 기기에서만)
- 런타임 권한 자동 요청

## 권한

앱에서 자동으로 다음 권한들을 요청합니다:
- ACCESS_WIFI_STATE
- CHANGE_WIFI_STATE  
- ACCESS_FINE_LOCATION
- ACCESS_COARSE_LOCATION
- VIBRATE

## 파일 구조

```
app/src/main/
├── java/com/baegaepro/kr/baegaeproapplication/
│   ├── MainActivity.kt          - 앱 진입점
│   ├── WebViewActivity.kt       - 웹뷰 액티비티
│   ├── WebAppInterface.kt       - JS 브리지
│   ├── WiFiManager.kt          - WiFi 관리
│   └── VibrationManager.kt     - 진동 관리
├── res/layout/
│   └── activity_webview.xml    - 웹뷰 레이아웃
└── assets/
    └── index.html              - 샘플 HTML
```

## 빌드 및 테스트

1. 프로젝트를 Android Studio에서 열기
2. Sync Project with Gradle Files
3. 실제 기기에서 테스트 (권한 및 WiFi/진동 기능)

## 추가 개발 사항

- 웹에서 axios로 HTTP 요청 처리
- 필요시 추가 네이티브 API 확장
