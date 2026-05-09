package com.eduvision.service;

/**
 * RecommendationService — Rule-based personalized study recommendation engine.
 *
 * <p>This service reads a student's historical {@link com.eduvision.model.StudentLectureSummary}
 * records (populated by the R analytics pipeline after each session) and applies a
 * deterministic rule engine to generate actionable, prioritised recommendations
 * for the student dashboard.
 *
 * <p>The engine uses simple threshold comparisons on averaged metrics rather than a
 * machine-learning model.  This design makes the rules transparent, debuggable, and
 * easy to tune without retraining.
 *
 * <p>Rules are evaluated in order; multiple rules can fire simultaneously so the student
 * may receive several recommendations at once.  Each recommendation has a category
 * (e.g., "FOCUS", "WELLBEING"), a human-readable title and message, and a priority
 * level (HIGH, MEDIUM, LOW) used by the Flutter app to sort and colour-code cards.
 *
 * <p>If the student has no summary data yet (first login, no sessions attended),
 * a single onboarding recommendation is returned instead.
 */

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
     *
     * <p>RULE ENGINE — rules evaluated in order (all may fire):
     *
     * <ul>
     *   <li><b>FOCUS (HIGH)</b> — avgConcentration &lt; 0.40: student is consistently
     *       struggling to stay focused.  Suggests front-row seating and pre-session
     *       review.</li>
     *   <li><b>FOCUS (MEDIUM)</b> — avgConcentration in [0.40, 0.65): concentration is
     *       moderate.  Suggests Pomodoro technique.</li>
     *   <li><b>DISTRACTION (HIGH)</b> — avgDistracted &gt; 0.35: more than 35% of
     *       snapshots showed distracted concentration.  Suggests closing unrelated
     *       browser tabs and using focus-mode apps.</li>
     *   <li><b>COMPREHENSION (HIGH)</b> — avgConfused &gt; 0.25: student appears
     *       confused in more than 25% of snapshots.  Directs them to office hours
     *       or peer study groups.</li>
     *   <li><b>WELLBEING (HIGH)</b> — avgSad &gt; 0.30: persistent negative emotion
     *       across sessions.  Encourages counselling outreach with non-stigmatising
     *       language.</li>
     *   <li><b>ATTENDANCE (MEDIUM)</b> — avgAttentive &lt; 0.50: attentive time is
     *       below 50% per session.  Suggests active note-taking and front seating.</li>
     *   <li><b>POSITIVE (LOW)</b> — avgEngaged &gt; 0.60 AND avgConc &gt; 0.70:
     *       student is performing well.  Positive reinforcement with suggestions to
     *       mentor peers or join academic clubs.</li>
     * </ul>
     *
     * @param studentId the UUID of the student
     * @return ordered list of {@link RecommendationDTO}; never null
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
        // Threshold < 0.40 = "high priority": student is consistently unfocused.
        // Threshold < 0.65 = "medium priority": moderate focus, room for improvement.
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
        // > 35% of snapshots labelled "distracted" indicates a persistent problem.
        if (avgDistracted > 0.35) {
            results.add(new RecommendationDTO("DISTRACTION",
                    "Manage In-Class Distractions",
                    "You appear distracted for " + pct(avgDistracted) + "% of lecture time. " +
                    "Consider closing unrelated tabs and using focus-mode apps.",
                    RecommendationDTO.Priority.HIGH));
        }

        // COMPREHENSION — high confusion rate
        // > 25% confused snapshots suggests unresolved conceptual gaps.
        if (avgConfused > 0.25) {
            results.add(new RecommendationDTO("COMPREHENSION",
                    "Seek Academic Support",
                    "Signs of confusion appear in " + pct(avgConfused) + "% of your sessions. " +
                    "Visit office hours or join a peer study group to clarify concepts early.",
                    RecommendationDTO.Priority.HIGH));
        }

        // WELLBEING — persistent sadness/negative emotion
        // > 30% sad snapshots across multiple sessions may indicate wellbeing concerns.
        if (avgSad > 0.30) {
            results.add(new RecommendationDTO("WELLBEING",
                    "Student Wellbeing Check-In",
                    "You've appeared emotionally low in recent sessions. " +
                    "Speaking with a student counsellor can help — it's a sign of strength.",
                    RecommendationDTO.Priority.HIGH));
        }

        // ATTENDANCE / ATTENTIVENESS
        // Attentive time < 50% means the student is off-task for more than half the lecture.
        if (avgAttentive < 0.50) {
            results.add(new RecommendationDTO("ATTENDANCE",
                    "Increase Active Engagement",
                    "Your attentive time per lecture is below 50%. " +
                    "Active note-taking and sitting closer to the front can make a big difference.",
                    RecommendationDTO.Priority.MEDIUM));
        }

        // POSITIVE REINFORCEMENT
        // Only fires when both engagement AND concentration are consistently high —
        // both thresholds must be met to avoid false positives.
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

    /** Computes the arithmetic mean of a BigDecimal metric across all summaries,
     *  ignoring null values.  Returns 0.0 if no non-null values exist. */
    private double avg(List<StudentLectureSummary> list,
                       Function<StudentLectureSummary, BigDecimal> getter) {
        return list.stream()
                .filter(s -> getter.apply(s) != null)
                .mapToDouble(s -> getter.apply(s).doubleValue())
                .average().orElse(0.0);
    }

    /** Converts a 0-1 ratio to an integer percentage string for use in recommendation messages. */
    private int pct(double ratio) {
        return (int) Math.round(ratio * 100);
    }
}
