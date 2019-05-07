package com.jbhunt.personnel.team.handler;

import java.util.List;

import com.jbhunt.personnel.team.dto.EventType;
import com.jbhunt.personnel.team.dto.TeamDTO;

public interface TeamEvent<T> {
	/**
	 * @return
	 */
	public List<TeamDTO> getTeamDTOs();
	public EventType getEventType();
}





