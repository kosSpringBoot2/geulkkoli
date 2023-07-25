package com.geulkkoli.web.social;

import com.geulkkoli.domain.user.User;
import lombok.Builder;
import org.hibernate.validator.constraints.Length;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.validation.constraints.*;

public class SocialSignUpDto {
    @NotEmpty
    private String userName;

    @NotEmpty
    @Length(min = 8, max = 20)
    @Pattern(regexp = "(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[*\\W])(?=\\S+$).{8,20}")
    private String password;

    @NotEmpty
    private String verifyPassword;

    @NotEmpty
    @Size(min = 2, max = 8)
    @Pattern(regexp = "^[a-zA-Z0-9가-힣]*$")
    private String nickName;

    @NotEmpty
    @Email
    private String email;

    @NotEmpty
    @Size(min = 10, max = 11)
    @Pattern(regexp = "^[*\\d]*$")
    private String phoneNo;

    @NotEmpty
    private String gender;

    @NotEmpty
    private String authorizationServerId;

    @NotEmpty
    private String clientregistrationName;

    @Builder
    public SocialSignUpDto(String userName, String password, String verifyPassword, String nickName, String email, String phoneNo, String gender, String authorizationServerId, String clientregistrationName) {
        this.userName = userName;
        this.password = password;
        this.verifyPassword = verifyPassword;
        this.nickName = nickName;
        this.email = email;
        this.phoneNo = phoneNo;
        this.gender = gender;
        this.authorizationServerId=authorizationServerId;
        this.clientregistrationName = clientregistrationName;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getVerifyPassword() {
        return verifyPassword;
    }

    public String getNickName() {
        return nickName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public String getGender() {
        return gender;
    }

    public String getAuthorizationServerId() {
        return authorizationServerId;
    }

    public String getClientregistrationName() {
        return clientregistrationName;
    }

    @Override
    public String toString() {
        return "SocialSignUpDto{" +
                "userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", verifyPassword='" + verifyPassword + '\'' +
                ", nickName='" + nickName + '\'' +
                ", email='" + email + '\'' +
                ", phoneNo='" + phoneNo + '\'' +
                ", gender='" + gender + '\'' +
                '}';
    }

    public User toEntity(PasswordEncoder passwordEncoder) {
        return User.builder()
                .userName(userName)
                .nickName(nickName)
                .password(passwordEncoder.encode(password))
                .email(email)
                .phoneNo(phoneNo)
                .gender(gender)
                .build();
    }

}

