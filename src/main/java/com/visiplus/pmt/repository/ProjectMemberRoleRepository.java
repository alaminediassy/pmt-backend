package com.visiplus.pmt.repository;

import com.visiplus.pmt.entity.ProjectMemberRole;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ProjectMemberRoleRepository extends CrudRepository<ProjectMemberRole, Long> {
    Optional<ProjectMemberRole> findByProjectIdAndMemberId(Long projectId, Long memberId);
}
