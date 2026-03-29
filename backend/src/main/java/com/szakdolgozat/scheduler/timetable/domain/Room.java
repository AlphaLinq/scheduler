package com.szakdolgozat.scheduler.timetable.domain;

import jakarta.validation.constraints.NotBlank;

public class Room {

    @NotBlank
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
