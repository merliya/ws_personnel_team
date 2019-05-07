package com.jbhunt.personnel.team.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.stereotype.Repository;

import com.jbhunt.personnel.team.Team;

/**
 * Team Repository
 * 
 *
 */
@Repository
public interface TeamRepository extends JpaRepository<Team, Integer>,QueryDslPredicateExecutor<Team>  {
	@Modifying
	@Query("UPDATE Team SET expirationTimestamp = :expirationTimestamp WHERE teamID in (:teamIDs)")
	void updateTeamExpirationTimestamp(@Param("teamIDs") List<Integer> teamIDs,
			@Param("expirationTimestamp") LocalDateTime expirationTimestamp);
	
	List<Team> findByTeamIDIn(@Param("teamID") List<Integer> teamIDs);
	
	Team findByTeamIDAndExpirationTimestampAfter(@Param("teamID") Integer teamID, @Param("expirationTimeStamp") LocalDateTime expirationTimeStamp);
	
	Page<Team> findByExpirationTimestampAfter(@Param("currentDateTime")@DateTimeFormat(iso = ISO.DATE_TIME) LocalDateTime expirationTimestamp,Pageable pageable);
	
	Page<Team> findByTeamNameContainingAndExpirationTimestampAfter(@Param("teamName") String teamName,@Param("expirationTimestamp") @DateTimeFormat(iso = ISO.DATE_TIME) LocalDateTime expirationTimestamp,Pageable pageable);
	
	List<Team> findByTeamIDInAndTeamMemberTeamAssignmentsExpirationTimestampAfter(List<Integer> teamIDs,LocalDateTime expirationTimestamp);
	//for validation
	List<Team> findByTeamIDNotIn(Integer teamID);
	//for Notification
	List<Team> findByTeamLeaderPersonIDAndExpirationTimestampAfter(String teamLeaderPersonID,LocalDateTime expirationTimestamp);

}
