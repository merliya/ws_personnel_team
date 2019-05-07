package com.jbhunt.personnel.team.validation;

import static com.jbhunt.infrastructure.exception.dto.MessageType.ERROR;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.WordUtils;
import org.springframework.stereotype.Component;

import com.jbhunt.hrms.eoi.api.dto.adhoc.AdHocPersonDTO;
import com.jbhunt.infrastructure.exception.dto.MessageType;
import com.jbhunt.infrastructure.taskassignment.entity.TeamMemberTaskAssignmentRoleAssociation;
import com.jbhunt.personnel.schedule.dto.TeamPersonDTO;
import com.jbhunt.personnel.team.Team;
import com.jbhunt.personnel.team.TeamMemberTeamAssignment;
import com.jbhunt.personnel.team.constants.TeamCommonConstants;
import com.jbhunt.personnel.team.dto.TeamDTO;
import com.jbhunt.personnel.team.dto.TeamValidationDTO;
import com.jbhunt.personnel.team.repository.TeamMemberTaskAssignmentRoleAssociationRepository;
import com.jbhunt.personnel.team.repository.TeamRepository;
import com.jbhunt.personnel.team.util.TeamUtil;

@Component
public class TeamValidator {
    private final TeamRepository teamRepository;
    private final TeamMemberTaskAssignmentRoleAssociationRepository teamMemberTaskAssignmentRoleAssociationRepository;
    private final TeamUtil teamUtil;

    public TeamValidator(TeamRepository teamRepository,
            TeamMemberTaskAssignmentRoleAssociationRepository teamMemberTaskAssignmentRoleAssociationRepository,
            TeamUtil teamUtil) {
        this.teamRepository = teamRepository;
        this.teamMemberTaskAssignmentRoleAssociationRepository = teamMemberTaskAssignmentRoleAssociationRepository;
        this.teamUtil = teamUtil;
    }

    public void validateTeamForInactivating(TeamValidationDTO teamValidationDTO, List<Integer> teamIDs) {
        Set<String> errorMsgs = new HashSet<>();
        List<Team> teams = new ArrayList<>();
        Optional.ofNullable(teamIDs).ifPresent(teamId -> {
            teamIDs.forEach(teamID -> teamMemberTaskAssignmentRoleAssociationRepository
                    .findByTeamTeamIDAndExpirationTimestampAfter(teamID, LocalDateTime.now()).stream().findAny()
                    .ifPresent(x -> teams.add(x.getTeam())));
            if (CollectionUtils.isNotEmpty(teams)) {
                errorMsgs.add(teams.stream().map(Team::getTeamName).collect(Collectors.joining(","))
                        + " has active task assignments");
            }
            addError(ERROR, TeamCommonConstants.TEAMMANAGEMENT, new ArrayList<>(errorMsgs), teamValidationDTO);
        });
    }

    public void validateTeamName(TeamValidationDTO teamValidationDTO, TeamDTO teamDTO) {
        Set<String> errorMsgs = new HashSet<>();
        mandatoryCheck(teamDTO.getTeamName(), TeamCommonConstants.TEAMNAME, errorMsgs);
        Optional.ofNullable(teamDTO.getTeamName()).ifPresent(teamName -> {
            if (Objects.isNull(teamDTO.getTeamID())) {
                teamRepository.findAll().stream()
                        .filter(team -> team.getTeamName().trim().equalsIgnoreCase(teamDTO.getTeamName().trim()))
                        .findAny().ifPresent(team -> errorMsgs.add(teamDTO.getTeamName() + TeamCommonConstants.TEAMNAME
                                + TeamCommonConstants.ALREADYEXISTS));
            } else {
                teamRepository.findByTeamIDNotIn(teamDTO.getTeamID()).stream()
                        .filter(team -> team.getTeamName().trim().equalsIgnoreCase(teamDTO.getTeamName().trim()))
                        .findAny()
                        .ifPresent(team -> errorMsgs.add(teamDTO.getTeamName() + TeamCommonConstants.ALREADYEXISTS));
            }
            addError(ERROR, TeamCommonConstants.TEAMMANAGEMENT, new ArrayList<>(errorMsgs), teamValidationDTO);
        });
    }

    public void validateTeamMemberBeforeRemove(TeamValidationDTO teamValidationDTO, List<Integer> removingPersonIDs) {
        Set<String> errorMsgs = new HashSet<>();
        Optional.ofNullable(removingPersonIDs).ifPresent(removeIds -> {
            List<String> teamMemberPersonIDs = teamMemberTaskAssignmentRoleAssociationRepository
                    .findByTeamMemberTeamAssignmentTeamMemberTeamAssignmentIDInAndExpirationTimestampAfter(
                            removingPersonIDs, LocalDateTime.now())
                    .stream().map(TeamMemberTaskAssignmentRoleAssociation::getTeamMemberTeamAssignment)
                    .map(TeamMemberTeamAssignment::getTeamMemberPersonID).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(teamMemberPersonIDs)) {
                Map<String, AdHocPersonDTO> employeeDetails = teamUtil.getEOIDetailsByEmployeeIDs(teamMemberPersonIDs);
                
                String errorMsg = employeeDetails.values().stream().map(x -> WordUtils.capitalizeFully(x.getPreferredName() + ' ' + x.getLastName()))
                        .collect(Collectors.joining(","));
                StringBuilder errorMsgbuilder = new StringBuilder(errorMsg);
                if(employeeDetails.size()>1) {
                	errorMsgbuilder.replace(errorMsg.lastIndexOf(","), errorMsg.lastIndexOf(",") + 1, " and " );
                }
                errorMsgs.add( errorMsgbuilder.toString() + "'s Task assignment(s) will be assigned to their team");
                Optional.ofNullable(errorMsgs).ifPresent(err -> addError(ERROR, TeamCommonConstants.TEAMMANAGEMENT,
                        new ArrayList<>(err), teamValidationDTO));
            }
        });
    }

    public void validateTeamMemberCount(TeamValidationDTO teamValidationDTO, List<TeamPersonDTO> teamPersonDTOs,
            List<Integer> removingPersonIDs) {
        Set<String> errorMsgs = new HashSet<>();
        Optional.ofNullable(teamPersonDTOs).filter(x -> (teamPersonDTOs.stream().map(TeamPersonDTO::getPersonEmployeeID).collect(Collectors.toSet()).size() - removingPersonIDs.size()) < 2)
                .ifPresent(mandatory -> {
                    errorMsgs.add(TeamCommonConstants.TWOMEMBERSAREMANDATORY);
                    Optional.ofNullable(errorMsgs).ifPresent(err -> addError(ERROR, TeamCommonConstants.TEAMMANAGEMENT,
                            new ArrayList<>(err), teamValidationDTO));
                });
    }

    public void validateTeamMemberCount(TeamValidationDTO teamValidationDTO, TeamDTO teamDTO) {
        Set<String> errorMsgs = new HashSet<>();
        Optional.ofNullable(teamDTO.getTeamPersonDTOs()).ifPresent(x -> {
            if (teamDTO.getTeamPersonDTOs().stream().map(TeamPersonDTO::getPersonEmployeeID).collect(Collectors.toSet()).size() < 2) {
                errorMsgs.add(TeamCommonConstants.TWOMEMBERSAREMANDATORY);
                Optional.ofNullable(errorMsgs).ifPresent(err -> addError(ERROR, TeamCommonConstants.TEAMMANAGEMENT,
                        new ArrayList<>(err), teamValidationDTO));
            }
        });
    }

    private void mandatoryCheck(Object fieldValue, String moduleName, Set<String> errorMsgs) {
        if (Objects.isNull(fieldValue)) {
            errorMsgs.add(moduleName + TeamCommonConstants.MANDATORY);
        }
    }

    public void addError(MessageType errorType, String key, List<String> params, TeamValidationDTO teamValidationDTO) {
        if (CollectionUtils.isNotEmpty(params)) {
            teamValidationDTO.addError(errorType, key, params);
        }
    }

}
