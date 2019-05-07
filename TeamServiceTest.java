package com.jbhunt.personnel.team.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.jbhunt.hrms.EOIAPIUtil.apiutil.EOIAPIUtil;
import com.jbhunt.hrms.EOIAPIUtil.exception.AdHocSearchParseException;
import com.jbhunt.hrms.EOIAPIUtil.exception.AuditInformationException;
import com.jbhunt.hrms.EOIAPIUtil.exception.EOIAPIException;
import com.jbhunt.hrms.EOIAPIUtil.exception.RecordNotFoundException;
import com.jbhunt.hrms.EOIAPIUtil.exception.SizeLimitExceededException;
import com.jbhunt.hrms.EOIAPIUtil.exception.TooManyRecordsException;
import com.jbhunt.hrms.EOIAPIUtil.exception.UnauthorizedException;
import com.jbhunt.hrms.EOIAPIUtil.util.UserCredentials;
import com.jbhunt.hrms.eoi.api.dto.adhoc.AdHocPersonDTO;
import com.jbhunt.hrms.eoi.api.dto.adhoc.AdHocPersonsDTO;
import com.jbhunt.hrms.eoi.api.dto.adhoc.AdHocPositionDTO;
import com.jbhunt.personnel.schedule.dto.TeamPersonDTO;
import com.jbhunt.personnel.team.AbstractTest;
import com.jbhunt.personnel.team.Team;
import com.jbhunt.personnel.team.config.TestSuiteConfig;
import com.jbhunt.personnel.team.dto.TeamDTO;
import com.jbhunt.personnel.team.dto.TeamSearchDTO;
import com.jbhunt.personnel.team.dto.TeamTeamPersonDTO;
import com.jbhunt.personnel.team.dto.TeamsDTO;
import com.jbhunt.personnel.team.mapper.TeamMapper;
import com.jbhunt.personnel.team.properties.TeamProperties;
import com.jbhunt.personnel.team.repository.TeamMemberTaskAssignmentRoleAssociationRepository;
import com.jbhunt.personnel.team.repository.TeamMemberTeamAssignmentRepository;
import com.jbhunt.personnel.team.repository.TeamRepository;
import com.jbhunt.personnel.team.util.TeamUtil;
import com.jbhunt.personnel.team.validation.TeamValidator;
import com.jbhunt.security.boot.autoconfig.enterprisesecurity.user.AuditUserInfo;
import com.jbhunt.security.boot.autoconfig.enterprisesecurity.user.JbhUser;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = { TestSuiteConfig.class }, initializers = {
        ConfigFileApplicationContextInitializer.class })
public class TeamServiceTest extends AbstractTest {
    @InjectMocks
    private TeamService teamService;
    @Mock
    public TeamProperties teamProperties;
    @Autowired
    private TeamMapper teamMapper;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private TeamMemberTeamAssignmentRepository teamMemberTeamAssignmentRepository;
    @Autowired
    private TeamMemberTaskAssignmentRoleAssociationRepository teamMemberTaskAssignmentRoleAssociationRepository;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    @Mock
    private EOIAPIUtil eoiAPIUtil;

    @Mock
    private UserCredentials userCredentials;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TeamManagementReport teamManagementReport;

    @Mock
    private TeamUtil teamUtil;

    @Mock
    private TeamValidator teamValidator;

    @Before
    public void init() {
        setUp("team");
        teamService = new TeamService(teamRepository, teamMemberTeamAssignmentRepository,
                teamMemberTaskAssignmentRoleAssociationRepository, teamMapper, entityManager,
                teamProperties, applicationEventPublisher, teamValidator, teamManagementReport, teamUtil);
    }

    @Test
    @Transactional
    public void updateTeamStatusInactivateTest() {
        List<Integer> teamIDs = Arrays.asList(6, 7);
        List<TeamDTO> teams = (List<TeamDTO>) teamService.inActivateTeam(teamIDs);
        assertNotEquals(LocalDate.of(2099, 12, 31), teams.stream().findFirst().get().getTeamExpirationTimestamp());

    }

    @Test
    @Transactional
    public void findTeamDetailsByTeamIDTest()
            throws RecordNotFoundException, TooManyRecordsException, AuditInformationException, EOIAPIException,
            AdHocSearchParseException, SizeLimitExceededException, UnauthorizedException {
        Mockito.doReturn(getEOIDetails()).when(eoiAPIUtil).getPeopleByCriteria(Mockito.anyObject(), Mockito.anyObject(),
                Mockito.any(UserCredentials.class));
        when(eoiAPIUtil.getPeopleByCriteria(Mockito.anyObject(), Mockito.anyObject(),
                Mockito.any(UserCredentials.class))).thenReturn(getEOIDetails());
        TeamDTO teamDTO = teamService.findTeamDetailsByTeamID(7);
        assertNotNull(teamDTO);
        assertEquals("Walmart Warriors team", teamDTO.getTeamName());
    }



    @Test
    @Transactional
    public void createTeamTest() {
        AuditUserInfo auditUserInfo = new AuditUserInfo("rcon412", "rcon412");
        auditUserInfo.setAuthenticatedUserDisplayName("Team Management");
        JbhUser jbhUser = new JbhUser("rcon412", auditUserInfo);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(jbhUser, null);
        SecurityContextHolder.getContext().setAuthentication(auth);
        TeamDTO dto = new TeamDTO();
        dto.setTeamName("Walmart Warrior");
        dto.setTeamLeaderPersonID("253699");
        TeamPersonDTO personDTO = new TeamPersonDTO();
        personDTO.setPersonEmployeeID("253699");
        TeamPersonDTO personDTO1 = new TeamPersonDTO();
        personDTO1.setPersonEmployeeID("253050");
        List<TeamPersonDTO> personDTOs = new ArrayList<>();
        personDTOs.add(personDTO);
        personDTOs.add(personDTO1);
        dto.setTeamPersonDTOs(personDTOs);
        when(teamProperties.getValueMinutes()).thenReturn(59);
        when(teamProperties.getValueDay()).thenReturn(31);
        when(teamProperties.getValueMonth()).thenReturn(12);
        when(teamProperties.getValueHour()).thenReturn(23);
        when(teamProperties.getValueMinutes()).thenReturn(59);
        when(teamProperties.getValueYear()).thenReturn(2099);
        TeamDTO team = teamService.updateTeam(dto);
        assertNotNull(team);
    }

    @Test
    @Transactional
    public void updateTeamTest() {
        AuditUserInfo auditUserInfo = new AuditUserInfo("rcon412", "rcon412");
        auditUserInfo.setAuthenticatedUserDisplayName("Team Management");
        JbhUser jbhUser = new JbhUser("rcon412", auditUserInfo);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(jbhUser, null);
        SecurityContextHolder.getContext().setAuthentication(auth);
        TeamDTO dto = new TeamDTO();
        dto.setTeamID(6);
        dto.setTeamName("Walmart Warrior");
        dto.setTeamLeaderPersonID("253658");
        dto.setTeamEffectiveTimestamp(LocalDateTime.of(2017, 07, 17, 00, 00));
        dto.setTeamExpirationTimestamp(LocalDateTime.of(2099, 12, 27, 23, 59));
        TeamPersonDTO personDTO = getTeamPersonDTO(4, "253658", LocalDateTime.of(2017, 07, 17, 00, 00),
                LocalDateTime.of(2099, 12, 27, 23, 59));
        TeamPersonDTO personDTO1 = getTeamPersonDTO(5, "253050", LocalDateTime.of(2017, 07, 17, 00, 00),
                LocalDateTime.now());
        TeamPersonDTO personDTO2 = new TeamPersonDTO();
        personDTO2.setPersonEmployeeID("245788");
        List<TeamPersonDTO> personDTOs = new ArrayList<>();
        personDTOs.add(personDTO);
        personDTOs.add(personDTO1);
        personDTOs.add(personDTO2);
        dto.setTeamPersonDTOs(personDTOs);
        when(teamProperties.getValueMinutes()).thenReturn(59);
        when(teamProperties.getValueDay()).thenReturn(31);
        when(teamProperties.getValueMonth()).thenReturn(12);
        when(teamProperties.getValueHour()).thenReturn(23);
        when(teamProperties.getValueMinutes()).thenReturn(59);
        when(teamProperties.getValueYear()).thenReturn(2099);
        TeamDTO team = teamService.updateTeam(dto);
        assertEquals(team.getTeamExpirationTimestamp().toLocalDate(), LocalDate.of(2099, 12, 27));
        assertEquals(team.getTeamPersonDTOs().stream()
                .filter(t -> t.getTeamAssignmentExpirationTimestamp().isAfter(LocalDateTime.now())).count(), 2);
        assertEquals(
                team.getTeamPersonDTOs().stream()
                        .filter(t -> t.getTeamAssignmentExpirationTimestamp().isBefore(LocalDateTime.now())).count(),
                1);
    }

    private TeamPersonDTO getTeamPersonDTO(int teamMemberPersonID, String personEmployeeID,
            LocalDateTime teamAssignmentEffectiveTimestamp, LocalDateTime teamAssignmentExpirationTimestamp) {
        TeamPersonDTO teamPersonDTO = new TeamPersonDTO();
        teamPersonDTO.setTeamMemberPersonID(Optional.ofNullable(teamMemberPersonID).get());
        teamPersonDTO.setPersonEmployeeID(Optional.ofNullable(personEmployeeID).get());
        teamPersonDTO.setTeamAssignmentEffectiveTimestamp(Optional.ofNullable(teamAssignmentEffectiveTimestamp).get());
        teamPersonDTO
                .setTeamAssignmentExpirationTimestamp(Optional.ofNullable(teamAssignmentExpirationTimestamp).get());
        return teamPersonDTO;
    }

    @Test
    @Transactional
    public void findTeamAndTeamMemberByTeamIDTest()
            throws RecordNotFoundException, TooManyRecordsException, AuditInformationException, EOIAPIException,
            AdHocSearchParseException, SizeLimitExceededException, UnauthorizedException {
        List<Integer> teamIDs = Arrays.asList(6, 7);
        when(eoiAPIUtil.getPeopleByCriteria(Mockito.anyObject(), Mockito.anyObject(),
                Mockito.any(UserCredentials.class))).thenReturn(getEOIDetails());
        List<TeamTeamPersonDTO> teamTeamPersons = teamService.findTeamAndTeamMemberByTeamID(teamIDs);
        assertEquals(2, teamTeamPersons.size());

    }

    @Test
    @Transactional
    public void findByTeamNameTest() {
        Pageable pageable = new PageRequest(0, 5);
        Page<Team> teams = teamService.findByTeamName("Warr", pageable);
        assertEquals(1, teams.getContent().size());
    }

    @Test
    @Transactional
    public void findByTeamNameAllTeamsTest() {
        Pageable pageable = new PageRequest(0, 5);
        Page<Team> teams = teamService.findByTeamName(null, pageable);
        assertEquals(2, teams.getContent().size());
    }

    @Test
    @Transactional
    public void findAllTeamsTest() throws RecordNotFoundException, TooManyRecordsException, AuditInformationException,
            EOIAPIException, AdHocSearchParseException, SizeLimitExceededException, UnauthorizedException, IOException {
        Mockito.doReturn(getEOIDetails()).when(eoiAPIUtil).getPeopleByCriteria(Mockito.anyObject(), Mockito.anyObject(),
                Mockito.any(UserCredentials.class));
        Pageable pageable = new PageRequest(0, 5);
        Page<TeamsDTO> resultTeams = teamService.findAllTeams(null, pageable,null,null);
        assertNotNull(resultTeams);
        ;
    }

    @Test
    @Transactional
    public void findAllTeamsOnlyActiveTeamsTest()
            throws RecordNotFoundException, TooManyRecordsException, AuditInformationException, EOIAPIException,
            AdHocSearchParseException, SizeLimitExceededException, UnauthorizedException, IOException {
        Mockito.doReturn(getEOIDetails()).when(eoiAPIUtil).getPeopleByCriteria(Mockito.anyObject(), Mockito.anyObject(),
                Mockito.any(UserCredentials.class));
        Pageable pageable = new PageRequest(0, 5);
        TeamSearchDTO searchDTO = new TeamSearchDTO();
        searchDTO.setActiveTeamsOnly(true);
        Page<TeamsDTO> resultTeams = teamService.findAllTeams(searchDTO, pageable,null,null);
        assertEquals(1, resultTeams.getContent().size());
    }

    @Test
    @Transactional
    public void findAllTeamsOnlyInActiveTeamsTest()
            throws RecordNotFoundException, TooManyRecordsException, AuditInformationException, EOIAPIException,
            AdHocSearchParseException, SizeLimitExceededException, UnauthorizedException, IOException {
        Mockito.doReturn(getEOIDetails()).when(eoiAPIUtil).getPeopleByCriteria(Mockito.anyObject(), Mockito.anyObject(),
                Mockito.any(UserCredentials.class));
        Pageable pageable = new PageRequest(0, 5);
        TeamSearchDTO searchDTO = new TeamSearchDTO();
        searchDTO.setInActiveTeamsOnly(true);
        Page<TeamsDTO> resultTeams = teamService.findAllTeams(searchDTO, pageable,null,null);
        assertEquals(1, resultTeams.getContent().size());
    }

    @Test
    @Transactional
    public void findAllTeamsForRoleCodeTest()
            throws RecordNotFoundException, TooManyRecordsException, AuditInformationException, EOIAPIException,
            AdHocSearchParseException, SizeLimitExceededException, UnauthorizedException, IOException {
        Mockito.doReturn(getEOIDetails()).when(eoiAPIUtil).getPeopleByCriteria(Mockito.anyObject(), Mockito.anyObject(),
                Mockito.any(UserCredentials.class));
        Pageable pageable = new PageRequest(0, 5);
        TeamSearchDTO searchDTO = new TeamSearchDTO();
        List<String> roleCodes = new ArrayList<>();
        roleCodes.add("OrderOwner");
        searchDTO.setRoleCodes(roleCodes);
        Page<TeamsDTO> resultTeams = teamService.findAllTeams(searchDTO, pageable,null,null);
        // assertNotNull(resultTeams);;
    }

    @Test
    @Transactional
    public void findTeamByPersonIDTest() {
        List<Team> resultTeams = teamService.findTeamByPersonID("245788");
        assertEquals(0, resultTeams.size());
    }

    @Test
    @Transactional
    public void findTeamByPersonIDTest1() {
        List<Team> resultTeams = teamService.findTeamByPersonID("253050");
        assertEquals(1, resultTeams.size());
    }

    @Test
    @Transactional
    public void findTeammemberNotificationTest() {
        List<TeamDTO> resultTeams = teamService.findTeammemberNotification("253050");
        // assertEquals(1, resultTeams.size());
    }

    @Test
    @Transactional
    public void findTeamLeaderNotificationTest() {
        Map<String, List<TeamDTO>> resultMap = teamService.findTeamLeaderNotification("253658");
        // assertEquals(1, resultMap.size());
    }

    /*
     * @Test
     * 
     * @Transactional public void findAllTeamsForSearchCriteriaTest() throws
     * RecordNotFoundException, TooManyRecordsException,
     * AuditInformationException, EOIAPIException, AdHocSearchParseException,
     * SizeLimitExceededException, UnauthorizedException{
     * Mockito.doReturn(getEOIDetails()).when(eoiAPIUtil).getPeopleByCriteria(
     * Mockito.anyObject(), Mockito.anyObject(),
     * Mockito.any(UserCredentials.class)); Pageable pageable = new
     * PageRequest(0, 5); TeamSearchDTO searchDTO=new TeamSearchDTO();
     * searchDTO.setSearchCriteria("wal"); List<TeamsDTO>
     * resultTeams=teamService.findAllTeams(searchDTO, pageable);
     * assertNotNull(resultTeams);; }
     */

    private AdHocPersonsDTO getEOIDetails() {
        AdHocPersonsDTO adHocPersonsDTOs = new AdHocPersonsDTO();
        List<AdHocPersonDTO> people = new ArrayList<>();
        people.add(getAdHocPersonDTO("253658", "DAVA99", "ALVERTIS", "DAVIS", "", "Target Class A Driver Regional"));
        people.add(getAdHocPersonDTO("253050", "JITM489", "ANTHONY", "LIPFORD", "J",
                "Logistics Coordinator Non Dedicated"));
        adHocPersonsDTOs.setPeople(people);
        return adHocPersonsDTOs;
    }

    private AdHocPersonDTO getAdHocPersonDTO(String emplID, String userID, String firsntname, String lastName,
            String middleName, String positiondescription) {
        AdHocPersonDTO adHocPersonDTO = new AdHocPersonDTO();
        adHocPersonDTO.setUserID(userID);
        adHocPersonDTO.setFirstName(firsntname);
        adHocPersonDTO.setLastName(lastName);
        adHocPersonDTO.setEmplid(emplID);
        adHocPersonDTO.setMiddleName(middleName);
        AdHocPositionDTO positionDTO = new AdHocPositionDTO();
        positionDTO.setPositionDescription(positiondescription);
        List<AdHocPositionDTO> positions = new ArrayList<>();
        positions.add(positionDTO);
        adHocPersonDTO.setPositions(positions);
        return adHocPersonDTO;
    }

}