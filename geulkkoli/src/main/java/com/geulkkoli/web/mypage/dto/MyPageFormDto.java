package com.geulkkoli.web.mypage.dto;

import lombok.Getter;
import lombok.ToString;

import javax.validation.constraints.NotEmpty;

@Getter
@ToString
public class MyPageFormDto {

    @NotEmpty
    private String userName;

    @NotEmpty
    private String email;

    public MyPageFormDto(String userName, String email) {
        this.userName = userName;
        this.email = email;
    }

}
