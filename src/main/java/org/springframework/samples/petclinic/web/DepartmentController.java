package org.springframework.samples.petclinic.web;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.model.Department;
import org.springframework.samples.petclinic.model.Role;
import org.springframework.samples.petclinic.model.Team;
import org.springframework.samples.petclinic.model.UserTW;
import org.springframework.samples.petclinic.service.DepartmentService;
import org.springframework.samples.petclinic.service.TeamService;
import org.springframework.samples.petclinic.service.UserTWService;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DepartmentController {
	private final DepartmentService departmentService;
	private final TeamService teamService;
	private final UserTWService userTWService;

	@Autowired
	public DepartmentController(DepartmentService departmentService, TeamService teamService,
			UserTWService userTWService) {
		this.departmentService = departmentService;
		this.teamService = teamService;
		this.userTWService = userTWService;
	}

	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@GetMapping(value = "/api/departments")

	public List<Department> getDeparments(HttpServletRequest r) {
		List<Department> l = new ArrayList<>();
		Integer teamId = (Integer) r.getSession().getAttribute("teamId");
		l = teamService.findTeamById(teamId).getDepartments();
		return l;
	}

	@PostMapping(value = "/api/departments")
	public ResponseEntity<String> postDeparments(@RequestParam(required = true) Integer teamId,
			@RequestBody Department department, HttpServletRequest r) {

		try {
			Integer userId = (Integer) r.getSession().getAttribute("userId");
			UserTW user = userTWService.findUserById(userId);
			if (user.getRole().equals(Role.team_owner)) {

				Team team = teamService.findTeamById(teamId);
				department.setTeam(team);
				departmentService.saveDepartment(department);
				return ResponseEntity.ok("Department create");
			} else {
				return ResponseEntity.status(403).build();
			}

		} catch (DataAccessException d) {
			return ResponseEntity.badRequest().build();
		}

	}

	@DeleteMapping(value = "/api/departments")
	public ResponseEntity<String> deleteDeparments(@RequestParam(required = true) Integer departmentId,
			HttpServletRequest r) {

		try {
			Integer userId = (Integer) r.getSession().getAttribute("userId");
			UserTW user = userTWService.findUserById(userId);
			if (user.getRole().equals(Role.team_owner)) {
				departmentService.deleteDepartmentById(departmentId);
				return ResponseEntity.ok("Department delete");
			} else {
				return ResponseEntity.status(403).build();
			}

		} catch (DataAccessException d) {
			return ResponseEntity.notFound().build();
		}

	}
}