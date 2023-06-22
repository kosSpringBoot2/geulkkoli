package com.geulkkoli.domain.user;

import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class ConfigDate {

    @Column(name = "sign_up_date", nullable = false, updatable = false)
    @CreatedDate
    private String signUpDate;

    // 추후 제거
    @Transient
    private LocalDate calendarData;

    @PrePersist
    public void onPrePersist() {
        if (calendarData != null) {
            this.signUpDate = calendarData.format(DateTimeFormatter.ofPattern("yyyy. MM. dd"));
        } else {
            this.signUpDate= LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy. MM. dd"));
        }
    }

    // 달력 잔디 심기용 달력 시작 기준 날짜 필요 (추후 제거)
    public void setCreatedAtForCalendarTest(LocalDate localDate) {
        calendarData = localDate;
    }
}
