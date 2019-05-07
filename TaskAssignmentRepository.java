package com.jbhunt.personnel.team.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jbhunt.infrastructure.taskassignment.entity.TaskAssignment;

@Repository
public interface TaskAssignmentRepository extends JpaRepository<TaskAssignment, Integer> {
    
    List<TaskAssignment> findByteamMemberTaskAssignmentRoleAssociationsTeamTeamIDInAndTeamMemberTaskAssignmentRoleAssociationsExpirationTimestampAfterAndExpirationTimestampAfter(
            @Param("teamID") Integer teamID, @Param("localDateTime") LocalDateTime taskAssignmentExpirationTimeStamp,
            @Param("localDateTime") LocalDateTime taskExpirationTimeStamp);
}
