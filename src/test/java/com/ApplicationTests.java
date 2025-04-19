package com;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
/*HTTP test*/
@AutoConfigureMockMvc
/*Test DB*/
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class ApplicationTests {

	@Autowired
	MockMvc mockMvc;

	/*Disable test*/
	//@Disabled("Temporarily disabled until bug XYZ is fixed")
	@Test
	void contextLoads() {

	}
}
