package com.project_management.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project_management.entities.Task;
import com.project_management.entities.TaskActionHistory;

@Repository
public interface TaskActionHistoryRepository extends JpaRepository<TaskActionHistory, Long> {

	List<TaskActionHistory> findByTaskOrderByPerformedAtDesc(Task task);

	List<TaskActionHistory> findByTaskIdOrderByPerformedAtDesc(Long TaskId);

	List<TaskActionHistory> findByPerformedBy(String performedBy);
}
