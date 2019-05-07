package com.jbhunt.personnel.team.mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.stereotype.Component;

import com.jbhunt.hrms.eoi.api.dto.adhoc.AdHocPersonDTO;
import com.jbhunt.infrastructure.taskassignment.entity.TaskAssignment;
import com.jbhunt.infrastructure.taskreferencedata.dto.TaskGroupDTO;
import com.jbhunt.infrastructure.taskreferencedata.entity.TaskGroup;
import com.jbhunt.personnel.schedule.dto.EmployeeTeamIndexDTO;
import com.jbhunt.personnel.schedule.dto.TaskAssignmentDTO;
import com.jbhunt.personnel.schedule.dto.TeamPersonDTO;
import com.jbhunt.personnel.team.Team;
import com.jbhunt.personnel.team.TeamMemberTeamAssignment;
import com.jbhunt.personnel.team.dto.TeamDTO;

@Component
@Mapper(componentModel = "spring")
@DecoratedWith(TeamMapperDecorator.class)
public interface TeamMapper {

    List<TeamDTO> teamsToTeamDTOs(List<Team> team);

    @Mapping(target = "teamLeaderPersonFirstName", ignore = true)
    @Mapping(target = "teamLeaderPersonLastName", ignore = true)
    @Mapping(target = "teamLeaderTitle", ignore = true)
    @Mapping(target = "taskGroups", ignore = true)
    @Mapping(target = "taskAssignments", ignore = true)
    @Mapping(target = "taskAssignmentDTOs", ignore = true)
    @Mapping(target = "teamEffectiveTimestamp", source = "effectiveTimestamp")
    @Mapping(target = "teamExpirationTimestamp", source = "expirationTimestamp")
    @Mapping(target = "teamPersonDTOs", source = "teamMemberTeamAssignments")
    TeamDTO teamToTeamDTO(Team team);

    List<TeamPersonDTO> teamMemberteamAssessmentsToTeamPersonDTOs(
            List<TeamMemberTeamAssignment> teamMemberTeamAssignments);

    @Mapping(target = "teamLeaderPersonDTOs", ignore = true)
    @Mapping(target = "extenstion", ignore = true)
    @Mapping(target = "currentHireDate", ignore = true)
    @Mapping(target = "addOrRemove", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "phone", ignore = true)
    @Mapping(target = "roleTypes", ignore = true)
    @Mapping(target = "title", ignore = true)
    @Mapping(target = "lastName", ignore = true)
    @Mapping(target = "firstName", ignore = true)
    @Mapping(target = "preferredName", ignore = true)
    @Mapping(target = "userName", ignore = true)
    @Mapping(target = "teamMemberPersonID", source = "teamMemberTeamAssignmentID")
    @Mapping(target = "roleTypeCode", ignore = true)
    @Mapping(target = "roleTypeName", ignore = true)
    @Mapping(target = "personId", ignore = true)
    @Mapping(target = "teamAssignmentEffectiveTimestamp", source = "effectiveTimestamp")
    @Mapping(target = "teamAssignmentExpirationTimestamp", source = "expirationTimestamp")
    @Mapping(target = "personEmployeeID", source = "teamMemberPersonID")
    TeamPersonDTO teamMemberteamAssessmentToTeamPersonDTO(TeamMemberTeamAssignment teamMemberTeamAssignment);

    TeamPersonDTO convertToTeamPersonDTO(AdHocPersonDTO adHocPersonDTO, @MappingTarget TeamPersonDTO teamPersonDTO);

    @Mapping(target = "effectiveTimestamp", source = "teamDTO.teamEffectiveTimestamp")
    @Mapping(target = "expirationTimestamp", source = "teamDTO.teamExpirationTimestamp")
    @Mapping(target = "teamLeaderPersonID", source = "teamDTO.teamLeaderPersonID")
    Team teamDTOToTeam(TeamDTO teamDTO, LocalDateTime updateExpirationTimestamp);

    Set<TaskGroupDTO> taskGroupsToTaskGroupDTOs(Set<TaskGroup> taskGroups);

    TaskGroupDTO taskGroupToTaskGroupDTO(TaskGroup taskGroup);

    EmployeeTeamIndexDTO teamToEmployeeTeamIndexDTO(Team team);

    Set<TaskAssignmentDTO> taskAssignmentsToTaskAssignmentDTOs(Set<TaskAssignment> taskAssignments);

    @Mapping(target = "taskGroupID", source = "taskGroup.taskGroupID")
    @Mapping(target = "taskGroupName", source = "taskGroup.taskGroupName")
    TaskAssignmentDTO taskAssignmentToTaskAssignmentDTO(TaskAssignment taskAssignment);

}
