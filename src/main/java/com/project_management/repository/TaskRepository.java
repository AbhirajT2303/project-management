package com.project_management.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project_management.entities.Task;

public interface TaskRepository extends JpaRepository<Task, Long> {

}
