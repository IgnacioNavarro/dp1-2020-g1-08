package org.springframework.samples.petclinic.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.sun.istack.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "teams")

public class Team extends BaseEntity {
	
	  // Attributes
	@Pattern(regexp ="^[A-Za-zÀ-ÿ0-9\\u00f1\\u00d1_-]*$")
	@NotEmpty
	@Size(min=1,max=25)
	@Column(name = "name",unique=true)
	String name;

	@Pattern(regexp = "^[a-zA-ZÀ-ÿ\\u00f1\\u00d1 ]+$")
	@NotEmpty
	@Size(min=1,max=25)
	@Column(name = "identifier",unique=true)
	String identifier;

	// Relaciones
	@JsonIgnore
	//@JsonManagedReference(value="team-user")
	@Column(name = "users")
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "team", orphanRemoval = true)
	private List<UserTW> users;
	
	@JsonIgnore
	//@JsonManagedReference(value="team-department")
	@Column(name = "departments")
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "team", orphanRemoval = true)
	private List<Department> departments;

}
