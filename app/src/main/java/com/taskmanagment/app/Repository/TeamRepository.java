package com.taskmanagment.app.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.taskmanagment.app.Models.TeamModel;

@Repository
public interface TeamRepository extends JpaRepository<TeamModel, String> {
 
    Page<TeamModel> findByOwnerId(String ownerId, Pageable pageable);
 
    @Query("SELECT t FROM TeamModel t JOIN t.members m WHERE m.id = :userId")
    Page<TeamModel> findByMemberId(@Param("userId") String userId, Pageable pageable);
 
    boolean existsByNameAndOwnerId(String name, String ownerId);
}