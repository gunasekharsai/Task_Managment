package com.taskmanagment.app.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.taskmanagment.app.Models.Attachment;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, String> {
 
    List<Attachment> findByTaskId(String taskId);
}