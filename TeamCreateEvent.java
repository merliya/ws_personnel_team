package com.jbhunt.personnel.team.handler;

import java.util.List;

import com.jbhunt.personnel.team.dto.EventType;
import com.jbhunt.personnel.team.dto.TeamDTO;

public class TeamCreateEvent implements TeamEvent<List<TeamDTO>> {
	private List<TeamDTO> teamDTOs;
	private EventType eventType;

	public TeamCreateEvent(List<TeamDTO> teamDTOs ,EventType eventType) {
		this.teamDTOs = teamDTOs;
		this.eventType=eventType;
	}
	
	@Override
	public List<TeamDTO> getTeamDTOs() {
		return teamDTOs;
	}
	
	@Override
    public EventType getEventType() {
        return eventType;
    }

}
