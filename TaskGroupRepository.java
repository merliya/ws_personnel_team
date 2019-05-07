package com.jbhunt.personnel.team.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jbhunt.infrastructure.taskreferencedata.entity.TaskGroup;

/**
 * TaskGroupRepository
 *
 */
public interface TaskGroupRepository extends JpaRepository<TaskGroup, Integer> {

}
