package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.EmployeeService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EmployeeServiceImplTest {

    private String employeeUrl;
    private String employeeIdUrl;
    private String employeeReportingStructureUrl;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private EmployeeRepository employeeRepository;


    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void setup() {
        employeeUrl = "http://localhost:" + port + "/employee";
        employeeIdUrl = "http://localhost:" + port + "/employee/{id}";
        employeeReportingStructureUrl = "http://localhost:" + port + "/employee/number-of-reports/{id}";
    }

    @Test
    public void testCreateReadUpdate() {
        Employee testEmployee = new Employee();
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setDepartment("Engineering");
        testEmployee.setPosition("Developer");

        // Create checks
        Employee createdEmployee = restTemplate.postForEntity(employeeUrl, testEmployee, Employee.class).getBody();

        assertNotNull(createdEmployee.getEmployeeId());
        assertEmployeeEquivalence(testEmployee, createdEmployee);


        // Read checks
        Employee readEmployee = restTemplate.getForEntity(employeeIdUrl, Employee.class, createdEmployee.getEmployeeId()).getBody();
        assertEquals(createdEmployee.getEmployeeId(), readEmployee.getEmployeeId());
        assertEmployeeEquivalence(createdEmployee, readEmployee);


        // Update checks
        readEmployee.setPosition("Development Manager");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Employee updatedEmployee =
                restTemplate.exchange(employeeIdUrl,
                        HttpMethod.PUT,
                        new HttpEntity<Employee>(readEmployee, headers),
                        Employee.class,
                        readEmployee.getEmployeeId()).getBody();

        assertEmployeeEquivalence(readEmployee, updatedEmployee);
    }

    @Test
    public void testNoDirectReports() {
        Employee noReportsEmployee = employeeRepository.findByEmployeeId("c0c2293d-16bd-4603-8e08-638a9d18b22c");

        assertEquals("George", noReportsEmployee.getFirstName());
        assertEquals("Harrison", noReportsEmployee.getLastName());

        ReportingStructure rs = restTemplate.getForEntity(employeeReportingStructureUrl, ReportingStructure.class, noReportsEmployee.getEmployeeId()).getBody();

        assertEquals(0, rs.getNumberOfReports());
    }

    @Test
    public void testOneLevelDepthReportingStructure() {
        Employee onlyDirectReportsEmployee = employeeRepository.findByEmployeeId("03aa1462-ffa9-4978-901b-7c001562cf6f");

        assertEquals("Ringo", onlyDirectReportsEmployee.getFirstName());
        assertEquals("Starr", onlyDirectReportsEmployee.getLastName());

        ReportingStructure rs = restTemplate.getForEntity(employeeReportingStructureUrl, ReportingStructure.class, onlyDirectReportsEmployee.getEmployeeId()).getBody();

        assertEquals(2, rs.getNumberOfReports());
    }

    @Test
    public void testTwoLevelDepthReportingStructure() {
        Employee rootEmployee = employeeRepository.findByEmployeeId("16a596ae-edd3-4847-99fe-c4518e82c86f");

        assertEquals("John", rootEmployee.getFirstName());
        assertEquals("Lennon", rootEmployee.getLastName());

        ReportingStructure rs = restTemplate.getForEntity(employeeReportingStructureUrl, ReportingStructure.class, rootEmployee.getEmployeeId()).getBody();

        assertEquals(4, rs.getNumberOfReports());
    }

    @Test
    public void testDetectsCircularReportingAndThrowsError() {
        Employee circularReportA = employeeRepository.findByEmployeeId("a7622629-1b49-2232-9080-71ef05ea69fe");

        assertEquals("Circular Report Test A", circularReportA.getFirstName());

        ResponseEntity<String> response = restTemplate.getForEntity(employeeReportingStructureUrl, String.class, circularReportA.getEmployeeId());
        String returnError = response.getBody();

        assertTrue(returnError.contains("Detected a circular reporting with employee a7622629-1b49-2232-9080-71ef05ea69fe"));

    }


    private static void assertEmployeeEquivalence(Employee expected, Employee actual) {
        assertEquals(expected.getFirstName(), actual.getFirstName());
        assertEquals(expected.getLastName(), actual.getLastName());
        assertEquals(expected.getDepartment(), actual.getDepartment());
        assertEquals(expected.getPosition(), actual.getPosition());
    }
}
