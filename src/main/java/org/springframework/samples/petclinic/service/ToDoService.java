package org.springframework.samples.petclinic.service;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.samples.petclinic.model.ToDo;
import org.springframework.samples.petclinic.repository.ToDoRepository;
import org.springframework.samples.petclinic.validation.ToDoLimitMilestoneException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ToDoService {

    private ToDoRepository toDoRepository;

    @Autowired
    public ToDoService(ToDoRepository toDoRepository) {
        this.toDoRepository = toDoRepository;
    }

    @Transactional(rollbackFor = ToDoLimitMilestoneException.class)
    public void saveToDo(ToDo toDo) throws DataAccessException, ToDoLimitMilestoneException {
        log.info("" + toDo.toString());
        if (toDo.getDone() || findToDoByMilestoneAndUser(toDo.getMilestone().getId(), toDo.getAssignee().getId())
                .stream().filter(t -> !t.getDone()).count() < 7L) {
            toDoRepository.save(toDo);
        } else {
            throw new ToDoLimitMilestoneException();
        }
    }

    @Transactional(readOnly = true)
    public ToDo findToDoById(Integer toDoId) {
        return toDoRepository.findById(toDoId);
    }

    @Transactional(readOnly = true)
    public void deleteToDoById(Integer toDoId) throws DataAccessException {
        toDoRepository.deleteById(toDoId);
    }

    @Transactional(readOnly = true)
    public Collection<ToDo> findToDoByUser(Integer userId) {
        return toDoRepository.findToDoByUser(userId);
    }

    @Transactional(readOnly = true)
    public Collection<ToDo> findToDoByMilestoneAndUser(Integer milestoneId, Integer userId) {
        return toDoRepository.findToDoByMilestoneAndUser(milestoneId, userId);
    }

}
