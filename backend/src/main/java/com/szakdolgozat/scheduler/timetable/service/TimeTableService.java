package com.szakdolgozat.scheduler.timetable.service;

import com.szakdolgozat.scheduler.timetable.domain.*;
import com.szakdolgozat.scheduler.timetable.solver.TimeTableConstraintProvider;
import org.optaplanner.core.api.score.ScoreManager;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.api.solver.event.BestSolutionChangedEvent;
import org.optaplanner.core.config.solver.SolverConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;

@Service
public class TimeTableService {

    private static final Logger log = LoggerFactory.getLogger(TimeTableService.class);

    private final SolverFactory<TimeTable> solverFactory;
    private final ScoreManager<TimeTable, HardSoftScore> scoreManager;

    public TimeTableService(
            @Qualifier("timeTableSolverFactory") SolverFactory<TimeTable> solverFactory,
            @Qualifier("timeTableScoreManager") ScoreManager<TimeTable, HardSoftScore> scoreManager) {
        this.solverFactory = solverFactory;
        this.scoreManager = scoreManager;
    }

    public TimeTable solve(TimeTable problem) {
        return solve(problem, 30);
    }

    public TimeTable solve(TimeTable problem, int timeLimit) {
        log.info("Starting timetable solve with {} lessons, {} timeslots, {} rooms ({}s limit)",
                problem.getLessonList().size(), problem.getTimeSlotList().size(), problem.getRoomList().size(), timeLimit);
        Solver<TimeTable> solver = buildSolver(timeLimit);
        TimeTable solution = solver.solve(problem);
        log.info("Timetable solve complete. Score: {}", solution.getScore());
        return solution;
    }

    public TimeTableSolutionWithIterations solveWithIterations(TimeTable problem) {
        return solveWithIterations(problem, 30);
    }

    public TimeTableSolutionWithIterations solveWithIterations(TimeTable problem, int timeLimit) {
        log.info("Starting timetable solve (with iterations) with {} lessons, {} timeslots, {} rooms ({}s limit)",
                problem.getLessonList().size(), problem.getTimeSlotList().size(), problem.getRoomList().size(), timeLimit);
        Solver<TimeTable> solver = buildSolver(timeLimit);
        List<TimeTableIteration> iterations = new ArrayList<>();

        solver.addEventListener(event -> {
            if (event instanceof BestSolutionChangedEvent) {
                BestSolutionChangedEvent bestEvent = (BestSolutionChangedEvent) event;
                TimeTable solution = (TimeTable) bestEvent.getNewBestSolution();

                TimeTable solutionCopy = deepCopyTimeTable(solution);

                boolean allAssigned = solution.getLessonList().stream()
                        .allMatch(l -> l.getTimeSlot() != null && l.getRoom() != null);
                String phaseName = allAssigned ? "Local Search" : "Construction Heuristic";

                Map<String, String> constraintScores = extractConstraintScores(solutionCopy);

                TimeTableIteration iteration = new TimeTableIteration(
                    iterations.size() + 1,
                    bestEvent.getTimeMillisSpent(),
                    solution.getScore(),
                    solutionCopy,
                    phaseName,
                    constraintScores
                );
                iterations.add(iteration);
                log.debug("New best solution at step {}, time {}ms, score: {}, phase: {}",
                        iteration.getStepCount(), iteration.getTimeMillis(), iteration.getScore(), phaseName);
            }
        });

        TimeTable finalSolution = solver.solve(problem);
        log.info("Timetable solve complete. Score: {}. Collected {} iterations",
                finalSolution.getScore(), iterations.size());
        return new TimeTableSolutionWithIterations(finalSolution, iterations);
    }

    public void solveWithStream(TimeTable problem, int timeLimit, SseEmitter emitter) {
        log.info("Starting timetable SSE solve with {} lessons ({}s limit)",
                problem.getLessonList().size(), timeLimit);
        new Thread(() -> {
            try {
                Solver<TimeTable> solver = buildSolver(timeLimit);
                List<TimeTableIteration> iterations = new ArrayList<>();

                solver.addEventListener(event -> {
                    if (event instanceof BestSolutionChangedEvent) {
                        BestSolutionChangedEvent bestEvent = (BestSolutionChangedEvent) event;
                        TimeTable solution = (TimeTable) bestEvent.getNewBestSolution();
                        TimeTable solutionCopy = deepCopyTimeTable(solution);

                        boolean allAssigned = solution.getLessonList().stream()
                                .allMatch(l -> l.getTimeSlot() != null && l.getRoom() != null);
                        String phaseName = allAssigned ? "Local Search" : "Construction Heuristic";
                        Map<String, String> constraintScores = extractConstraintScores(solutionCopy);

                        TimeTableIteration iteration = new TimeTableIteration(
                                iterations.size() + 1, bestEvent.getTimeMillisSpent(),
                                solution.getScore(), solutionCopy, phaseName, constraintScores);
                        iterations.add(iteration);

                        try {
                            emitter.send(SseEmitter.event().name("iteration").data(iteration));
                        } catch (Exception e) {
                            log.debug("SSE send failed: {}", e.getMessage());
                        }
                    }
                });

                TimeTable finalSolution = solver.solve(problem);
                log.info("Timetable SSE solve complete. Score: {}. {} iterations",
                        finalSolution.getScore(), iterations.size());
                emitter.send(SseEmitter.event().name("complete").data(
                        new TimeTableSolutionWithIterations(finalSolution, iterations)));
                emitter.complete();
            } catch (Exception e) {
                log.error("SSE solve failed", e);
                emitter.completeWithError(e);
            }
        }, "timetable-solver").start();
    }

    private Solver<TimeTable> buildSolver(int timeLimit) {
        SolverConfig config = new SolverConfig()
                .withSolutionClass(TimeTable.class)
                .withEntityClasses(Lesson.class)
                .withConstraintProviderClass(TimeTableConstraintProvider.class)
                .withTerminationSpentLimit(java.time.Duration.ofSeconds(timeLimit));
        return SolverFactory.<TimeTable>create(config).buildSolver();
    }

    private Map<String, String> extractConstraintScores(TimeTable solution) {
        Map<String, String> result = new LinkedHashMap<>();
        try {
            scoreManager.explainScore(solution).getConstraintMatchTotalMap()
                    .forEach((constraintId, matchTotal) -> {
                        String name = constraintId.contains("/") ? constraintId.substring(constraintId.lastIndexOf('/') + 1) : constraintId;
                        result.put(name, matchTotal.getScore().toString());
                    });
        } catch (Exception e) {
            log.debug("Could not explain score: {}", e.getMessage());
        }
        return result;
    }

    TimeTable deepCopyTimeTable(TimeTable original) {
        TimeTable copy = new TimeTable(
            new ArrayList<>(original.getTimeSlotList()),
            new ArrayList<>(original.getRoomList()),
            new ArrayList<>()
        );

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

    public TimeTable generateDemoData(int numLessons, int numRooms) {
        if (numLessons <= 0) numLessons = 20;
        if (numRooms <= 0) numRooms = 3;

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

        List<Room> roomList = new ArrayList<>(numRooms);
        for (int i = 0; i < numRooms; i++) {
            roomList.add(new Room("Room " + (char) ('A' + i)));
        }

        String[] subjects = {"Math", "Physics", "Chemistry", "Biology", "History", "English", "Spanish", "French", "Geography", "Art"};
        String[] teachers = {"A. Turing", "M. Curie", "C. Darwin", "I. Jones", "P. Cruz"};
        String[] groups = {"9th grade", "10th grade", "11th grade"};

        List<Lesson> lessonList = new ArrayList<>();
        for (long id = 0; id < numLessons; id++) {
            lessonList.add(new Lesson(id,
                    subjects[(int) (id % subjects.length)],
                    teachers[(int) (id % teachers.length)],
                    groups[(int) (id % groups.length)]));
        }

        return new TimeTable(timeSlotList, roomList, lessonList);
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
