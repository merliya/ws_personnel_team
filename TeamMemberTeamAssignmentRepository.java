package com.jbhunt.personnel.team.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.stereotype.Repository;

import com.jbhunt.personnel.team.TeamMemberTeamAssignment;

@Repository
public interface TeamMemberTeamAssignmentRepository
        extends JpaRepository<TeamMemberTeamAssignment, Integer>, QueryDslPredicateExecutor<TeamMemberTeamAssignment> {

    List<TeamMemberTeamAssignment> findByExpirationTimestampAfterAndTeamTeamNameContainsOrTeamTeamID(
            @Param("expirationTimestamp") LocalDateTime localDateTime, @Param("teamName") String teamName,
            @Param("teamID") Integer teamID);

    List<TeamMemberTeamAssignment> findByExpirationTimestampAfterAndTeamTeamIDAndTeamExpirationTimestampAfter(
            @Param("expirationTimeStamp") LocalDateTime expirationTimestamp, @Param("teamID") Integer teamID,
            @Param("expirationTimeStamp") LocalDateTime teamExpirationTimestamp);

    List<TeamMemberTeamAssignment> findByExpirationTimestampAfterAndTeamTeamID(LocalDateTime expirationTimestamp,
            Integer teamID);

    List<TeamMemberTeamAssignment> findByExpirationTimestampAfterAndTeamTeamIDIn(LocalDateTime expirationTimestamp,
            List<Integer> teamID);

    List<TeamMemberTeamAssignment> findByTeamMemberPersonIDAndExpirationTimestampAfter(
            @Param("teamMemberPersonID") String teamMemberPersonID,
            @DateTimeFormat(iso = ISO.DATE_TIME) @Param("expirationTimestamp") LocalDateTime expirationTimestamp);

    List<TeamMemberTeamAssignment> findByTeamTeamIDAndTeamMemberPersonIDIn(Integer teamID,
            List<String> existingTeamMemberTeamAssignmentIDs);

    List<TeamMemberTeamAssignment> findByTeamMemberPersonID(@Param("teamMemberPersonID") String teamMemberPersonID);

    List<TeamMemberTeamAssignment> findByTeamMemberTeamAssignmentIDIn(List<Integer> teamMemberTeamAssignmentIDs);

    List<TeamMemberTeamAssignment> findByTeamMemberTeamAssignmentIDInAndExpirationTimestampBefore(
            List<Integer> teamMemberTeamAssignmentIDs, LocalDateTime expirationTimestamps);

    List<TeamMemberTeamAssignment> findByTeamTeamID(Integer teamID);

    /**
     * Team Employee Name Filter Search
     */
    List<TeamMemberTeamAssignment> findByTeamMemberPersonIDInAndExpirationTimestampAfter(List<String> personIDs,
            LocalDateTime expirationTimestamp);

}
