package com.group6.assignment2.entity;

import jakarta.persistence.*;

@Entity
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private ATTENDANCE isPresent;

    private String excuse;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    public Attendance() {}

    public Attendance(ATTENDANCE isPresent, String excuse, Student student, Session session) {
        this.isPresent = isPresent;
        this.excuse = excuse;
        this.student = student;
        this.session = session;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ATTENDANCE isPresent() {
        return isPresent;
    }

    public void setPresent(ATTENDANCE present) {
        isPresent = present;
    }

    public String getExcuse() {
        return excuse;
    }

    public void setExcuse(String excuse) {
        this.excuse = excuse;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public enum ATTENDANCE {
        PRESENT,
        ABSENT,
        LATE,
        EXCUSED,
        NOT_MARKED
    }
}
