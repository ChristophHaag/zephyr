package com.texasgamer.zephyr.model;

import org.json.JSONObject;

public class Token {

    private String mName, mAvatar, mRoom, mToken;

    public Token(String name, String avatar, String room, String token) {
        mName = name;
        mAvatar = avatar;
        mRoom = room;
        mToken = token;
    }

    public String getName() {
        return mName;
    }

    public String getAvatar() {
        return mAvatar;
    }

    public String getRoom() {
        return mRoom;
    }

    public String getToken() {
        return mToken;
    }

    public void updateUser(String name, String avatar) {
        mName = name;
        mAvatar = avatar;
    }

    @Override
    public String toString() {
        JSONObject tokenJson = new JSONObject();

        try {
            tokenJson.put("name", mName);
            tokenJson.put("avatar", mAvatar);
            tokenJson.put("room", mRoom);
            tokenJson.put("token", mToken);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return tokenJson.toString();
    }
}
