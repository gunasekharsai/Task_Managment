package com.taskmanagment.app.Repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.taskmanagment.app.Models.TaskModel;

@Repository
public interface TaskRepository extends JpaRepository<TaskModel, String>, JpaSpecificationExecutor<TaskModel> {
 
    // Tasks assigned to a user
    Page<TaskModel> findByAssigneeId(String assigneeId, Pageable pageable);
 
    // Tasks created by a user
    Page<TaskModel> findByCreatorId(String creatorId, Pageable pageable);
 
    // Tasks for a team
    Page<TaskModel> findByTeamId(String teamId, Pageable pageable);
 
    // Tasks by status for a user
    Page<TaskModel> findByAssigneeIdAndStatus(String assigneeId, TaskModel.Status status, Pageable pageable);
 
    // Tasks by status for a team
    Page<TaskModel> findByTeamIdAndStatus(String teamId, TaskModel.Status status, Pageable pageable);
 
    // Full-text search: title or description contains keyword (case-insensitive)
    @Query("SELECT t FROM TaskModel t WHERE " +
           "(LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           " OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:assigneeId IS NULL OR t.assignee.id = :assigneeId) " +
           "AND (:teamId IS NULL OR t.team.id = :teamId)")
    Page<TaskModel> searchTasks(@Param("keyword") String keyword,
                           @Param("assigneeId") String assigneeId,
                           @Param("teamId") String teamId,
                           Pageable pageable);
 
    // Count tasks by status for a user
    long countByAssigneeIdAndStatus(String assigneeId, TaskModel.Status status);
 
    // Count tasks by status for a team
    long countByTeamIdAndStatus(String teamId, TaskModel.Status status);
}