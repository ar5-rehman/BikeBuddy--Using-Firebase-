package com.techo.bikebuddy.Models;

public class GroupPojo {

    String groupTitle;
    String joinedUsers;
    String groupPic;
    String description;
    String messages;
    String likes;

    public String getMessages() {
        return messages;
    }

    public void setMessages(String messages) {
        this.messages = messages;
    }

    public String getLikes() {
        return likes;
    }

    public void setLikes(String likes) {
        this.likes = likes;
    }

    public long getGroupUsers() {
        return groupUsers;
    }

    public void setGroupUsers(long groupUsers) {
        this.groupUsers = groupUsers;
    }

    long groupUsers;

    public GroupPojo(String groupTitle, long groupUsers, String groupPic, String description) {
        this.groupTitle = groupTitle;
        this.groupUsers = groupUsers;
        this.groupPic = groupPic;
        this.description = description;
    }

    public GroupPojo(){

    }

    public String getJoinedUsers() {
        return joinedUsers;
    }

    public void setJoinedUsers(String joinedUsers) {
        this.joinedUsers = joinedUsers;
    }

    public String getGroupPic() {
        return groupPic;
    }

    public void setGroupPic(String groupPic) {
        this.groupPic = groupPic;
    }

    public String getGroupTitle() {
        return groupTitle;
    }

    public void setGroupTitle(String groupTitle) {
        this.groupTitle = groupTitle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
