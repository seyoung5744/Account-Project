package com.example.account.domain;

import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;

// userId는 프로젝트에서 다루는 데이터가 아니기 때문에 임시로 클래스 생성
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
// createdAt, updatedAt 자동 생성을 위한 어노테이션
@EntityListeners(AuditingEntityListener.class)
public class AccountUser extends BaseEntity {
    private String name; // 사용자 이름
}
