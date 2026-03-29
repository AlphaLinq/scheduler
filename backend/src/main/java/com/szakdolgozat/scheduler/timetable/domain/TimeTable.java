package com.szakdolgozat.scheduler.timetable.domain;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

import java.util.List;

//Ebben az osztályban minden input és output data szerepel
@PlanningSolution
public class TimeTable {

    //Problem facts, nem változnak a megoldási folyamat közben
    @Getter
    @ValueRangeProvider
    @ProblemFactCollectionProperty
    @NotEmpty
    @Valid
    private List<TimeSlot> timeSlotList;

    @Getter
    @ValueRangeProvider
    @ProblemFactCollectionProperty
    @NotEmpty
    @Valid
    private List<Room> roomList;

    /*
        Planning entity, megváltozik megoldás közben
        Timeslot és room mezők értékei általában null -> planning variables
        Többi mező mint subject, teacher, studentGroup rendelkeznek értékkel -> problem properties

        Output solution: lessonList, score

     */
    @Getter
    @PlanningEntityCollectionProperty
    @NotEmpty
    @Valid
    private List<Lesson> lessonList;

    @Getter
    @Setter
    @PlanningScore
    private HardSoftScore score;
    //pl: 0hard/-5soft

    public TimeTable() {
    }

    public TimeTable(List<TimeSlot> timeSlotList, List<Room> roomList, List<Lesson> lessonList) {
        this.timeSlotList = timeSlotList;
        this.roomList = roomList;
        this.lessonList = lessonList;
    }

}
