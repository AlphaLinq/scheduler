package com.szakdolgozat.scheduler.timetable.solver;

import com.szakdolgozat.scheduler.timetable.domain.Lesson;
import com.szakdolgozat.scheduler.timetable.domain.Room;
import com.szakdolgozat.scheduler.timetable.domain.TimeSlot;
import com.szakdolgozat.scheduler.timetable.domain.TimeTable;
import org.junit.jupiter.api.Test;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.test.api.score.stream.ConstraintVerifier;

import java.time.DayOfWeek;
import java.time.LocalTime;

class TimeTableConstraintProviderTest {

    private final ConstraintVerifier<TimeTableConstraintProvider, TimeTable> constraintVerifier =
            ConstraintVerifier.build(new TimeTableConstraintProvider(), TimeTable.class, Lesson.class);

    private final TimeSlot MONDAY_0830 = new TimeSlot(DayOfWeek.MONDAY, LocalTime.of(8, 30), LocalTime.of(9, 30));
    private final TimeSlot MONDAY_0930 = new TimeSlot(DayOfWeek.MONDAY, LocalTime.of(9, 30), LocalTime.of(10, 30));
    // Gap of 10 min: 9:30 -> 9:40 start => minutesBetween = 10, satisfies >0 && <=30
    private final TimeSlot MONDAY_0940 = new TimeSlot(DayOfWeek.MONDAY, LocalTime.of(9, 40), LocalTime.of(10, 40));
    private final TimeSlot TUESDAY_0830 = new TimeSlot(DayOfWeek.TUESDAY, LocalTime.of(8, 30), LocalTime.of(9, 30));
    private final Room ROOM_A = new Room("Room A");
    private final Room ROOM_B = new Room("Room B");

    @Test
    void roomConflict() {
        // Same timeslot, same room, same student group => hard: room(-1) + group(-1) = -2
        // Different teachers => no teacher conflict
        // Same room => no room stability penalty
        Lesson lesson1 = new Lesson(1L, "Math", "A. Turing", "9th grade");
        lesson1.setTimeSlot(MONDAY_0830);
        lesson1.setRoom(ROOM_A);
        Lesson lesson2 = new Lesson(2L, "Physics", "M. Curie", "9th grade");
        lesson2.setTimeSlot(MONDAY_0830);
        lesson2.setRoom(ROOM_A);

        // room conflict -1, student group conflict -1 = -2hard, 0soft
        constraintVerifier.verifyThat()
                .given(lesson1, lesson2)
                .scores(HardSoftScore.of(-2, 0));
    }

    @Test
    void noConflict_differentTimeslots() {
        Lesson lesson1 = new Lesson(1L, "Math", "A. Turing", "9th grade");
        lesson1.setTimeSlot(MONDAY_0830);
        lesson1.setRoom(ROOM_A);
        Lesson lesson2 = new Lesson(2L, "Physics", "A. Turing", "10th grade");
        lesson2.setTimeSlot(TUESDAY_0830);
        lesson2.setRoom(ROOM_B);

        // Different days, different groups => only room stability penalty (same teacher, diff room) = -1soft
        constraintVerifier.verifyThat()
                .given(lesson1, lesson2)
                .scores(HardSoftScore.of(0, -1));
    }

    @Test
    void teacherConflict_sameTimeslot() {
        // Same teacher, same timeslot, different rooms, different groups
        Lesson lesson1 = new Lesson(1L, "Math", "A. Turing", "9th grade");
        lesson1.setTimeSlot(MONDAY_0830);
        lesson1.setRoom(ROOM_A);
        Lesson lesson2 = new Lesson(2L, "Physics", "A. Turing", "10th grade");
        lesson2.setTimeSlot(MONDAY_0830);
        lesson2.setRoom(ROOM_B);

        // teacher conflict -1hard, room stability -1soft (different rooms same teacher)
        constraintVerifier.verifyThat()
                .given(lesson1, lesson2)
                .scores(HardSoftScore.of(-1, -1));
    }

    @Test
    void studentGroupConflict_sameTimeslot() {
        // Same student group, same timeslot, different rooms, different teachers
        Lesson lesson1 = new Lesson(1L, "Math", "A. Turing", "9th grade");
        lesson1.setTimeSlot(MONDAY_0830);
        lesson1.setRoom(ROOM_A);
        Lesson lesson2 = new Lesson(2L, "Physics", "M. Curie", "9th grade");
        lesson2.setTimeSlot(MONDAY_0830);
        lesson2.setRoom(ROOM_B);

        // student group conflict = -1hard, no soft penalties (different teachers)
        constraintVerifier.verifyThat()
                .given(lesson1, lesson2)
                .scores(HardSoftScore.of(-1, 0));
    }

    @Test
    void teacherRoomStability_penalized() {
        // Same teacher, different rooms, different timeslots on same day
        // Gap: 9:30 end -> 9:40 start = 10 min, satisfies time efficiency reward
        Lesson lesson1 = new Lesson(1L, "Math", "A. Turing", "9th grade");
        lesson1.setTimeSlot(MONDAY_0830);
        lesson1.setRoom(ROOM_A);
        Lesson lesson2 = new Lesson(2L, "Physics", "A. Turing", "10th grade");
        lesson2.setTimeSlot(MONDAY_0940);
        lesson2.setRoom(ROOM_B);

        // room stability -1, teacher time efficiency +1 = 0soft
        constraintVerifier.verifyThat()
                .given(lesson1, lesson2)
                .scores(HardSoftScore.of(0, 0));
    }

    @Test
    void teacherTimeEfficiency_rewarded() {
        // Same teacher, same room, same day, gap of 10 min (9:30-end to 9:40-start)
        Lesson lesson1 = new Lesson(1L, "Math", "A. Turing", "9th grade");
        lesson1.setTimeSlot(MONDAY_0830);
        lesson1.setRoom(ROOM_A);
        Lesson lesson2 = new Lesson(2L, "Physics", "A. Turing", "10th grade");
        lesson2.setTimeSlot(MONDAY_0940);
        lesson2.setRoom(ROOM_A);

        // Same room = no room stability penalty, teacher time efficiency +1 = +1soft
        constraintVerifier.verifyThat()
                .given(lesson1, lesson2)
                .scores(HardSoftScore.of(0, 1));
    }
}
