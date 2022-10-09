package com.example.demo.dao;

import com.example.demo.model.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeachersRepo extends JpaRepository<Teacher, Integer> {


}
