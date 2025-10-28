package com.szakdolgozat.scheduler.timetable.domain;

public class Room {

    private String roomName;

    public Room() {
    }

    public Room(String roomName) {
        this.roomName = roomName;
    }

    public String getRoomName() {
        return roomName;
    }

    @Override
    public String toString() {
        return roomName;
    }
}
