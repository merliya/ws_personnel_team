package com.jbhunt.personnel.team.handler;

import java.util.List;

import org.apache.camel.ProducerTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.jbhunt.personnel.team.dto.TeamDTO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TeamEventListener {
    private final ProducerTemplate producerTemplate;

    public TeamEventListener(ProducerTemplate producerTemplate) {
        this.producerTemplate = producerTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleGuaranteeListEvents(TeamEvent<List<TeamDTO>> teamEvent) {
        log.info("TeamEventListener:handleGuaranteeListEvents");
        List<TeamDTO> teamDTOs = teamEvent.getTeamDTOs();
        producerTemplate.requestBody("direct:postTeamToTopic", teamDTOs);
    }

}
