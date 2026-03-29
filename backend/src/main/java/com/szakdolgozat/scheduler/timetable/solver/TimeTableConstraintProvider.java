package com.szakdolgozat.scheduler.timetable.solver;

import com.szakdolgozat.scheduler.timetable.domain.Lesson;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.core.api.score.stream.Joiners;

public class TimeTableConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                // Hard constraints
                roomConflict(constraintFactory),
                teacherConflict(constraintFactory),
                studentGroupConflict(constraintFactory),
                // Soft constraints
                teacherRoomStability(constraintFactory),
                teacherTimeEfficiency(constraintFactory),
                studentGroupTimeEfficiency(constraintFactory)
        };
    }

    private Constraint roomConflict(ConstraintFactory constraintFactory) {

        //Órát választunk
        return constraintFactory
                .forEach(Lesson.class)
                .join(Lesson.class,                                                                     //Párosítjuk egy másik órával
                        Joiners.equal(Lesson::getTimeSlot),                                             //Ugyanabban a time slotban
                        Joiners.equal(Lesson::getRoom),                                                 //Ugyanabban a szobában
                        Joiners.lessThan(Lesson::getId))                                                //És nézzük hogy ne legyen ugyan az
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Room conflict");
    }

    private Constraint teacherConflict(ConstraintFactory constraintFactory) {

        return constraintFactory
                .forEach(Lesson.class)
                .join(Lesson.class,
                        Joiners.equal(Lesson::getTimeSlot),
                        Joiners.equal(Lesson::getTeacher),
                        Joiners.lessThan(Lesson::getId))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Teacher conflict");
    }

    private Constraint studentGroupConflict(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(Lesson.class)
                .join(Lesson.class,
                        Joiners.equal(Lesson::getTimeSlot),
                        Joiners.equal(Lesson::getStudentGroup),
                        Joiners.lessThan(Lesson::getId))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Student group conflict");
    }

    // Soft constraint: A teacher prefers to teach in a single room as much as possible.
    private Constraint teacherRoomStability(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(Lesson.class)
                .join(Lesson.class,
                        Joiners.equal(Lesson::getTeacher),
                        Joiners.lessThan(Lesson::getId))
                .filter((lesson1, lesson2) -> lesson1.getRoom() != lesson2.getRoom())
                .penalize(HardSoftScore.ONE_SOFT)
                .asConstraint("Teacher room stability");
    }

    // Soft constraint: A teacher prefers to have consecutive lessons (no gaps).
    private Constraint teacherTimeEfficiency(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(Lesson.class)
                .join(Lesson.class,
                        Joiners.equal(Lesson::getTeacher),
                        Joiners.equal((lesson) -> lesson.getTimeSlot().getDayOfWeek()))
                .filter((lesson1, lesson2) -> {
                    long minutesBetween = java.time.Duration.between(
                            lesson1.getTimeSlot().getEndTime(),
                            lesson2.getTimeSlot().getStartTime()).toMinutes();
                    return minutesBetween > 0 && minutesBetween <= 30; // 30 minutes threshold for consecutive
                })
                .reward(HardSoftScore.ONE_SOFT)
                .asConstraint("Teacher time efficiency");
    }

    // Soft constraint: A student group prefers to have consecutive lessons (no gaps).
    private Constraint studentGroupTimeEfficiency(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(Lesson.class)
                .join(Lesson.class,
                        Joiners.equal(Lesson::getStudentGroup),
                        Joiners.equal((lesson) -> lesson.getTimeSlot().getDayOfWeek()))
                .filter((lesson1, lesson2) -> {
                    long minutesBetween = java.time.Duration.between(
                            lesson1.getTimeSlot().getEndTime(),
                            lesson2.getTimeSlot().getStartTime()).toMinutes();
                    return minutesBetween > 0 && minutesBetween <= 30; // 30 minutes threshold for consecutive
                })
                .reward(HardSoftScore.ONE_SOFT)
                .asConstraint("Student group time efficiency");
    }
}
