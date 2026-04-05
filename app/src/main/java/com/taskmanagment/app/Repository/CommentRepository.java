package com.taskmanagment.app.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.taskmanagment.app.Models.Comments;

@Repository
public interface CommentRepository extends JpaRepository<Comments, String> {
 
    Page<Comments> findByTaskId(String taskId, Pageable pageable);
 
    long countByTaskId(String taskId);
}
