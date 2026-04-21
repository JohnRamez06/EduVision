package com.eduvision.service;

import com.eduvision.dto.student.RecommendationDTO;
import com.eduvision.model.StudentLectureSummary;
import com.eduvision.repository.StudentLectureSummaryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Service
@Transactional(readOnly = true)
public class RecommendationService {

    private final StudentLectureSummaryRepository summaryRepository;

    public RecommendationService(StudentLectureSummaryRepository summaryRepository) {
        this.summaryRepository = summaryRepository;
    }

    /**
     * Analyses all of a student's historical summaries and returns
     * prioritised, actionable recommendations.
     */
    public List<RecommendationDTO> generateRecommendations(String studentId) {
        List<StudentLectureSummary> summaries =
                summaryRepository.findByStudent_IdOrderByGeneratedAtDesc(studentId);

        List<RecommendationDTO> results = new ArrayList<>();

        if (summaries.isEmpty()) {
            results.add(new RecommendationDTO(
                    "ONBOARDING",
                    "Complete Your First Session",
                    "Attend a lecture session to start receiving personalised insights.",
                    RecommendationDTO.Priority.MEDIUM));
            return results;
        }

        // ── Compute averages across all sessions ──────────────────────────
        double avgConc        = avg(summaries, StudentLectureSummary::getAvgConcentration);
        double avgDistracted  = avg(summaries, StudentLectureSummary::getPctDistracted);
        double avgConfused    = avg(summaries, StudentLectureSummary::getPctConfused);
        double avgSad         = avg(summaries, StudentLectureSummary::getPctSad);
        double avgAttentive   = avg(summaries, StudentLectureSummary::getAttentivePercentage);
        double avgEngaged     = avg(summaries, StudentLectureSummary::getPctEngaged);

        // ── Rule engine ───────────────────────────────────────────────────

        // FOCUS — low concentration
        if (avgConc < 0.40) {
            results.add(new RecommendationDTO("FOCUS",
                    "Improve Your Focus During Lectures",
                    "Your average concentration is " + pct(avgConc) + "%. " +
                    "Try sitting in the front rows, silencing your phone, " +
                    "and reviewing slides before each session.",
                    RecommendationDTO.Priority.HIGH));
        } else if (avgConc < 0.65) {
            results.add(new RecommendationDTO("FOCUS",
                    "Boost Your Concentration",
                    "Your concentration is moderate (" + pct(avgConc) + "%). " +
                    "The Pomodoro technique and short study-break cycles may help.",
                    RecommendationDTO.Priority.MEDIUM));
        }

        // DISTRACTION — high distracted percentage
        if (avgDistracted > 0.35) {
            results.add(new RecommendationDTO("DISTRACTION",
                    "Manage In-Class Distractions",
                    "You appear distracted for " + pct(avgDistracted) + "% of lecture time. " +
                    "Consider closing unrelated tabs and using focus-mode apps.",
                    RecommendationDTO.Priority.HIGH));
        }

        // COMPREHENSION — high confusion rate
        if (avgConfused > 0.25) {
            results.add(new RecommendationDTO("COMPREHENSION",
                    "Seek Academic Support",
                    "Signs of confusion appear in " + pct(avgConfused) + "% of your sessions. " +
                    "Visit office hours or join a peer study group to clarify concepts early.",
                    RecommendationDTO.Priority.HIGH));
        }

        // WELLBEING — persistent sadness/negative emotion
        if (avgSad > 0.30) {
            results.add(new RecommendationDTO("WELLBEING",
                    "Student Wellbeing Check-In",
                    "You've appeared emotionally low in recent sessions. " +
                    "Speaking with a student counsellor can help — it's a sign of strength.",
                    RecommendationDTO.Priority.HIGH));
        }

        // ATTENDANCE / ATTENTIVENESS
        if (avgAttentive < 0.50) {
            results.add(new RecommendationDTO("ATTENDANCE",
                    "Increase Active Engagement",
                    "Your attentive time per lecture is below 50%. " +
                    "Active note-taking and sitting closer to the front can make a big difference.",
                    RecommendationDTO.Priority.MEDIUM));
        }

        // POSITIVE REINFORCEMENT
        if (avgEngaged > 0.60 && avgConc > 0.70) {
            results.add(new RecommendationDTO("POSITIVE",
                    "Outstanding Engagement — Keep It Up!",
                    "You're consistently engaged and focused. " +
                    "Consider mentoring peers or joining academic clubs " +
                    "to channel your momentum further.",
                    RecommendationDTO.Priority.LOW));
        }

        return results;
    }

    // ─── HELPERS ──────────────────────────────────────────────────────────

    private double avg(List<StudentLectureSummary> list,
                       Function<StudentLectureSummary, BigDecimal> getter) {
        return list.stream()
                .filter(s -> getter.apply(s) != null)
                .mapToDouble(s -> getter.apply(s).doubleValue())
                .average().orElse(0.0);
    }

    private int pct(double ratio) {
        return (int) Math.round(ratio * 100);
    }
}