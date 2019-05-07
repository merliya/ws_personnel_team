package com.jbhunt.personnel.team.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.jbhunt.personnel.team.Team;
import com.jbhunt.personnel.team.TeamMemberTeamAssignment;
import com.jbhunt.personnel.team.dto.TeamDTO;
import com.jbhunt.personnel.team.dto.TeamSearchDTO;
import com.jbhunt.personnel.team.dto.TeamTeamPersonDTO;
import com.jbhunt.personnel.team.dto.TeamsDTO;
import com.jbhunt.personnel.team.service.TeamService;

import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRException;

/**
 * Team Management Controller
 */
@Slf4j
@RepositoryRestController
public class TeamController {
    private final TeamService teamService;

    /**
     * @param teamService
     */
    public TeamController(TeamService teamService) {
        this.teamService = teamService;

    }

    /**
     * Find Team Details By TeamID
     * 
     * @param teamID
     * @return
     */
    @GetMapping(value = "/teams/fetchteam")
    public ResponseEntity<TeamDTO> findTeamDetailsByTeamID(@RequestParam(value = "teamID") Integer teamID) {
        log.debug("Team Controller:find Team Details By TeamID");
        return ResponseEntity.ok(teamService.findTeamDetailsByTeamID(teamID));
    }

    /**
     * Activate/Re-Activate Team
     * 
     * @param teamIDs
     * @return
     */
    @PatchMapping(value = "/teams/activateteam")
    public ResponseEntity<List<TeamDTO>> reActivateTeams(@RequestBody List<Integer> teamIDs) {
        log.info("Team Management Controller:Activate Team");
        return ResponseEntity.ok(teamService.reActivateTeams(teamIDs));
    }

    /**
     * Inactivate Team
     * 
     * @param teamIDs
     * @return
     */
    @PatchMapping(value = "/teams/inactivateteam")
    public ResponseEntity<List<TeamDTO>> inActivateTeam(@RequestBody List<Integer> teamIDs) {
        log.info("Team Management Controller:Inactivate Team");
        return ResponseEntity.ok(teamService.inActivateTeam(teamIDs));
    }

    /**
     * Find Teams by Person ID
     * 
     * @param personID
     * @return
     */
    @GetMapping(value = "/teams/findteambypersonid")
    public ResponseEntity<List<Team>> findTeamByPersonID(@RequestParam("personid") String personID) {
        log.info("Team Management Controller:Find Teams by Person ID");
        return ResponseEntity.ok(teamService.findTeamByPersonID(personID));
    }

    /**
     * Find Team And TeamMember based on teamID
     * 
     * @param teamIDs
     * @return
     */
    @GetMapping(value = "/teams/findteammemberbyteamids")
    public ResponseEntity<List<TeamTeamPersonDTO>> findTeamAndTeamMemberByTeamID(
            @RequestParam("teamIDs") List<Integer> teamIDs) {
        log.info("Team Management Controller:find Team And TeamMember based on teamID");
        return ResponseEntity.ok(teamService.findTeamAndTeamMemberByTeamID(teamIDs));
    }

    /**
     * Create team
     * 
     * @param team
     * @return
     */
    @PostMapping(value = "/teams/createteam")
    public ResponseEntity<TeamDTO> createTeam(@RequestBody TeamDTO team) {
        log.info("Team Management Controller:Create team");
        return ResponseEntity.ok(teamService.updateTeam(team));
    }

    /**
     * Update Team
     * 
     * @param teamDTO
     * @return
     */
    @PatchMapping(value = "/teams/updateteam")
    public ResponseEntity<TeamDTO> updateTeam(@RequestBody TeamDTO teamDTO) {
        log.info("Team Management Controller:Update Team");
        return ResponseEntity.ok(teamService.updateTeam(teamDTO));
    }

    /**
     * Team Search Page
     * 
     * @param teamSearchDTO
     * @param pageable
     * @return
     * @throws IOException
     */
    @PostMapping(value = "/teams/search/findteams")
    public ResponseEntity<Page<TeamsDTO>> findAllTeams(@RequestBody(required = false) TeamSearchDTO teamSearchDTO,
            Pageable pageable, @RequestParam(name="sortField",required = false) String sortField, 
            @RequestParam(name="sortDir",required = false) String sortDir) throws IOException {
        log.info("Team Management Controller:Team search page");
        return ResponseEntity.ok(teamService.findAllTeams(teamSearchDTO, pageable,sortField,sortDir));
    }

    /**
     * Find Team By Team name
     * 
     * @param teamName
     * @param pageable
     * @return
     */
    @GetMapping(value = "/teams/findteambyteamname")
    public ResponseEntity<Page<Team>> findByTeamName(
            @RequestParam(value = "teamname", required = false) String teamName, Pageable pageable) {
        log.info("Team Management Controller:Find Team By Team name");
        return ResponseEntity.ok(teamService.findByTeamName(teamName, pageable));
    }

    /**
     * Team Leader Notification
     * 
     * @param personID
     * @return
     */
    @GetMapping(value = "/teams/teamleadernotification")
    public ResponseEntity<Map<String, List<TeamDTO>>> findTeamLeaderNotification(
            @RequestParam("personID") String personID) {
        return ResponseEntity.ok(teamService.findTeamLeaderNotification(personID));
    }

    /**
     * Team Member Notification
     * 
     * @param personID
     * @return
     */
    @GetMapping(value = "/teams/teammembernotification")
    public ResponseEntity<List<TeamDTO>> findTeammemberNotification(@RequestParam("personID") String personID) {
        List<TeamDTO> resultTeamDTOs = new ArrayList<>();
        Optional.ofNullable(personID)
                .ifPresent(id -> resultTeamDTOs.addAll(teamService.findTeammemberNotification(id)));
        return ResponseEntity.ok(resultTeamDTOs);
    }

    /**
     * Remove Team members
     * 
     * @param personID
     */
    @PutMapping(value = "/teams/removeteammember")
    public ResponseEntity<List<TeamMemberTeamAssignment>> updateTeamAssignmentForRemovedIDs(
            @RequestParam("personid") String personID) {
        log.info("Team Management Controller:Update Team Member Team Assignment For Person IDs");
        return ResponseEntity.ok(teamService.updateTeamAssignmentForRemovedIDs(personID));
    }

    @PostMapping(value = "/teams/export")
    public void exportTaskAssignments(@RequestBody(required = false)TeamSearchDTO teamSearchDTO, final HttpServletResponse response)
            throws JRException, IOException {
    	log.info("Team Management Controller: exportTeamsToExcel");
        teamService.exportTeamDetails(teamSearchDTO, response);
    }
    /**
     * validateTeamMemberBeforeRemove
     * 
     * @param teamDTO
     * @return
     */
    @PatchMapping(value = "/teams/validateTeamMemberBeforeRemove")
    public ResponseEntity<TeamDTO> validateTeamMemberBeforeRemove(@RequestBody TeamDTO teamDTO) {
        log.info("Team Management Controller:Validate Team Member Before Remove");
        return ResponseEntity.ok(teamService.validateTeamMemberBeforeRemove(teamDTO));
    }
}