package com.szakdolgozat.scheduler.timetable.domain;

import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.calculator.EasyScoreCalculator;

import java.util.List;

// -- Nem jól skálázható megoldás --
public class TimeTableEasyScoreCalculator implements EasyScoreCalculator<TimeTable, HardSoftScore> {

    @Override
    public HardSoftScore calculateScore(TimeTable timeTable) {
        List<Lesson> lessonList = timeTable.getLessonList();
        int hardScore = 0;
        for (Lesson a : lessonList) {
            for (Lesson b : lessonList) {
                if (a.getTimeSlot() != null && a.getTimeSlot().equals(b.getTimeSlot()) && a.getId() < b.getId()) {

                    //Szobánként 1
                    if (a.getRoom() != null && a.getRoom().equals(b.getRoom())) {
                        hardScore--;
                    }

                    //Tanár egyszerre 1 helyen tud lenni
                    if (a.getTeacher().equals(b.getTeacher())) {
                        hardScore--;
                    }

                    //Diák 1 helyen
                    if (a.getStudentGroup().equals(b.getStudentGroup())) {
                        hardScore--;
                    }

                }
            }
        }
        int softScore = 0;
        return HardSoftScore.of(hardScore, softScore);
    }
}
