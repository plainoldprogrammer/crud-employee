package com.example.relationaldataaccess;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class EmployeeController {

	private static final Logger log = LoggerFactory.getLogger(EmployeeController.class);

	@Autowired
	JdbcTemplate jdbcTemplate;

	@GetMapping("/display-all-employees")
	public String displayAllEmployees(
		@RequestParam(name="employ", required=false, defaultValue="newone") String theEmploy,
		Model model) {

		log.info("Call to displayAllEmployees with: " + theEmploy);

		List<Employee> employees = jdbcTemplate.query(
			"SELECT id, first_name, last_name FROM employees", 
			(rs, rowNum) -> new Employee(rs.getLong("id"), rs.getString("first_name"), rs.getString("last_name")));

		String allEmployees = "";

		for (Employee e : employees) {
			allEmployees += e.toString();
		}

		model.addAttribute("allEmployees", allEmployees);

		return "display-all-employees";
	}

	@GetMapping("/create-an-employee")
	public String createAnEmployee(
		@RequestParam(name="first-name", required = false, defaultValue="New Employee") String firstName,
		@RequestParam(name="last-name", required=false, defaultValue="New Employee") String lastName,
		Model model) {

		log.info("call to createAnEmployee");
		log.info("Request to create an employee");
		log.info("First name: " + firstName);
		log.info("Last name: " + lastName);

		jdbcTemplate.update("INSERT INTO employees(first_name, last_name) VALUES (?, ?);", firstName, lastName);

		model.addAttribute("firstName", firstName);
		model.addAttribute("lastName", lastName);

		return "create-an-employee";
	}

	@GetMapping("/delete-all-employees")
	public String deleteAllEmployees(Model model) {
		log.info("Call to deleteAllEmployees");

		jdbcTemplate.execute("DELETE FROM employees;");

		return "delete-all-employees";
	}

	@GetMapping("/configure")
	public String configure(Model model) {
		log.info("Call to configure");

		jdbcTemplate.execute("DROP TABLE IF EXISTS employees;");

		jdbcTemplate.execute("CREATE TABLE employees(" +
			"id SERIAL, first_name VARCHAR(255), last_name VARCHAR(255));");

		// Split up the array of while names into an array of first/last names.
		List<Object[]> splitUpNames = Arrays.asList("John Woo", "Jeff Dean", "Josh Block", "Josh Long").stream()
			.map(name-> name.split(" "))
			.collect(Collectors.toList());

		// Use a Java 8 stream to print out each tuple of the list.
		splitUpNames.forEach(name -> log.info(String.format("Inserting employee record for %s %s", name[0], name[1])));

		// Use JdbcTemplate's batchUpdate operation to bulk load data
		jdbcTemplate.batchUpdate("INSERT INTO employees(first_name, last_name) VALUES (?, ?);", splitUpNames);

		return "configure";
	}

}
