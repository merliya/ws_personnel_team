package com.jbhunt.personnel.team.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.joda.time.LocalDateTime;
import org.springframework.stereotype.Service;

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
import com.jbhunt.personnel.schedule.dto.EmployeeProfileElasticIndexDTO;
import com.jbhunt.personnel.schedule.dto.TaskAssignmentDTO;
import com.jbhunt.personnel.team.configuration.JestConfiguration;
import com.jbhunt.personnel.team.constants.TeamCommonConstants;
import com.jbhunt.personnel.team.properties.TeamElasticProperties;

import io.searchbox.client.JestResult;
import io.searchbox.core.Search;
import lombok.extern.slf4j.Slf4j;

/**
 * Team Management services
 *
 */

@Slf4j
@Service
public class TeamUtil {

    private final EOIAPIUtil eoiapiUtil;
    private final UserCredentials userCredentials;
    private final JestConfiguration jestConfiguration;
    private final TeamElasticProperties teamElasticProperties;

    public TeamUtil(EOIAPIUtil eoiapiUtil, UserCredentials userCredentials, JestConfiguration jestConfiguration,
            TeamElasticProperties teamElasticProperties) {
        this.eoiapiUtil = eoiapiUtil;
        this.userCredentials = userCredentials;
        this.jestConfiguration = jestConfiguration;
        this.teamElasticProperties = teamElasticProperties;
    }

    /**
     * Method to fetch EOI details based on person Employee ID
     * 
     * @param empIDs
     * @return
     */
    public Map<String, AdHocPersonDTO> getEOIDetailsByEmployeeIDs(List<String> empIDs) {
        AdHocPersonsDTO adHocPersonsDTOs = new AdHocPersonsDTO();
        StringJoiner empids = new StringJoiner("','");
        empIDs.forEach(empids::add);
        try {
            String search = "Person.userid,Person.emplId,Person.firstName,Person.middleName,Person.preferredName,"
                    + "Person.lastName,Position.PositionDescription";
            String criteria = "Person.emplId IN ('" + empids.toString() + "')";
            adHocPersonsDTOs = eoiapiUtil.getPeopleByCriteria(search, criteria, userCredentials);
        } catch (RecordNotFoundException | TooManyRecordsException | AuditInformationException | EOIAPIException
                | AdHocSearchParseException | SizeLimitExceededException | UnauthorizedException e) {
            log.error("Team Service: getEOIDetailsByEmployeeIDs" + e);
        }
        Map<String, AdHocPersonDTO> eoiDetails = new HashMap<>();
        Optional.ofNullable(adHocPersonsDTOs.getPeople()).ifPresent(
                people -> people.forEach(eoiPersonDTO -> eoiDetails.put(eoiPersonDTO.getEmplid(), eoiPersonDTO)));
        return eoiDetails;
    }

    /**
     * @param employeeIDs
     * @param typeAhead
     * @return
     * @throws IOException
     */
    public List<EmployeeProfileElasticIndexDTO> fetchElasticEmployeeDetailsByEmployeeID(List<String> employeeIDs,
            String typeAhead) throws IOException {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder query = QueryBuilders.boolQuery()
                .must(QueryBuilders.termsQuery(TeamCommonConstants.EMPLOYEE_ID,
                        employeeIDs.stream().filter(x -> Objects.nonNull(x)).collect(Collectors.toList())))
                .must(QueryBuilders.matchQuery("isActive", true));
        Optional.ofNullable(typeAhead).ifPresent(typeAheadTxt -> query.must(
                QueryBuilders.multiMatchQuery(typeAhead, "firstName", "lastName", "middleName").type("phrase_prefix")));
        searchSourceBuilder.size(employeeIDs.size()).query(query);
        Search search = new Search.Builder(searchSourceBuilder.toString())
                .addIndex(teamElasticProperties.getEmployeeindex()).addType(teamElasticProperties.getEmployeetype())
                .build();
        JestResult result = jestConfiguration.openClient().execute(search);
        return result.getSourceAsObjectList(EmployeeProfileElasticIndexDTO.class);
    }

    public List<TaskAssignmentDTO> fetchElasticTaskAssignmentDetailsByTeamID(Integer teamID) throws IOException {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder query = QueryBuilders.boolQuery()
                .must(QueryBuilders.termQuery(TeamCommonConstants.TEAM_TASK_ID, teamID))
                .must(QueryBuilders.rangeQuery(TeamCommonConstants.TEAM_TASK_EXPIRATION_TIMESTAMP)
                        .gte(LocalDateTime.now()))
                .must(QueryBuilders.rangeQuery(TeamCommonConstants.EXPIRATION_TIMESTAMP).gte(LocalDateTime.now()));
        searchSourceBuilder.size(TeamCommonConstants.RESULTSIZE).query(query);
        List<TaskAssignmentDTO> taskAssignmentDTOList = new ArrayList<>();
        Search search = new Search.Builder(searchSourceBuilder.toString())
                .addIndex(teamElasticProperties.getTaskindex()).addType(teamElasticProperties.getTasktype()).build();
        JestResult result = jestConfiguration.openClient().execute(search);
        Optional.ofNullable(result.getSourceAsObjectList(TaskAssignmentDTO.class))
                .ifPresent(taskAssignmentDTOList::addAll);

        return taskAssignmentDTOList;
    }
    
    public List<EmployeeProfileElasticIndexDTO> fetchElasticEmployeeDetailsByUserID(List<String> userIDs,
            String typeAhead) throws IOException {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder query = QueryBuilders.boolQuery()
                .must(QueryBuilders.termsQuery(TeamCommonConstants.USER_ID, userIDs))
                .must(QueryBuilders.matchQuery("isActive", true));
        Optional.ofNullable(typeAhead).ifPresent(typeAheadTxt -> query.must(
                QueryBuilders.multiMatchQuery(typeAhead, "firstName", "lastName", "middleName").type("phrase_prefix")));
        searchSourceBuilder.size(userIDs.size()).query(query);
        Search search = new Search.Builder(searchSourceBuilder.toString())
                .addIndex(teamElasticProperties.getEmployeeindex())
                .addType(teamElasticProperties.getEmployeetype()).build();
        JestResult result = jestConfiguration.openClient().execute(search);
        return result.getSourceAsObjectList(EmployeeProfileElasticIndexDTO.class);
    }
    
    /**
     * Fetch employee details from EOI
     * 
     * @param userIDs
     * @param typeAheadText
     * @return
     */
    public List<EmployeeProfileElasticIndexDTO> getEmployeeId(String typeAheadText) {
    	 SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
         List<EmployeeProfileElasticIndexDTO> employeeList = new ArrayList<>();
    	 BoolQueryBuilder query = QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("isActive", true));
         Optional.ofNullable(typeAheadText).ifPresent(typeAheadTxt -> query.must(
                 QueryBuilders.multiMatchQuery(typeAheadText, "firstName", "lastName", "personDTO.prefName").type("phrase_prefix")));
         searchSourceBuilder.size(200).query(query);
         Search search = new Search.Builder(searchSourceBuilder.toString()).addIndex(teamElasticProperties.getEmployeeindex())
                 .addType(teamElasticProperties.getEmployeetype()).build();
         JestResult result;
		try {
			result = jestConfiguration.openClient().execute(search);
			employeeList = result.getSourceAsObjectList(EmployeeProfileElasticIndexDTO.class);
		} catch (IOException e) {
			log.error("Exception while fetching user details from Elastic : " , e);
		}
         return employeeList;

    }
}
