package com.eduvision.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "weekly_periods")
public class WeeklyPeriods {

    @Id
    @Column(name = "id", columnDefinition = "char(36)")
    private String id;

    @Column(name = "week_number", nullable = false)
    private int weekNumber;

    @Column(name = "year", nullable = false)
    private int year;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    public WeeklyPeriods() {}

    public String getId()                        { return id; }
    public void setId(String id)                 { this.id = id; }

    public int getWeekNumber()                   { return weekNumber; }
    public void setWeekNumber(int weekNumber)    { this.weekNumber = weekNumber; }

    public int getYear()                         { return year; }
    public void setYear(int year)                { this.year = year; }

    public LocalDate getStartDate()              { return startDate; }
    public void setStartDate(LocalDate startDate){ this.startDate = startDate; }

    public LocalDate getEndDate()                { return endDate; }
    public void setEndDate(LocalDate endDate)    { this.endDate = endDate; }
}
