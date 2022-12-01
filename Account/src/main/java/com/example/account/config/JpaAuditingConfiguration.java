package com.example.account.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
// 해당 클래스가 Spring application이 시작될 때 JpaAuditing이 auto스캔이 되도록 한다.
// 그래서 DB에 데이터를 저장, 업데이트 할 때 Data.annotaion 이 위치한 값들을 자동으로 저장해준다.
public class JpaAuditingConfiguration {
}
