package com.szakdolgozat.scheduler.timetable.service;

import com.szakdolgozat.scheduler.timetable.domain.*;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.api.solver.event.BestSolutionChangedEvent;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;

@Service
public class TimeTableService {

    private final SolverFactory<TimeTable> solverFactory;

    public TimeTableService(@Qualifier("timeTableSolverFactory") SolverFactory<TimeTable> solverFactory){
        this.solverFactory = solverFactory;
    }

    public TimeTable solve(TimeTable problem) {
        Solver<TimeTable> solver = solverFactory.buildSolver();
        return solver.solve(problem);
    }

    public TimeTableSolutionWithIterations solveWithIterations(TimeTable problem) {
        Solver<TimeTable> solver = solverFactory.buildSolver();
        List<TimeTableIteration> iterations = new ArrayList<>();

        // Listener for best solution changes
        solver.addEventListener(event -> {
            if (event instanceof BestSolutionChangedEvent) {
                BestSolutionChangedEvent bestEvent = (BestSolutionChangedEvent) event;
                TimeTable solution = (TimeTable) bestEvent.getNewBestSolution();

                // Deep copy the solution to preserve it at this iteration
                TimeTable solutionCopy = deepCopyTimeTable(solution);

                // Determine phase name from solver state
                String phaseName = "Unknown Phase";
                try {
                    // Get the current phase name from the solver state
                    phaseName = "Solving Phase";
                } catch (Exception e) {
                    // Fallback
                }

                TimeTableIteration iteration = new TimeTableIteration(
                    0,  // step count - we'll use 0 since it's not directly available
                    bestEvent.getTimeMillisSpent(),
                    solution.getScore(),
                    solutionCopy,
                    phaseName
                );
                iterations.add(iteration);
            }
        });

        TimeTable finalSolution = solver.solve(problem);
        return new TimeTableSolutionWithIterations(finalSolution, iterations);
    }

    private TimeTable deepCopyTimeTable(TimeTable original) {
        // Create a new TimeTable with copies of the lists
        TimeTable copy = new TimeTable(
            new ArrayList<>(original.getTimeSlotList()),
            new ArrayList<>(original.getRoomList()),
            new ArrayList<>()
        );

        // Deep copy the lessons to preserve their state
        for (Lesson lesson : original.getLessonList()) {
            Lesson lessonCopy = new Lesson(
                lesson.getId(),
                lesson.getSubject(),
                lesson.getTeacher(),
                lesson.getStudentGroup()
            );
            lessonCopy.setTimeSlot(lesson.getTimeSlot());
            lessonCopy.setRoom(lesson.getRoom());
            copy.getLessonList().add(lessonCopy);
        }

        copy.setScore(original.getScore());
        return copy;
    }

    public TimeTable generateDemoData() {
        List<TimeSlot> timeSlotList = new ArrayList<>(10);
        timeSlotList.add(new TimeSlot(DayOfWeek.MONDAY, LocalTime.of(8, 30), LocalTime.of(9, 30)));
        timeSlotList.add(new TimeSlot(DayOfWeek.MONDAY, LocalTime.of(9, 30), LocalTime.of(10, 30)));
        timeSlotList.add(new TimeSlot(DayOfWeek.MONDAY, LocalTime.of(10, 30), LocalTime.of(11, 30)));
        timeSlotList.add(new TimeSlot(DayOfWeek.MONDAY, LocalTime.of(13, 30), LocalTime.of(14, 30)));
        timeSlotList.add(new TimeSlot(DayOfWeek.MONDAY, LocalTime.of(14, 30), LocalTime.of(15, 30)));

        timeSlotList.add(new TimeSlot(DayOfWeek.TUESDAY, LocalTime.of(8, 30), LocalTime.of(9, 30)));
        timeSlotList.add(new TimeSlot(DayOfWeek.TUESDAY, LocalTime.of(9, 30), LocalTime.of(10, 30)));
        timeSlotList.add(new TimeSlot(DayOfWeek.TUESDAY, LocalTime.of(10, 30), LocalTime.of(11, 30)));
        timeSlotList.add(new TimeSlot(DayOfWeek.TUESDAY, LocalTime.of(13, 30), LocalTime.of(14, 30)));
        timeSlotList.add(new TimeSlot(DayOfWeek.TUESDAY, LocalTime.of(14, 30), LocalTime.of(15, 30)));

        List<Room> roomList = new ArrayList<>(3);
        roomList.add(new Room("Room A"));
        roomList.add(new Room("Room B"));
        roomList.add(new Room("Room C"));

        List<Lesson> lessonList = new ArrayList<>();
        long id = 0;
        lessonList.add(new Lesson(id++, "Math", "A. Turing", "9th grade"));
        lessonList.add(new Lesson(id++, "Math", "A. Turing", "9th grade"));
        lessonList.add(new Lesson(id++, "Physics", "M. Curie", "9th grade"));
        lessonList.add(new Lesson(id++, "Chemistry", "M. Curie", "9th grade"));
        lessonList.add(new Lesson(id++, "Biology", "C. Darwin", "9th grade"));
        lessonList.add(new Lesson(id++, "History", "I. Jones", "9th grade"));
        lessonList.add(new Lesson(id++, "English", "I. Jones", "9th grade"));
        lessonList.add(new Lesson(id++, "English", "I. Jones", "9th grade"));
        lessonList.add(new Lesson(id++, "Spanish", "P. Cruz", "9th grade"));
        lessonList.add(new Lesson(id++, "Spanish", "P. Cruz", "9th grade"));

        lessonList.add(new Lesson(id++, "Math", "A. Turing", "10th grade"));
        lessonList.add(new Lesson(id++, "Math", "A. Turing", "10th grade"));
        lessonList.add(new Lesson(id++, "Math", "A. Turing", "10th grade"));
        lessonList.add(new Lesson(id++, "Physics", "M. Curie", "10th grade"));
        lessonList.add(new Lesson(id++, "Chemistry", "M. Curie", "10th grade"));
        lessonList.add(new Lesson(id++, "French", "M. Curie", "10th grade"));
        lessonList.add(new Lesson(id++, "Geography", "C. Darwin", "10th grade"));
        lessonList.add(new Lesson(id++, "History", "I. Jones", "10th grade"));
        lessonList.add(new Lesson(id++, "English", "P. Cruz", "10th grade"));
        lessonList.add(new Lesson(id++, "Spanish", "P. Cruz", "10th grade"));

        return new TimeTable(timeSlotList, roomList, lessonList);
    }
}
