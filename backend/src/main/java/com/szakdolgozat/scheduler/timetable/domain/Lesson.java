package com.szakdolgozat.scheduler.timetable.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.lookup.PlanningId;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

@PlanningEntity
public class Lesson {

        @Getter
        @PlanningId
        @NotNull
        private Long id;

        @Getter
        @NotBlank
        private String subject;

        @Getter
        @NotBlank
        private String teacher;

        @Getter
        @NotBlank
        private String studentGroup;

        @Setter
        @PlanningVariable
        private TimeSlot timeSlot;

        @Setter
        @PlanningVariable
        private Room room;

    public Lesson() {
    }

    public Lesson(Long id, String subject, String teacher, String studentGroup) {
        this.id = id;
        this.subject = subject;
        this.teacher = teacher;
        this.studentGroup = studentGroup;
    }

    public TimeSlot getTimeSlot() {
        return timeSlot;
    }

    public Room getRoom() {
        return room;
    }

    @Override
    public String toString() {
        return subject + "(" + id + ")";
    }
}
