package com.jbhunt.personnel.team.service;

import static com.jbhunt.personnel.team.constants.TeamCommonConstants.EFFECTIVE_TIMESTAMP;
import static com.jbhunt.personnel.team.constants.TeamCommonConstants.EXPIRATION_TIMESTAMP;
import static com.jbhunt.personnel.team.constants.TeamCommonConstants.HYPHEN;
import static com.jbhunt.personnel.team.constants.TeamCommonConstants.TEAM_ID;
import static com.jbhunt.personnel.team.constants.TeamCommonConstants.TEAM_LEADER_PERSON_ID;
import static com.jbhunt.personnel.team.constants.TeamCommonConstants.TEAM_MEMBER_COUNT;
import static com.jbhunt.personnel.team.constants.TeamCommonConstants.TEAM_NAME;
import static com.jbhunt.personnel.team.constants.TeamCommonConstants.TEAM_SUFFIX;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaBuilder.In;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jbhunt.hrms.eoi.api.dto.adhoc.AdHocPersonDTO;
import com.jbhunt.hrms.eoi.api.dto.person.PersonDTO;
import com.jbhunt.infrastructure.taskassignment.entity.TaskAssignment;
import com.jbhunt.infrastructure.taskassignment.entity.TeamMemberTaskAssignmentRoleAssociation;
import com.jbhunt.infrastructure.taskreferencedata.dto.TaskGroupDTO;
import com.jbhunt.infrastructure.taskreferencedata.entity.RoleType;
import com.jbhunt.infrastructure.taskreferencedata.entity.TaskGroupRoleTypeAssociation;
import com.jbhunt.personnel.schedule.dto.EmployeeProfileElasticIndexDTO;
import com.jbhunt.personnel.schedule.dto.TaskAssignmentDTO;
import com.jbhunt.personnel.schedule.dto.TeamPersonDTO;
import com.jbhunt.personnel.team.Team;
import com.jbhunt.personnel.team.TeamMemberTeamAssignment;
import com.jbhunt.personnel.team.constants.TeamCommonConstants;
import com.jbhunt.personnel.team.dto.EventType;
import com.jbhunt.personnel.team.dto.TeamDTO;
import com.jbhunt.personnel.team.dto.TeamProfileDTO;
import com.jbhunt.personnel.team.dto.TeamSearchDTO;
import com.jbhunt.personnel.team.dto.TeamTeamPersonDTO;
import com.jbhunt.personnel.team.dto.TeamValidationDTO;
import com.jbhunt.personnel.team.dto.TeamsDTO;
import com.jbhunt.personnel.team.handler.TeamCreateEvent;
import com.jbhunt.personnel.team.mapper.TeamMapper;
import com.jbhunt.personnel.team.properties.TeamProperties;
import com.jbhunt.personnel.team.repository.TeamMemberTaskAssignmentRoleAssociationRepository;
import com.jbhunt.personnel.team.repository.TeamMemberTeamAssignmentRepository;
import com.jbhunt.personnel.team.repository.TeamRepository;
import com.jbhunt.personnel.team.util.TeamUtil;
import com.jbhunt.personnel.team.validation.TeamValidator;

import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRException;

/**
 * Team Management services
 *
 */

@Slf4j
@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberTeamAssignmentRepository teamMemberTeamAssignmentRepository;
    private final TeamMemberTaskAssignmentRoleAssociationRepository teamMemberTaskAssignmentRoleAssociationRepository;
    private final TeamMapper teamMapper;
    private final EntityManager entityManager;
    private final TeamProperties teamProperties;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final TeamValidator teamValidator;
    private final TeamManagementReport teamManagementReport;
    private final TeamUtil teamUtil;

    public TeamService(TeamRepository teamRepository,
            TeamMemberTeamAssignmentRepository teamMemberTeamAssignmentRepository,
            TeamMemberTaskAssignmentRoleAssociationRepository teamMemberTaskAssignmentRoleAssociationRepository,
            TeamMapper teamMapper, EntityManager entityManager,
            TeamProperties teamProperties, ApplicationEventPublisher applicationEventPublisher,
            TeamValidator teamValidator, TeamManagementReport teamManagementReport, TeamUtil teamUtil) {
        this.teamRepository = teamRepository;
        this.teamMemberTeamAssignmentRepository = teamMemberTeamAssignmentRepository;
        this.teamMemberTaskAssignmentRoleAssociationRepository = teamMemberTaskAssignmentRoleAssociationRepository;
        this.teamMapper = teamMapper;
        this.entityManager = entityManager;
        this.teamProperties = teamProperties;
        this.applicationEventPublisher = applicationEventPublisher;
        this.teamValidator = teamValidator;
        this.teamManagementReport = teamManagementReport;
        this.teamUtil = teamUtil;
    }

    /**
     * @param teamID
     * @return
     */

    public TeamDTO findTeamDetailsByTeamID(Integer teamID) {
        TeamDTO teamDTO = new TeamDTO();
        Team team = teamRepository.findOne(teamID);
        Optional.ofNullable(team).ifPresent(x -> {
            teamDTO.setTeamID(teamID);
            teamDTO.setTeamName(Optional.ofNullable(team.getTeamName()).get());
            teamDTO.setTeamEffectiveTimestamp(team.getEffectiveTimestamp());
            teamDTO.setTeamExpirationTimestamp(team.getExpirationTimestamp());
            teamDTO.setCreatedBy(team.getCreateUserId());
            teamDTO.setCreatedOn(team.getCreateTimestamp());
            teamDTO.setUpdatedBy(team.getLastUpdateUserId());
            teamDTO.setUpdatedOn(team.getLastUpdateTimestamp());
            List<TeamMemberTeamAssignment> teamMemberTeamAssignments = new ArrayList<>();
            teamMemberTeamAssignments.addAll(Optional.ofNullable(team.getExpirationTimestamp())
                    .filter(expDate -> expDate.isAfter(LocalDateTime.now()))
                    .map(y -> teamMemberTeamAssignmentRepository
                            .findByExpirationTimestampAfterAndTeamTeamIDAndTeamExpirationTimestampAfter(LocalDateTime.now(), teamID, LocalDateTime.now()))
                    .orElse(teamMemberTeamAssignmentRepository.findByTeamTeamID(teamID)));
            List<String> empIDs = new ArrayList<>();
            teamMemberTeamAssignments.forEach(assignment -> empIDs.add(assignment.getTeamMemberPersonID()));
            Map<String, AdHocPersonDTO> eoiDetails = teamUtil.getEOIDetailsByEmployeeIDs(empIDs);
            Optional.ofNullable(team.getTeamLeaderPersonID()).ifPresent(id -> updateteamleaderdetails(id, teamDTO, eoiDetails));
            populateTeamMemberTeamAssignmentDetails(teamMemberTeamAssignments, eoiDetails, teamDTO);
        });
        return teamDTO;
    }

    private void populateTeamMemberTeamAssignmentDetails(List<TeamMemberTeamAssignment> teamMemberTeamAssignments,
            Map<String, AdHocPersonDTO> eoiDetails, TeamDTO teamDTO) {
        if (!teamMemberTeamAssignments.isEmpty()) {
            Map<Integer,TaskAssignmentDTO> taskMap = new HashMap<Integer,TaskAssignmentDTO>();
            getTaskDetails(teamDTO.getTeamID(), taskMap);
            Map<String,Map<TaskAssignment,List<TeamMemberTaskAssignmentRoleAssociation>>> teamMemTaskRoleMap = findRoleTypeByTeamMemberTeamAssignment(teamDTO);
            List<TeamPersonDTO> personDTOs = new ArrayList<>();
            List<TeamProfileDTO> profileDTOs = new ArrayList<>();
            teamMemberTeamAssignments.forEach(teamMemberAssignment -> Optional
                    .ofNullable(eoiDetails.get(teamMemberAssignment.getTeamMemberPersonID())).ifPresent(personID -> {
                        TeamPersonDTO teamPersonDTO = new TeamPersonDTO();
                        teamPersonDTO.setPersonEmployeeID(teamMemberAssignment.getTeamMemberPersonID());
                        teamPersonDTO.setTeamMemberPersonID(teamMemberAssignment.getTeamMemberTeamAssignmentID());
                        teamMapper.convertToTeamPersonDTO(eoiDetails.get(teamMemberAssignment.getTeamMemberPersonID()),teamPersonDTO);
                        teamPersonDTO.setTeamAssignmentEffectiveTimestamp(teamMemberAssignment.getEffectiveTimestamp());
                        teamPersonDTO.setTeamAssignmentExpirationTimestamp(teamMemberAssignment.getExpirationTimestamp());
                        teamPersonDTO.setFirstName(camelCaseNames(teamPersonDTO.getFirstName()));
                        teamPersonDTO.setPreferredName(camelCaseNames(teamPersonDTO.getPreferredName()));
                        teamPersonDTO.setLastName(camelCaseNames(teamPersonDTO.getLastName()));
                        if(Objects.nonNull(teamMemTaskRoleMap.get(teamMemberAssignment.getTeamMemberPersonID()))) {
                        	teamPersonDTO.setTaskCount(teamMemTaskRoleMap.get(teamMemberAssignment.getTeamMemberPersonID()).keySet().size());
                        	populateTeamProfileForTeamMembers(teamMemTaskRoleMap.get(teamMemberAssignment.getTeamMemberPersonID()),teamPersonDTO,taskMap,profileDTOs,teamDTO);
                        }else {
                        	teamPersonDTO.setTaskCount(0);
                        }
                        personDTOs.add(teamPersonDTO);
                    }));
            if(Objects.nonNull(teamMemTaskRoleMap.get("0"))) {
            	populateTeamProfileForTeamMembers(teamMemTaskRoleMap.get("0"),null,taskMap,profileDTOs,teamDTO);
            }
            teamDTO.setTeamPersonDTOs(personDTOs);
            teamDTO.setTeamProfileDTOs(profileDTOs);
        }
    }

    private void updateteamleaderdetails(String teamLeaderPersonID, TeamDTO teamDTO, Map<String, AdHocPersonDTO> eoiDetails) {
        List<String> empIDs = new ArrayList<>();
        empIDs.add(teamLeaderPersonID);
        teamDTO.setTeamLeaderPersonID(teamLeaderPersonID);
        AdHocPersonDTO adHocPersonDTO = eoiDetails.get(teamLeaderPersonID);
        Optional.ofNullable(adHocPersonDTO).ifPresent(adHocPerson -> {
            teamDTO.setTeamLeaderPersonFirstName(camelCaseNames(adHocPersonDTO.getPreferredName()));
            teamDTO.setTeamLeaderPersonLastName(camelCaseNames(adHocPersonDTO.getLastName()));
            teamDTO.setTeamLeaderTitle(adHocPersonDTO.getPositions().stream().findAny().get().getPositionDescription());
        });
    }


    /**
     * @param assignment
     * @return
     */
    public Map<String,Map<TaskAssignment,List<TeamMemberTaskAssignmentRoleAssociation>>> findRoleTypeByTeamMemberTeamAssignment(TeamDTO teamDTO) {
        List<TeamMemberTaskAssignmentRoleAssociation> roleAssociations = teamMemberTaskAssignmentRoleAssociationRepository
                .findByTeamTeamIDAndExpirationTimestampAfter(teamDTO.getTeamID(), LocalDateTime.now());
        Map<TeamMemberTeamAssignment, List<TeamMemberTaskAssignmentRoleAssociation>> teamAsgnMap = roleAssociations.stream()
				        .filter(asc -> Objects.nonNull(asc.getTeamMemberTeamAssignment()))
				        .collect(Collectors.groupingBy(TeamMemberTaskAssignmentRoleAssociation::getTeamMemberTeamAssignment));
        Map<String,Map<TaskAssignment,List<TeamMemberTaskAssignmentRoleAssociation>>> teamMemTaskRoleMap = new HashMap<>();
        Map<TaskAssignment,List<TeamMemberTaskAssignmentRoleAssociation>> teamTaskMap = roleAssociations.stream()
        		.filter(asociation -> Objects.isNull(asociation.getTeamMemberTeamAssignment()))
        		.collect(Collectors.groupingBy(TeamMemberTaskAssignmentRoleAssociation::getTaskAssignment));
        teamMemTaskRoleMap.put("0", teamTaskMap);
        teamAsgnMap.entrySet().forEach( entry ->{
        	Map<TaskAssignment,List<TeamMemberTaskAssignmentRoleAssociation>> taskRoleMap = entry.getValue().stream()
        			.collect(Collectors.groupingBy(TeamMemberTaskAssignmentRoleAssociation::getTaskAssignment));
        		teamMemTaskRoleMap.put(entry.getKey().getTeamMemberPersonID(), taskRoleMap);
        });
        
		return teamMemTaskRoleMap;
    }
    
    private void populateTeamProfileForTeamMembers(Map<TaskAssignment,List<TeamMemberTaskAssignmentRoleAssociation>> taskRoleMap,
    		TeamPersonDTO teamPersonDTO,Map<Integer,TaskAssignmentDTO> taskMap,List<TeamProfileDTO> profileDTOs,TeamDTO teamDTO) {
    	taskRoleMap.entrySet().forEach(entry ->{
    		TaskAssignment task = entry.getKey();
    		String role = entry.getValue().stream().map(TeamMemberTaskAssignmentRoleAssociation::getTaskGroupRoleTypeAssociation)
    				.map(TaskGroupRoleTypeAssociation::getRoleType).map(RoleType::getRoleTypeName).collect(Collectors.joining(","));
    		TeamProfileDTO teamProfile;
    		if(teamPersonDTO!=null) {
    			 teamProfile = new TeamProfileDTO(teamPersonDTO.getUserID(),teamPersonDTO.getPersonEmployeeID(),teamPersonDTO.getFirstName(),
        				teamPersonDTO.getLastName(),teamPersonDTO.getPreferredName(),teamPersonDTO.getTitle(),task.getTaskAssignmentID(),task.getTaskAssignmentName(),
        				task.getTaskGroup().getTaskGroupID(),task.getTaskGroup().getTaskGroupName(), role,teamDTO.getTeamName());
    		}else {
    			 teamProfile = new TeamProfileDTO(null,null,null,null,null,null,task.getTaskAssignmentID(),task.getTaskAssignmentName(),
        				task.getTaskGroup().getTaskGroupID(),task.getTaskGroup().getTaskGroupName(), role,teamDTO.getTeamName());
    		}
    		if(Objects.nonNull(taskMap.get(task.getTaskAssignmentID()))) {
    			teamProfile.setTaskAssignmentResponsibilityGroupDTOs(taskMap.get(task.getTaskAssignmentID()).getTaskAssignmentResponsibilityGroupDTOs());
    		}
    		profileDTOs.add(teamProfile);
    	});
    }
    
    /**
     * @param teamIDs
     * @param status
     * @return
     */
    @Transactional
    public List<TeamDTO> inActivateTeam(List<Integer> teamIDs) {
        List<TeamDTO> teamDTOs = new ArrayList<>();
        List<Team> teamAssignments = teamRepository.findByTeamIDIn(teamIDs);
        TeamValidationDTO teamValidationDTO = new TeamValidationDTO();
        teamValidator.validateTeamForInactivating(teamValidationDTO, teamIDs);
        if (MapUtils.isNotEmpty(teamValidationDTO.getErrors())) {
            TeamDTO dtoWithValidationError = new TeamDTO();
            dtoWithValidationError.setTeamValidationDTO(teamValidationDTO);
            teamDTOs.add(dtoWithValidationError);
            return teamDTOs;
        }
        teamAssignments.forEach(team -> {
            List<TeamMemberTeamAssignment> teamMemberTeamAssignment = Optional
                    .ofNullable(team.getTeamMemberTeamAssignments()).get().stream()
                    .filter(teamMemberAssignment -> teamMemberAssignment.getExpirationTimestamp()
                            .isAfter(LocalDateTime.now()))
                    .collect(Collectors.toList());
            if (!teamMemberTeamAssignment.isEmpty()) {
                teamMemberTeamAssignment.forEach(t -> t.setExpirationTimestamp(LocalDateTime.now()));
                team.setTeamMemberTeamAssignments(teamMemberTeamAssignment);
            } else {
                team.setTeamMemberTeamAssignments(new ArrayList<TeamMemberTeamAssignment>());
            }
            team.setExpirationTimestamp(LocalDateTime.now());
        });
        List<Team> teams = teamRepository.save(teamAssignments);
        teamDTOs = teamMapper.teamsToTeamDTOs(teams);
        applicationEventPublisher.publishEvent(new TeamCreateEvent(teamDTOs, EventType.INACTIVATETEAM));
        return teamDTOs;
    }

    public List<TeamDTO> reActivateTeams(List<Integer> teamIDs) {
        List<TeamDTO> teamDTOs = new ArrayList<>();
        List<Team> teams = new ArrayList<>();
        Optional.ofNullable(teamIDs).ifPresent(ids -> teams.addAll(teamRepository.findByTeamIDIn(ids)));
        List<TeamMemberTeamAssignment> teamMemberTeamAssignments = new ArrayList<>();
        List<String> employeeIDs = new ArrayList<>();
        if (!teams.isEmpty()) {
            teams.forEach(team -> {
                team.setExpirationTimestamp(updateExpirationTimestamp());
                teamMemberTeamAssignments.addAll(team.getTeamMemberTeamAssignments());
                employeeIDs.addAll(team.getTeamMemberTeamAssignments().stream()
                        .map(TeamMemberTeamAssignment::getTeamMemberPersonID).collect(Collectors.toList()));
            });
        }
        List<String> activeEmployeeIDs = new ArrayList<>();
        Optional.ofNullable(employeeIDs).ifPresent(x -> activeEmployeeIDs
                .addAll(Optional.ofNullable(teamUtil.getEOIDetailsByEmployeeIDs(x)).map(obj -> obj.keySet()).get()));
        if (!activeEmployeeIDs.isEmpty()) {
            List<TeamMemberTeamAssignment> activeTeamMemberTeamAssignments = teamMemberTeamAssignments.stream()
                    .filter(teamassg -> activeEmployeeIDs.contains(teamassg.getTeamMemberPersonID()))
                    .collect(Collectors.toList());
            activeTeamMemberTeamAssignments.forEach(assg -> assg.setExpirationTimestamp(updateExpirationTimestamp()));
            teams.forEach(team -> {
                team.setTeamMemberTeamAssignments(activeTeamMemberTeamAssignments.stream()
                        .filter(team1 -> team1.getTeamID().equals(team.getTeamID())).collect(Collectors.toList()));
                if (!activeEmployeeIDs.contains(team.getTeamLeaderPersonID())) {
                    team.setTeamLeaderPersonID(null);
                }
            });
        } else {
            teams.forEach(t -> t.setTeamLeaderPersonID(null));
        }
        Optional.ofNullable(teams)
                .ifPresent(tt -> teamDTOs.addAll(teamMapper.teamsToTeamDTOs(teamRepository.save(tt))));
        Optional.ofNullable(teamDTOs).ifPresent(
                dto -> applicationEventPublisher.publishEvent(new TeamCreateEvent(dto, EventType.REACTIVATETEAM)));
        return teamDTOs;
    }

    private LocalDateTime updateExpirationTimestamp() {
        return LocalDateTime.of(teamProperties.getValueYear(), teamProperties.getValueMonth(),
                teamProperties.getValueDay(), teamProperties.getValueHour(), teamProperties.getValueMinutes());
    }

    /**
     * Fetches the active team member team assignments based on personID.
     * 
     * @param personID
     * @return
     */
    public List<Team> findTeamByPersonID(String personID) {
        List<TeamMemberTeamAssignment> teamAssignments = Optional.ofNullable(teamMemberTeamAssignmentRepository
                .findByTeamMemberPersonIDAndExpirationTimestampAfter(personID, LocalDateTime.now())).get();
        List<Team> assignedTeamIDs = new ArrayList<>();
        Optional.ofNullable(teamAssignments)
                .ifPresent(x -> x.forEach(teamAssignment -> assignedTeamIDs.add(teamAssignment.getTeam())));
        return assignedTeamIDs;
    }

    /**
     * @param teamIDs
     * @return
     */
    public List<TeamTeamPersonDTO> findTeamAndTeamMemberByTeamID(List<Integer> teamIDs) {
        List<TeamTeamPersonDTO> personDTOs = new ArrayList<>();
        teamIDs.forEach(teamID -> personDTOs.addAll(updateTeamTeamPersonDTO(teamID)));
        return personDTOs;
    }

    private List<TeamTeamPersonDTO> updateTeamTeamPersonDTO(Integer teamID) {
        List<TeamTeamPersonDTO> teamTeamPersonDTOs = new ArrayList<>();
        Team team = teamRepository.findByTeamIDAndExpirationTimestampAfter(teamID, LocalDateTime.now());
        Optional.ofNullable(team).ifPresent(tem -> {
            TeamTeamPersonDTO teamTeamPersonDTO = new TeamTeamPersonDTO();
            teamTeamPersonDTO.setID(TEAM_SUFFIX + team.getTeamID().toString());
            teamTeamPersonDTO.setName(team.getTeamName());
            teamTeamPersonDTOs.add(teamTeamPersonDTO);
            List<TeamMemberTeamAssignment> teamMemberAssignments = teamMemberTeamAssignmentRepository
                    .findByExpirationTimestampAfterAndTeamTeamIDAndTeamExpirationTimestampAfter(LocalDateTime.now(),
                            teamID, LocalDateTime.now());
            populateEOIDetails(teamMemberAssignments, teamTeamPersonDTOs);
        });
        return teamTeamPersonDTOs;
    }

    private void populateEOIDetails(List<TeamMemberTeamAssignment> teamMemberAssignments,
            List<TeamTeamPersonDTO> teamTeamPersonDTOs) {
        List<String> empIDs = new ArrayList<>();
        teamMemberAssignments.forEach(assignment -> empIDs.add(assignment.getTeamMemberPersonID()));
        Map<String, AdHocPersonDTO> eoiDetails = teamUtil.getEOIDetailsByEmployeeIDs(empIDs);
        teamMemberAssignments.forEach(assignment -> {
            TeamTeamPersonDTO teamPersonDTO = new TeamTeamPersonDTO();
            AdHocPersonDTO adhocPersonDTO = eoiDetails.get(assignment.getTeamMemberPersonID());
            Optional.ofNullable(adhocPersonDTO).ifPresent(x -> {
                teamPersonDTO.setID(TEAM_SUFFIX + assignment.getTeam().getTeamID() + HYPHEN
                        + assignment.getTeamMemberTeamAssignmentID());
                teamPersonDTO.setName(camelCaseNames(x.getPreferredName()) + " " + camelCaseNames(x.getLastName()) + ", "
                        + x.getPositions().stream().findAny().get().getPositionDescription());
                teamPersonDTO.setTeamMemberId(assignment.getTeamMemberPersonID());
                teamTeamPersonDTOs.add(teamPersonDTO);
            });
        });
    }

    /**
     * @param teamDTO
     * @return
     */

    @Transactional
    public TeamDTO updateTeam(TeamDTO teamDTO) {
        TeamDTO resultTeamDTO = new TeamDTO();
        // team validation
        TeamValidationDTO teamValidationDTO = new TeamValidationDTO();
        List<Integer> removingPersonIds = teamValiadtion(teamDTO, teamValidationDTO);
        if (MapUtils.isNotEmpty(teamValidationDTO.getErrors())) {
            log.info("Team Validation Error present");
            resultTeamDTO.setTeamValidationDTO(teamValidationDTO);
            return resultTeamDTO;
        }
        Team resultTeam = teamMapper.teamDTOToTeam(teamDTO, updateExpirationTimestamp());
        Map<Integer, TeamMemberTeamAssignment> teamMemberTeamAsgnMap = new HashMap<>();
        List<TeamMemberTeamAssignment> allnewlist = new ArrayList<>();
        List<String> teamMemberTeamAssignmentIDs = resultTeam.getTeamMemberTeamAssignments().stream()
                .map(TeamMemberTeamAssignment::getTeamMemberPersonID).collect(Collectors.toList());
        if(Objects.nonNull(teamDTO.getTeamID())){
            Map<String, TeamMemberTeamAssignment> teamAssignmentMap = teamMemberTeamAssignmentRepository.findByTeamTeamIDAndTeamMemberPersonIDIn(teamDTO.getTeamID(), teamMemberTeamAssignmentIDs)
            		.stream().collect(Collectors.toMap(p -> p.getTeamMemberPersonID(), p -> p));
            if (!teamAssignmentMap.isEmpty()) {
            	Map<Integer,TeamMemberTeamAssignment> idList = teamAssignmentMap.values().stream().collect(Collectors.toMap(p -> p.getTeamMemberTeamAssignmentID(), p -> p));
                List<String> incomingPerson = resultTeam.getTeamMemberTeamAssignments().stream().map(TeamMemberTeamAssignment::getTeamMemberPersonID).collect(Collectors.toList());
                resultTeam.getTeamMemberTeamAssignments().forEach(teamAssignment -> {
                    if (Objects.nonNull(teamAssignmentMap.get(teamAssignment.getTeamMemberPersonID()))) {
                        teamAssignmentMap.get(teamAssignment.getTeamMemberPersonID()).setExpirationTimestamp(teamAssignment.getExpirationTimestamp());
                        teamMemberTeamAsgnMap.put(teamAssignment.getTeamMemberTeamAssignmentID(),teamAssignmentMap.get(teamAssignment.getTeamMemberPersonID()));
                    } else if (Objects.nonNull(teamAssignment.getTeamMemberTeamAssignmentID())) { 
                    	if(idList.keySet().contains(teamAssignment.getTeamMemberTeamAssignmentID()) && incomingPerson.contains(idList.get(teamAssignment.getTeamMemberTeamAssignmentID()).getTeamMemberPersonID())){
                    		teamAssignment.setTeamMemberTeamAssignmentID(null);
                    		teamMemberTeamAsgnMap.put(Integer.parseInt(teamAssignment.getTeamMemberPersonID()), teamAssignment);
                    	}else
                    		teamMemberTeamAsgnMap.put(teamAssignment.getTeamMemberTeamAssignmentID(), teamAssignment);
                    } else if (Objects.isNull(teamAssignment.getTeamMemberTeamAssignmentID()))
                        allnewlist.add(teamAssignment);
                });
                allnewlist.addAll(teamMemberTeamAsgnMap.values());
                resultTeam.setTeamMemberTeamAssignments(allnewlist);
            }
        }
        //Defect 88687
        if (CollectionUtils.isNotEmpty(removingPersonIds)) {
        	List<TeamMemberTaskAssignmentRoleAssociation> removedmembers = teamMemberTaskAssignmentRoleAssociationRepository
        	        .findByTeamMemberTeamAssignmentTeamMemberTeamAssignmentIDInAndExpirationTimestampAfter(
        	        		removingPersonIds, LocalDateTime.now());
        	removedmembers.forEach(association -> association.setTeamMemberTeamAssignment(null));
        	teamMemberTaskAssignmentRoleAssociationRepository.save(removedmembers);
        }
        
        TeamDTO savedTeamDTO = teamMapper.teamToTeamDTO(teamRepository.save(resultTeam));
        applicationEventPublisher.publishEvent(
                new TeamCreateEvent(new ArrayList<TeamDTO>(Arrays.asList(savedTeamDTO)), EventType.CREATETEAM));
        
        return savedTeamDTO;
    }

    private List<Integer> teamValiadtion(TeamDTO teamDTO, TeamValidationDTO teamValidationDTO) {
        teamValidator.validateTeamName(teamValidationDTO, teamDTO);
        if (Objects.isNull(teamDTO.getTeamID())) {
            teamValidator.validateTeamMemberCount(teamValidationDTO, teamDTO);
        }
        List<Integer> removingPersonIDs = new ArrayList<Integer>();
        // update team- validations
        Optional.ofNullable(teamDTO.getTeamID()).ifPresent(teamID -> {
            
            List<TeamPersonDTO> removeTeamPersonDTOs = new ArrayList<>();
            List<TeamPersonDTO> teamPersonDTOs = Optional.ofNullable(teamDTO.getTeamPersonDTOs()).get();
            teamPersonDTOs.forEach(personDTO -> Optional.ofNullable(personDTO.getTeamMemberPersonID())
                    .ifPresent(x1 -> removeTeamPersonDTOs.add(personDTO)));
            if (!removeTeamPersonDTOs.isEmpty()) {
                removingPersonIDs.addAll(removeTeamPersonDTOs.stream()
                        .filter(removePersonDTO -> removePersonDTO.getTeamAssignmentExpirationTimestamp()
                                .isBefore(LocalDateTime.now()))
                        .map(TeamPersonDTO::getTeamMemberPersonID).collect(Collectors.toList()));
            }
            if (CollectionUtils.isNotEmpty(removingPersonIDs)) {
                teamValidator.validateTeamMemberCount(teamValidationDTO, teamPersonDTOs, removingPersonIDs);
            } else {
                teamValidator.validateTeamMemberCount(teamValidationDTO, teamDTO);
            }
          //Defect 88687
            //teamValidator.validateTeamMemberBeforeRemove(teamValidationDTO, removingPersonIDs);
        });
        return removingPersonIDs;
    }

    public Page<TeamsDTO> findAllTeams(TeamSearchDTO teamSearchDTO, Pageable pageable,String sortField, String sortDir) throws IOException {
        List<TeamsDTO> teamDTOs = populateFilteredTeams(teamSearchDTO,sortField,sortDir);
        List<TeamsDTO> teamPaginated = new ArrayList<>();
        if("teamLeaderPersonFirstName".equals(sortField)) {
        	enrichEOIEmployeeDetails(teamDTOs);
        	if("desc".equals(sortDir)) {
        		Collections.sort(teamDTOs, 
            			Comparator.comparing(TeamsDTO::getTeamLeaderPersonFirstName, Comparator.nullsLast(Comparator.reverseOrder())));
        	}else {
        		Collections.sort(teamDTOs, 
            			Comparator.comparing(TeamsDTO::getTeamLeaderPersonFirstName, Comparator.nullsFirst(Comparator.naturalOrder())));
        	}
        	
        }
        if (CollectionUtils.isEmpty(teamDTOs)) {
            teamPaginated.addAll(teamDTOs);
        } else {
            int fromIndex = pageable.getPageNumber() * pageable.getPageSize();
            int toIndex = pageable.getPageNumber() * pageable.getPageSize() + pageable.getPageSize();
           
            if (!CollectionUtils.isEmpty(teamDTOs)) {
                if (teamDTOs.size() <= toIndex) {
                    toIndex = teamDTOs.size();
                }
                teamPaginated.addAll(teamDTOs.subList(fromIndex, toIndex));

            }
        }
        if(!"teamLeaderPersonFirstName".equals(sortField)) {
        	enrichEOIEmployeeDetails(teamPaginated);
        }
        enrichTaskDetails(teamPaginated);
        return new PageImpl<>(teamPaginated, pageable, teamDTOs.size());
    }

    private List<TeamsDTO> populateFilteredTeams(TeamSearchDTO teamSearchDTO,String sortField,String sortDir) throws IOException {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<TeamsDTO> criteriaQuery = criteriaBuilder.createQuery(TeamsDTO.class);
        Root<Team> teamRoot = criteriaQuery.from(Team.class);
        Join<Team, TeamMemberTeamAssignment> teamMemberTeamAssignment = teamRoot.join("teamMemberTeamAssignments",
                JoinType.LEFT);
        criteriaQuery
                .multiselect(teamRoot.<String>get(TEAM_NAME).alias(TEAM_NAME),
                        teamRoot.<Integer>get(TEAM_ID).alias(TEAM_ID),
                        teamRoot.<String>get(TEAM_LEADER_PERSON_ID).alias(TEAM_LEADER_PERSON_ID),
                        teamRoot.<LocalDateTime>get(EFFECTIVE_TIMESTAMP).alias(EFFECTIVE_TIMESTAMP),
                        teamRoot.<LocalDateTime>get(EXPIRATION_TIMESTAMP).alias(EXPIRATION_TIMESTAMP),
                        criteriaBuilder.count(teamRoot).alias(TEAM_MEMBER_COUNT))
                .groupBy(teamRoot.<String>get(TEAM_NAME), teamRoot.<Integer>get(TEAM_ID),
                        teamRoot.<LocalDateTime>get(EFFECTIVE_TIMESTAMP),
                        teamRoot.<LocalDateTime>get(EXPIRATION_TIMESTAMP), teamRoot.<String>get(TEAM_LEADER_PERSON_ID));
        
        List<Predicate> filterPredicates = new ArrayList<>();
        if (Objects.nonNull(teamSearchDTO)) {
        	if(StringUtils.isNotEmpty(teamSearchDTO.getSearchCriteria()) && teamSearchDTO.getSearchCriteria().length() < 3) {
            	criteriaQuery.having(criteriaBuilder.equal(criteriaBuilder.count(teamRoot).as(String.class), teamSearchDTO.getSearchCriteria()));
            }
        	performSort(sortField,sortDir,criteriaQuery,criteriaBuilder,teamRoot);
            filterPredicates.addAll(createFilterPredicates(teamSearchDTO, criteriaBuilder, teamRoot, teamMemberTeamAssignment));
            // Role code filter
            if (Objects.nonNull(teamSearchDTO.getRoleCodes())
                    && CollectionUtils.isNotEmpty(teamSearchDTO.getRoleCodes())) {
                List<String> roleCodes = Optional.ofNullable(teamSearchDTO.getRoleCodes()).get();
                List<TeamMemberTaskAssignmentRoleAssociation> teamMemberTaskAssignmentRoleAssociations = teamMemberTaskAssignmentRoleAssociationRepository
                        .findByTaskGroupRoleTypeAssociationRoleTypeRoleTypeCodeInAndExpirationTimestampAfter(roleCodes,
                                LocalDateTime.now());
                List<Integer> teamIDs = teamMemberTaskAssignmentRoleAssociations.stream()
                        .map(association -> association.getTeam().getTeamID()).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(teamIDs)) {
                	In<Integer> teamIDin = criteriaBuilder.in(teamRoot.get(TEAM_ID));
                    teamIDs.forEach(teamIDin::value);
                    filterPredicates.add(teamIDin);
                }
            }
            // task group filter
            if (Objects.nonNull(teamSearchDTO.getTaskGroupIDs())
                    && CollectionUtils.isNotEmpty(teamSearchDTO.getTaskGroupIDs())) {
                List<Integer> taskGroupIDs = Optional.ofNullable(teamSearchDTO.getTaskGroupIDs()).get();
                List<TeamMemberTaskAssignmentRoleAssociation> teamMemberTaskAssignmentRoleAssociations = teamMemberTaskAssignmentRoleAssociationRepository
                        .findByTaskGroupRoleTypeAssociationTaskGroupTaskGroupIDInAndExpirationTimestampAfter(
                                taskGroupIDs, LocalDateTime.now());
                List<Integer> teamIDs = teamMemberTaskAssignmentRoleAssociations.stream()
                        .map(teamMemberTaskAssignmentRoleAssociation -> teamMemberTaskAssignmentRoleAssociation
                                .getTeam().getTeamID())
                        .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(teamIDs)) {
                	In<Integer> teamIDin = criteriaBuilder.in(teamRoot.get(TEAM_ID));
                    teamIDs.forEach(teamIDin::value);
                    filterPredicates.add(teamIDin);
                }
                
            }
            // personID filter
            if (Objects.nonNull(teamSearchDTO.getPersonIDs())
                    && CollectionUtils.isNotEmpty(teamSearchDTO.getPersonIDs())) {
                List<String> personIDs = Optional.ofNullable(teamSearchDTO.getPersonIDs()).get();
                List<TeamMemberTeamAssignment> teamMemberTaskAssignmentRoleAssociations = teamMemberTeamAssignmentRepository
                        .findByTeamMemberPersonIDInAndExpirationTimestampAfter(personIDs, LocalDateTime.now());
                List<Integer> teamIDs = teamMemberTaskAssignmentRoleAssociations.stream()
                        .map(teamMemberTaskAssignmentRoleAssociation -> teamMemberTaskAssignmentRoleAssociation
                                .getTeam().getTeamID())
                        .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(teamIDs)) {
                	In<Integer> teamIDin = criteriaBuilder.in(teamRoot.get(TEAM_ID));
                    teamIDs.forEach(teamIDin::value);
                    filterPredicates.add(teamIDin);
                }
            }
        }

        Predicate activeMemberAssignment = criteriaBuilder.not(criteriaBuilder.and(
                criteriaBuilder.greaterThan(teamRoot.get(TeamCommonConstants.EXPIRATION_TIMESTAMP),
                        LocalDateTime.now()),
                criteriaBuilder.lessThan(teamMemberTeamAssignment.get(TeamCommonConstants.EXPIRATION_TIMESTAMP),
                        LocalDateTime.now())));
        filterPredicates.add(activeMemberAssignment);

        // equal condition by Search by Team name or ID
        criteriaQuery.where(criteriaBuilder.and(filterPredicates.toArray(new Predicate[] {})));
        TypedQuery<TeamsDTO> query = entityManager.createQuery(criteriaQuery);
        List<TeamsDTO> teamDTOs = query.getResultList();
        return teamDTOs;
    }

    private void enrichTaskDetails(List<TeamsDTO> teamDTOs) throws IOException {

        teamDTOs.forEach(dto -> {
            List<TaskAssignmentDTO> taskDTOs = new ArrayList<>();
            try {
                taskDTOs.addAll(teamUtil.fetchElasticTaskAssignmentDetailsByTeamID(dto.getTeamID()));
            } catch (IOException e) {
                log.error("Exception while fetching task details from Elastic : " , e);
            }
            Set<TaskGroupDTO> taskGroupDTOs  =  new HashSet<TaskGroupDTO>();
            taskDTOs.stream().filter(t -> Objects.nonNull(t.getTaskGroupName())).forEach(task ->{
            	TaskGroupDTO tg = new TaskGroupDTO();
            	tg.setTaskGroupName(task.getTaskGroupName());
            	tg.setTaskGroupID(task.getTaskGroupID());
            	taskGroupDTOs.add(tg);
            });
            dto.setTaskGroupDTOs(taskGroupDTOs);
            dto.setTaskGroupName(taskDTOs.stream().map(TaskAssignmentDTO::getTaskGroupName).distinct()
                    .filter(Objects::nonNull).collect(Collectors.joining(",")));
        });
    }

    private void enrichEOIEmployeeDetails(List<TeamsDTO> teamDTOs) throws IOException {
        // Fetch Employee detail from EOI based on the IDS and bind with TeamDTO
        List<String> empIDs = teamDTOs.stream().map(teamDTO -> teamDTO.getTeamLeaderPersonID()).distinct()
                .collect(Collectors.toList());
        List<EmployeeProfileElasticIndexDTO> employeeProfileElasticIndexDTOs = null;
        try {
	    	employeeProfileElasticIndexDTOs = teamUtil.fetchElasticEmployeeDetailsByEmployeeID(empIDs, null);
	    } catch (IOException e) {
			log.error("Exception while fetching user details from Elastic : " , e);
		}
        Map<String, EmployeeProfileElasticIndexDTO> employeeProfileElasticIndexDTOsMap = new HashMap<>();
        if(CollectionUtils.isNotEmpty(employeeProfileElasticIndexDTOs)) {
        	employeeProfileElasticIndexDTOsMap.putAll(employeeProfileElasticIndexDTOs.stream()
                    .collect(Collectors.toMap(EmployeeProfileElasticIndexDTO::getEmplid, Function.identity())));
		    teamDTOs.forEach(dto -> Optional.ofNullable(dto.getTeamLeaderPersonID()).ifPresent(x -> {
		        EmployeeProfileElasticIndexDTO personDTO = employeeProfileElasticIndexDTOsMap.get(x);
		        Optional.ofNullable(personDTO).ifPresent(p -> {
		            dto.setTeamLeaderPersonFirstName(camelCaseNames(Optional.ofNullable(p.getPersonDTO()).map(PersonDTO::getPrefName).orElse("")));
		            dto.setTeamLeaderPersonLastName(camelCaseNames(p.getLastName()));
		        });
		    }));
        }
        
    }

    private List<Predicate> createFilterPredicates(TeamSearchDTO teamSearchDTO, CriteriaBuilder criteriaBuilder,
            Root<Team> teamRoot, Join<Team, TeamMemberTeamAssignment> teamMemberTeamAssignment) {
        List<Predicate> filterPredicates = new ArrayList<>();
        if(StringUtils.isNotEmpty(teamSearchDTO.getSearchCriteria()) && teamSearchDTO.getSearchCriteria().length() >= 3) {
        	List<Predicate> searchParameters = new ArrayList<>();
        	String searchCriteria = teamSearchDTO.getSearchCriteria();
        	Predicate teamName = criteriaBuilder.like(teamRoot.get(TEAM_NAME), "%" + searchCriteria + "%");
        	searchParameters.add(teamName);
        	List<TeamMemberTaskAssignmentRoleAssociation> teamMemberTaskAssignmentRoleAssociations = teamMemberTaskAssignmentRoleAssociationRepository.
        			findByTaskGroupRoleTypeAssociationTaskGroupTaskGroupNameLikeAndExpirationTimestampAfter("%" + searchCriteria + "%", LocalDateTime.now());
        	List<Integer> teamIDs = teamMemberTaskAssignmentRoleAssociations.stream().map(association -> association.getTeam().getTeamID()).collect(Collectors.toList());
        	if(CollectionUtils.isNotEmpty(teamIDs)) {
        		In<Integer> teamIDin = criteriaBuilder.in(teamRoot.get(TEAM_ID));
                teamIDs.forEach(teamIDin::value);
                searchParameters.add(teamIDin);
        	}
            
            List<EmployeeProfileElasticIndexDTO> personIds = teamUtil.getEmployeeId(searchCriteria);
            if(CollectionUtils.isNotEmpty(personIds)) {
            	List<String> emplId = personIds.stream().map(EmployeeProfileElasticIndexDTO::getEmplid).collect(Collectors.toList());
            	In<String> teamLeaderIn = criteriaBuilder.in(teamRoot.get(TEAM_LEADER_PERSON_ID));
            	emplId.forEach(teamLeaderIn::value);
            	searchParameters.add(teamLeaderIn);
            }
            filterPredicates.add(criteriaBuilder.or(searchParameters.toArray(new Predicate[] {})));
        }
        if (!(teamSearchDTO.isActiveTeamsOnly() && teamSearchDTO.isInActiveTeamsOnly())) {
            Optional.ofNullable(teamSearchDTO.isActiveTeamsOnly()).filter(Boolean.TRUE::equals).ifPresent(status -> {
                Predicate activeTeams = criteriaBuilder
                        .greaterThan(teamRoot.get(TeamCommonConstants.EXPIRATION_TIMESTAMP), LocalDateTime.now());
                filterPredicates.add(activeTeams);
            });
            Optional.ofNullable(teamSearchDTO.isInActiveTeamsOnly()).filter(Boolean.TRUE::equals).ifPresent(status -> {
                Predicate inActiveTeams = criteriaBuilder
                        .lessThanOrEqualTo(teamRoot.get(TeamCommonConstants.EXPIRATION_TIMESTAMP), LocalDateTime.now());
                filterPredicates.add(inActiveTeams);
            });
        }
        // team leader personID filter
        if (Objects.nonNull(teamSearchDTO.getTeamLeaderPersonIDs())
                && CollectionUtils.isNotEmpty(teamSearchDTO.getTeamLeaderPersonIDs())) {
            In<String> teamLeaderpersonIDin = criteriaBuilder.in(teamRoot.get(TEAM_LEADER_PERSON_ID));
            teamSearchDTO.getTeamLeaderPersonIDs().forEach(teamLeaderpersonIDin::value);
            filterPredicates.add(teamLeaderpersonIDin);
        }

        return filterPredicates;
    }

    /**
     * @param teamName
     * @param pageable
     * @return
     */
    public Page<Team> findByTeamName(String teamName, Pageable pageable) {
        return Optional.ofNullable(teamName)
                .map(name -> teamRepository.findByTeamNameContainingAndExpirationTimestampAfter(teamName,
                        LocalDateTime.now(), pageable))
                .orElse(teamRepository.findByExpirationTimestampAfter(LocalDateTime.now(), pageable));
    }

    /**
     * @param personID
     * @return
     */
    public Map<String, List<TeamDTO>> findTeamLeaderNotification(String perID) {
    	String personID = getEmplId(perID);
        Map<String, List<TeamDTO>> resultMap = new HashMap<>();
        Optional.ofNullable(personID).ifPresent(perId -> {
            List<Team> teams = teamRepository.findByTeamLeaderPersonIDAndExpirationTimestampAfter(personID,
                    LocalDateTime.now());
            if (!teams.isEmpty()) {
                List<Integer> teamIDs = teams.stream().map(Team::getTeamID).collect(Collectors.toList());
                List<Integer> teamTaskRoleAssocationMemberIDs = teamMemberTaskAssignmentRoleAssociationRepository
                        .findByTeamTeamIDInAndExpirationTimestampAfter(teamIDs, LocalDateTime.now()).stream()
                        .filter(x -> Objects.nonNull(x.getTeamMemberTeamAssignment()))
                        .map(TeamMemberTaskAssignmentRoleAssociation::getTeamMemberTeamAssignment)
                        .map(TeamMemberTeamAssignment::getTeamMemberTeamAssignmentID).collect(Collectors.toList());
                if (!teamTaskRoleAssocationMemberIDs.isEmpty()) {
                    Set<String> mismatchEmplIDs = teamMemberTeamAssignmentRepository
                            .findByTeamMemberTeamAssignmentIDInAndExpirationTimestampBefore(
                                    teamTaskRoleAssocationMemberIDs, LocalDateTime.now())
                            .stream().map(TeamMemberTeamAssignment::getTeamMemberPersonID).collect(Collectors.toSet());
                    mismatchEmplIDs.forEach(emplID -> populateTaskAssignments(emplID, mismatchEmplIDs, resultMap));
                }
                enrichEOIDetails(resultMap);
            }
        });
        return resultMap;
    }

    private void populateTaskAssignments(String emplID, Set<String> mismatchEmplIDs,
            Map<String, List<TeamDTO>> resultMap) {
        List<TeamMemberTaskAssignmentRoleAssociation> assignmentss = teamMemberTaskAssignmentRoleAssociationRepository
                .findByTeamMemberTeamAssignmentTeamMemberPersonIDInAndExpirationTimestampAfter(
                        new ArrayList<String>(mismatchEmplIDs), LocalDateTime.now());
        List<TeamMemberTaskAssignmentRoleAssociation> valuess = assignmentss.stream()
                .filter(tt -> tt.getTeamMemberTeamAssignment().getTeamMemberPersonID().equals(emplID))
                .collect(Collectors.toList());
        Set<Integer> eachteamIDs = valuess.stream().map(TeamMemberTaskAssignmentRoleAssociation::getTeam)
                .map(Team::getTeamID).collect(Collectors.toSet());
        List<TeamDTO> teamDTOs = new ArrayList<>();
        eachteamIDs.forEach(id -> {
            TeamDTO teamDTO = new TeamDTO();
            Team team = valuess.stream().map(TeamMemberTaskAssignmentRoleAssociation::getTeam)
                    .filter(t -> t.getTeamID().equals(id)).findFirst().get();
            teamDTO.setTeamID(team.getTeamID());
            teamDTO.setTeamName(team.getTeamName());
            Set<TaskAssignmentDTO> taskAssignments = teamMapper.taskAssignmentsToTaskAssignmentDTOs(valuess.stream()
                    .filter(assign -> assign.getTeam().getTeamID().equals(id))
                    .map(TeamMemberTaskAssignmentRoleAssociation::getTaskAssignment).collect(Collectors.toSet()));
            teamDTO.setTaskAssignmentDTOs(taskAssignments);
            teamDTOs.add(teamDTO);
        });
        resultMap.put(emplID, teamDTOs);

    }

    private void enrichEOIDetails(Map<String, List<TeamDTO>> resultMap) {
        if (!resultMap.isEmpty()) {
            Map<String, AdHocPersonDTO> eoiDetails = teamUtil.getEOIDetailsByEmployeeIDs(new ArrayList<String>(resultMap.keySet()));
            resultMap.forEach((k, v) -> {
                TeamPersonDTO teampersonDTO = teamMapper
                        .convertToTeamPersonDTO(Optional.ofNullable(eoiDetails.get(k)).get(), new TeamPersonDTO());
                teampersonDTO.setFirstName(camelCaseNames(teampersonDTO.getFirstName()));
                teampersonDTO.setPreferredName(camelCaseNames(teampersonDTO.getPreferredName()));
                teampersonDTO.setLastName(camelCaseNames(teampersonDTO.getLastName()));
                List<TeamPersonDTO> teamPersonDTOs = new ArrayList<>();
                teamPersonDTOs.add(teampersonDTO);
                v.forEach(t -> t.setTeamPersonDTOs(teamPersonDTOs));
            });
        }
    }

    public List<TeamDTO> findTeammemberNotification(String perID) {
    	String personID = getEmplId(perID);
    	List<TeamDTO> teamDTOs = new ArrayList<TeamDTO>();
    	Optional.ofNullable(personID).ifPresent(perId -> {
	        List<Team> resultteams = new ArrayList<>();
	        List<TeamMemberTeamAssignment> teamMemberTeamAssignments = teamMemberTeamAssignmentRepository
	                .findByTeamMemberPersonIDAndExpirationTimestampAfter(personID, LocalDateTime.now());
	        if (CollectionUtils.isNotEmpty(teamMemberTeamAssignments)) {
	            List<Team> teams = teamMemberTeamAssignments.stream().map(TeamMemberTeamAssignment::getTeam)
	                    .collect(Collectors.toList());
	            teams.forEach(team -> {
	                if (Objects.isNull(team.getTeamLeaderPersonID())) {
	                    resultteams.add(team);
	                }
	            });
	            teamDTOs.addAll(teamMapper.teamsToTeamDTOs(resultteams));
	        }
    	});
        return teamDTOs;
    }

    /**
     * Remove Team members
     * 
     * @param personID
     * @return
     */
    public List<TeamMemberTeamAssignment> updateTeamAssignmentForRemovedIDs(String personID) {
        List<TeamMemberTeamAssignment> teamMemberTeamAssignments = new ArrayList<>();
        Optional.ofNullable(personID).ifPresent(perId -> {
            teamMemberTeamAssignments.addAll(teamMemberTeamAssignmentRepository
                    .findByTeamMemberPersonIDAndExpirationTimestampAfter(personID, LocalDateTime.now()));
            Optional.ofNullable(teamMemberTeamAssignments).ifPresent(teamAssignments -> {
                teamAssignments.forEach(assignment -> assignment.setExpirationTimestamp(LocalDateTime.now()));
                teamMemberTeamAssignmentRepository.save(teamAssignments);
            });
            List<Team> teams = teamRepository.findByTeamLeaderPersonIDAndExpirationTimestampAfter(personID,
                    LocalDateTime.now());
            Optional.ofNullable(teams).ifPresent(teamss -> {
                teamss.forEach(team -> team.setTeamLeaderPersonID(null));
                teamRepository.save(teamss);
            });
        });
        return teamMemberTeamAssignments;
    }

    /**
     * Export Team Details
     * 
     * @param teamSearchDTO
     * @param response
     * @throws JRException
     * @throws IOException
     */
    public void exportTeamDetails(TeamSearchDTO teamSearchDTO, HttpServletResponse response)
            throws JRException, IOException {
        List<TeamsDTO> teamDTOs = populateFilteredTeams(teamSearchDTO,null,null);
        enrichEOIEmployeeDetails(teamDTOs);
        enrichTaskDetails(teamDTOs);
        teamManagementReport.jasperReport(teamDTOs, response);

    }
    
    private String getEmplId(String perID){
    	String empId = null;
    	try {
			List<EmployeeProfileElasticIndexDTO> personDto = teamUtil.fetchElasticEmployeeDetailsByUserID(new ArrayList<String>(Arrays.asList(perID)), null);
			empId =  CollectionUtils.isNotEmpty(personDto) ? personDto.get(0).getEmplid()  :  null;
		} catch (IOException e) {
			log.error("Exception while fetching user details from Elastic : " , e);
		}
		return empId;
    	
    }
    
    private String camelCaseNames(String original) {
    	return  WordUtils.capitalizeFully(original);
    }
    
	public TeamDTO validateTeamMemberBeforeRemove(TeamDTO teamDTO) {
		TeamDTO resultTeamDTO = new TeamDTO();
		// team validation
		TeamValidationDTO teamValidationDTO = new TeamValidationDTO();
		List<Integer> removingPersonIDs = new ArrayList<Integer>();
		// update team- validations
		Optional.ofNullable(teamDTO.getTeamID()).ifPresent(teamID -> {
			List<TeamPersonDTO> removeTeamPersonDTOs = new ArrayList<>();
			List<TeamPersonDTO> teamPersonDTOs = Optional.ofNullable(teamDTO.getTeamPersonDTOs()).get();
			teamPersonDTOs.forEach(personDTO -> Optional.ofNullable(personDTO.getTeamMemberPersonID()).ifPresent(x1 -> removeTeamPersonDTOs.add(personDTO)));
			if (!removeTeamPersonDTOs.isEmpty()) {
				removingPersonIDs.addAll(removeTeamPersonDTOs.stream()
						.filter(removePersonDTO -> removePersonDTO.getTeamAssignmentExpirationTimestamp().isBefore(LocalDateTime.now()))
						.map(TeamPersonDTO::getTeamMemberPersonID).collect(Collectors.toList()));
			}
			// Defect 88687
			teamValidator.validateTeamMemberBeforeRemove(teamValidationDTO, removingPersonIDs);
		});

		if (MapUtils.isNotEmpty(teamValidationDTO.getErrors())) {
			log.info("Team Validation Error present");
			resultTeamDTO.setTeamValidationDTO(teamValidationDTO);
		}
		return resultTeamDTO;
	}
	
	private Map<Integer,TaskAssignmentDTO> getTaskDetails(Integer teamId, Map<Integer,TaskAssignmentDTO> taskMap)  {
        List<TaskAssignmentDTO> taskDTOs = new ArrayList<>();
        try {
            taskDTOs.addAll(teamUtil.fetchElasticTaskAssignmentDetailsByTeamID(teamId));
        } catch (IOException e) {
            log.error("Exception while fetching task details from Elastic : " , e);
        }
        taskDTOs.forEach(task -> taskMap.put(task.getTaskAssignmentID(), task));
		return taskMap;
    }
	
	private void performSort(String sortField, String sortDir, CriteriaQuery<TeamsDTO> criteriaQuery,CriteriaBuilder criteriaBuilder,Root<Team> teamRoot) {
		if(Objects.nonNull(sortField) && Objects.nonNull(sortDir)) {
    			switch(sortField) {
    			case TEAM_NAME :
    				if("asc".equalsIgnoreCase(sortDir)) {
    					criteriaQuery.orderBy(criteriaBuilder.asc(teamRoot.<String>get(TEAM_NAME)));
    				}else {
    					criteriaQuery.orderBy(criteriaBuilder.desc(teamRoot.<String>get(TEAM_NAME)));
    				}
    			break;
    			case TEAM_MEMBER_COUNT :
    				if("asc".equalsIgnoreCase(sortDir)) {
    					criteriaQuery.orderBy(criteriaBuilder.asc(criteriaBuilder.count(teamRoot)));
    				}else {
    					criteriaQuery.orderBy(criteriaBuilder.desc(criteriaBuilder.count(teamRoot)));
    				}
    			break;
    			default:
    				break;
    			}
    	}
	}
    
}
