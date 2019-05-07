package com.jbhunt.personnel.team.controller;

import static org.mockito.Matchers.isA;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jbhunt.hrms.eoi.api.dto.adhoc.AdHocPersonDTO;
import com.jbhunt.personnel.schedule.dto.EmployeeProfileElasticIndexDTO;
import com.jbhunt.personnel.schedule.dto.TeamPersonDTO;
import com.jbhunt.personnel.team.AbstractTest;
import com.jbhunt.personnel.team.config.TestSuiteConfig;
import com.jbhunt.personnel.team.dto.TeamDTO;
import com.jbhunt.personnel.team.util.TeamUtil;
import com.jbhunt.security.boot.autoconfig.enterprisesecurity.user.AuditUserInfo;
import com.jbhunt.security.boot.autoconfig.enterprisesecurity.user.JbhUser;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = { TestSuiteConfig.class }, initializers = {
		ConfigFileApplicationContextInitializer.class })
public class TeamControllerTest extends AbstractTest {

	@Autowired
	private WebApplicationContext context;

	private ObjectMapper objectMapper = new ObjectMapper();
	
	@MockBean
	private TeamUtil teamUtil;

	@Rule
	public final JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation("target/generated-snippets");
	private RestDocumentationResultHandler document;
	private MockMvc mockMvc;

	@SuppressWarnings("unchecked")
	@Before
	public void setup() throws Exception {
		setUp("team");
		this.document = document("{method-name}");
		mockMvc = MockMvcBuilders.webAppContextSetup(context)
				.apply(documentationConfiguration(this.restDocumentation).uris().withScheme("https"))
				.alwaysDo(this.document).build();
		JbhUser jbhUser = Mockito.mock(JbhUser.class);
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		Authentication authentication = Mockito.mock(Authentication.class);
		Principal principal = Mockito.mock(Principal.class);
		Mockito.when(authentication.getPrincipal()).thenReturn(principal);
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
		AuditUserInfo auditUserInfo = new AuditUserInfo("rcon641", "rcon641");
		auditUserInfo.setAuthenticatedUserDisplayName("Team Management");
		Mockito.when(jbhUser.getAuditUserInfo()).thenReturn(auditUserInfo);
		Mockito.when(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
				.thenReturn(new JbhUser("rcon641", auditUserInfo));
		EmployeeProfileElasticIndexDTO emp =  new EmployeeProfileElasticIndexDTO();
		emp.setEmplid("320273");
		AdHocPersonDTO a =  new AdHocPersonDTO();
		List<EmployeeProfileElasticIndexDTO> elasticList = new ArrayList<>();
		elasticList.add(emp);
		Map<String, AdHocPersonDTO> eoiDetails = new HashMap<String, AdHocPersonDTO>();
		eoiDetails.put("320273", a);
		Mockito.when(teamUtil.fetchElasticEmployeeDetailsByEmployeeID(isA(List.class), isA(String.class)))
        .thenReturn(elasticList);
		Mockito.when(teamUtil.getEOIDetailsByEmployeeIDs(isA(List.class)))
        .thenReturn(eoiDetails);
		Mockito.when(teamUtil.fetchElasticEmployeeDetailsByUserID(isA(List.class), isA(String.class)))
        .thenReturn(elasticList);
	}

	@Test
	public void findTeamDetailsByTeamID() throws Exception {
		this.mockMvc.perform(get("/teams/fetchteam").param("teamID", "1").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andDo(document);
	}

	@Test
	public void findTeamByPersonIDTest() throws Exception {
		this.mockMvc.perform(get("/teams/findteambypersonid").param("personid", "1").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andDo(document);
	}

	@Test
	public void createTeamTest() throws Exception {
		this.mockMvc
				.perform(post("/teams/createteam").contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(populateTeamDTO())))
				.andExpect(status().isOk()).andDo(document);
	}

	@Test
	public void updateTeamTest() throws Exception {
		this.mockMvc
				.perform(patch("/teams/updateteam").contentType(MediaType.APPLICATION_JSON)
						.content(this.objectMapper.writeValueAsString(populateTeamDTO())))
				.andExpect(status().isOk()).andDo(document);
	}

	@Test
	public void findByTeamNameTest() throws Exception {
		this.mockMvc
				.perform(get("/teams/findteambyteamname").param("teamname", "Order,Admin").param("page", "0")
						.param("size", "1").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andDo(document);
	}

	@Test
	public void findTeamLeaderNotificationTest() throws Exception {
		this.mockMvc
				.perform(get("/teams/teamleadernotification").param("personID", "jcnt129").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andDo(document);

	}

	@Test
	public void findTeammemberNotificationTest() throws Exception {
		this.mockMvc
				.perform(get("/teams/teammembernotification").param("personID", "jcnt129").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andDo(document);

	}

	@Test
	public void updateTeamAssignmentForRemovedIDsTest() throws Exception {
		this.mockMvc.perform(put("/teams/removeteammember").param("personid", "1").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andDo(document);
	}

	private TeamDTO populateTeamDTO() {
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
		return dto;
	}
}
