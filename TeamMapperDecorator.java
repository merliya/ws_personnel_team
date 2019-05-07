package com.jbhunt.personnel.team.mapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.jbhunt.hrms.eoi.api.dto.adhoc.AdHocPersonDTO;
import com.jbhunt.personnel.schedule.dto.TeamPersonDTO;
import com.jbhunt.personnel.team.Team;
import com.jbhunt.personnel.team.TeamMemberTeamAssignment;
import com.jbhunt.personnel.team.dto.TeamDTO;

public abstract class TeamMapperDecorator implements TeamMapper {
    @Autowired
    @Qualifier("delegate")
    private TeamMapper delegate;

    @Override
    public Team teamDTOToTeam(TeamDTO teamDTO, LocalDateTime expirationTimestamp) {
        Team resultTeam = delegate.teamDTOToTeam(teamDTO, expirationTimestamp);
        List<TeamMemberTeamAssignment> assignments = new ArrayList<>();
        if (Objects.isNull(teamDTO.getTeamID())) {
            resultTeam.setEffectiveTimestamp(LocalDateTime.now());
            resultTeam.setExpirationTimestamp(expirationTimestamp);
        }

        teamDTO.getTeamPersonDTOs().forEach(personDTO -> {
            TeamMemberTeamAssignment teamAssignment = new TeamMemberTeamAssignment();
            if (Objects.nonNull(personDTO.getTeamMemberPersonID())) {
                teamAssignment.setTeamMemberTeamAssignmentID(personDTO.getTeamMemberPersonID());
                teamAssignment.setEffectiveTimestamp(personDTO.getTeamAssignmentEffectiveTimestamp());
                teamAssignment.setExpirationTimestamp(personDTO.getTeamAssignmentExpirationTimestamp());
            } else {
                teamAssignment.setEffectiveTimestamp(LocalDateTime.now());
                teamAssignment.setExpirationTimestamp(expirationTimestamp);
            }
            teamAssignment.setTeamMemberPersonID(personDTO.getPersonEmployeeID());
            teamAssignment.setTeam(resultTeam);
            assignments.add(teamAssignment);
        });
        resultTeam.setTeamMemberTeamAssignments(assignments);
        return resultTeam;
    }

    @Override
    public TeamPersonDTO convertToTeamPersonDTO(AdHocPersonDTO adHocPersonDTO, TeamPersonDTO teamPersonDTO) {
        TeamPersonDTO updatedTeamPersonDTO = delegate.convertToTeamPersonDTO(adHocPersonDTO, teamPersonDTO);
        updatedTeamPersonDTO.setPersonEmployeeID(adHocPersonDTO.getEmplid());
        updatedTeamPersonDTO.setFirstName(adHocPersonDTO.getFirstName());
        updatedTeamPersonDTO.setLastName(adHocPersonDTO.getLastName());
        updatedTeamPersonDTO.setTitle(adHocPersonDTO.getPositions().stream().findAny().get().getPositionDescription());
        return updatedTeamPersonDTO;
    }

}
