package com.visiplus.pmt.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.visiplus.pmt.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "project_member_roles", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "project_id"})
})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectMemberRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "project_id")
    @JsonBackReference
    @EqualsAndHashCode.Exclude
    private Project project;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private AppUser member;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role;

    public ProjectMemberRole(Project project, AppUser appUser, Role role) {
    }
}
