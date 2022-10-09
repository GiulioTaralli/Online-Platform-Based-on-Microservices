package com.example.demo.dao;

import com.example.demo.model.Teaching;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeachingRepo extends JpaRepository<Teaching, Integer> {
}
