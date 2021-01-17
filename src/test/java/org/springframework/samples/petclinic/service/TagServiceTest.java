package org.springframework.samples.petclinic.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.dao.DataAccessException;

import org.springframework.samples.petclinic.model.Project;
import org.springframework.samples.petclinic.model.Tag;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest(includeFilters = @ComponentScan.Filter(Service.class))
public class TagServiceTest {
	@Autowired
	protected TagService tagService;
	@Autowired
	protected ProjectService projectService;

	@Test
	@Transactional
	public void shouldInsertTagIntoDatabaseAndGenerateId() {

		Project project = projectService.findProjectById(1);
		Tag tag = new Tag();
		tag.setTitle("In Process");
		tag.setColor("#B0D9CD");
		tag.setProject(project);

		try {
			this.tagService.saveTag(tag);
		} catch (DataAccessException ex) {
			Logger.getLogger(TagServiceTest.class.getName()).log(Level.SEVERE, null, ex);
		}

		assertThat(tag.getId()).isNotNull();

	}

	@Test
	void shouldFindTagWithCorrectId() {
		Tag tag = this.tagService.findTagById(3);
		assertThat(tag.getTitle()).isEqualTo("Testing");
		assertThat(tag.getColor()).isEqualTo("#B0D9CD");
	}

	@Test
	@Transactional
	void shouldDeleteTag() {

		tagService.deleteTagById(2);
		Tag tag = this.tagService.findTagById(2);
		assertThat(tag).isNull();
	}

}