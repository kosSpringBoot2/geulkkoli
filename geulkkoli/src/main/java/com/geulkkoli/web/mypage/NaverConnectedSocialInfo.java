package com.geulkkoli.web.mypage;

public class NaverConnectedSocialInfo implements ConnectSocialInfo {
    private String socialType;
    private Boolean connected;

    public NaverConnectedSocialInfo(String socialType, Boolean connected) {
        this.socialType = socialType;
        this.connected = connected;
    }

    @Override
    public String getSocialType() {
        return this.socialType;
    }

    @Override
    public Boolean isConnect() {
        return connected;
    }
}
