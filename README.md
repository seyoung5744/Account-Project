# Account-Project
은행 계좌 관리 API 개발 실습

## 활용 기술
- **Framework** : Spring boot 2.6.x (JDK11)
- **Build** : Gradle
- **DataBase** : H2 Database
- JPA
- Redis
- Lombok
- Test
  - Mockito
  - Junit5

## 제공하는 기능(API)
#### 계좌(Account) 관련 기능
1. 계좌 생성
2. 계좌 해지
3. 계좌 확인
#### 거래(Transaction) 관련 기능
1. 잔액 사용(거래 생성)
2. 잔액 사용 취소(거래 취소)
3. 거래 확인
#### 사용자 관련 기능
구현 간소화를 위해 DB에 데이터 자동 입력하도록 구현


