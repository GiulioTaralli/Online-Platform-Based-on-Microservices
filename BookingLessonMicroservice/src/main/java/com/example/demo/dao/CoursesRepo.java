package com.example.demo.dao;

import com.example.demo.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoursesRepo extends JpaRepository<Course, Integer> {
}
