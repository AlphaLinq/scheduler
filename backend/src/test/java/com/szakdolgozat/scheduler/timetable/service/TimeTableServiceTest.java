package com.szakdolgozat.scheduler.timetable.service;

import com.szakdolgozat.scheduler.timetable.domain.TimeTable;
import com.szakdolgozat.scheduler.timetable.domain.TimeTableSolutionWithIterations;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TimeTableServiceTest {

    @Autowired
    private TimeTableService timeTableService;

    @Test
    void testGenerateDemoDataValidity() {
        TimeTable demo = timeTableService.generateDemoData();
        assertEquals(10, demo.getTimeSlotList().size());
        assertEquals(3, demo.getRoomList().size());
        assertEquals(20, demo.getLessonList().size());
        demo.getLessonList().forEach(lesson -> {
            assertNotNull(lesson.getId());
            assertNotNull(lesson.getSubject());
            assertNotNull(lesson.getTeacher());
            assertNotNull(lesson.getStudentGroup());
        });
    }

    @Test
    void testSolveReturnsFeasibleScore() {
        TimeTable problem = timeTableService.generateDemoData();
        TimeTable solution = timeTableService.solve(problem);
        assertNotNull(solution);
        assertNotNull(solution.getScore());
        assertTrue(solution.getScore().isFeasible(), "Solution should be feasible (hardScore >= 0)");
    }

    @Test
    void testSolveWithIterationsCollectsIterations() {
        TimeTable problem = timeTableService.generateDemoData();
        TimeTableSolutionWithIterations result = timeTableService.solveWithIterations(problem);
        assertNotNull(result.getFinalSolution());
        assertFalse(result.getIterations().isEmpty(), "Should have collected at least one iteration");
        result.getIterations().forEach(iter -> {
            assertNotNull(iter.getScore());
            assertTrue(iter.getTimeMillis() >= 0);
            assertTrue(iter.getStepCount() > 0);
            assertNotNull(iter.getSolution());
        });
    }

    @Test
    void testDeepCopyIntegrity() {
        TimeTable problem = timeTableService.generateDemoData();
        TimeTableSolutionWithIterations result = timeTableService.solveWithIterations(problem);
        if (result.getIterations().size() >= 2) {
            var iter0 = result.getIterations().get(0).getSolution();
            var iter1 = result.getIterations().get(1).getSolution();
            // Mutate iter0's first lesson and ensure iter1 is unaffected
            var lesson0 = iter0.getLessonList().get(0);
            var originalRoom1 = iter1.getLessonList().get(0).getRoom();
            lesson0.setRoom(null);
            // iter1's lesson should still have its original room
            assertEquals(originalRoom1, iter1.getLessonList().get(0).getRoom(),
                    "Deep copy should isolate iterations from each other");
        }
    }
}
