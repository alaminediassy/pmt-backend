package com.visiplus.pmt.repository;

import com.visiplus.pmt.entity.Project;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ProjectRepository extends CrudRepository<Project, Long> {
    @Query("SELECT p FROM Project p JOIN p.membersWithRoles m WHERE p.owner.id = :userId OR m.member.id = :userId")
    List<Project> findProjectsByUserId(Long userId);
}
