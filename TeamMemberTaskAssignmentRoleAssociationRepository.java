package com.jbhunt.personnel.team.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jbhunt.infrastructure.taskassignment.entity.TeamMemberTaskAssignmentRoleAssociation;

@Repository
public interface TeamMemberTaskAssignmentRoleAssociationRepository
        extends JpaRepository<TeamMemberTaskAssignmentRoleAssociation, Integer> {

    List<TeamMemberTaskAssignmentRoleAssociation> findByTeamMemberTeamAssignmentTeamMemberTeamAssignmentIDAndExpirationTimestampAfter(
            Integer teamMemberTeamAssignment, LocalDateTime localDateTime);

    List<TeamMemberTaskAssignmentRoleAssociation> findByTaskGroupRoleTypeAssociationRoleTypeRoleTypeCodeInAndExpirationTimestampAfter(
            List<String> roleCodes, LocalDateTime expirationTimestamp);

    List<TeamMemberTaskAssignmentRoleAssociation> findByTaskGroupRoleTypeAssociationTaskGroupTaskGroupIDInAndExpirationTimestampAfter(
            List<Integer> taskGroupIDs, LocalDateTime expirationTimestamp);

    List<TeamMemberTaskAssignmentRoleAssociation> findByTeamTeamIDInAndExpirationTimestampAfter(List<Integer> teamIds,LocalDateTime expirationTimestamp );

    List<TeamMemberTaskAssignmentRoleAssociation> findByTeamTeamIDAndExpirationTimestampAfter(Integer teamID,
            LocalDateTime expirationTimestamp);

    List<TeamMemberTaskAssignmentRoleAssociation> findByTeamMemberTeamAssignmentTeamMemberTeamAssignmentIDInAndExpirationTimestampAfter(
            List<Integer> teamMemberTeamAssignment, LocalDateTime localDateTime);

     List<TeamMemberTaskAssignmentRoleAssociation>  findByTeamMemberTeamAssignmentTeamMemberPersonIDInAndExpirationTimestampAfter(List<String> empIDs,
            LocalDateTime expirationTimeStamp);
     
     List<TeamMemberTaskAssignmentRoleAssociation> findByTaskGroupRoleTypeAssociationTaskGroupTaskGroupNameLikeAndExpirationTimestampAfter(
             String taskGroupName, LocalDateTime localDateTime);

}
