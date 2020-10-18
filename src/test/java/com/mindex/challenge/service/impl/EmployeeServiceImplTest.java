package com.mindex.challenge.service.impl;

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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EmployeeServiceImplTest {

    private String employeeUrl;
    private String employeeIdUrl;
    private String employeeReportingStructureUrl;

    @Autowired
    private EmployeeService employeeService;

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
        Employee testEmployee = new Employee();
        testEmployee.setEmployeeId("1");

        // Create checks
        Employee createdEmployee = restTemplate.postForEntity(employeeUrl, testEmployee, Employee.class).getBody();

        assertNotNull(createdEmployee.getEmployeeId());
        assertEmployeeEquivalence(testEmployee, createdEmployee);


        ReportingStructure rs = restTemplate.getForEntity(employeeReportingStructureUrl, ReportingStructure.class, createdEmployee.getEmployeeId()).getBody();

        assertEquals(0, rs.getNumberOfReports());

    }

    @Test
    public void testOneLevelDepthReportingStructure() {
        Employee rootEmployee = new Employee();
        rootEmployee.setEmployeeId("1");

        int numDirectReports = 5;
        List<Employee> directReports = new LinkedList<Employee>();
        for(int i = 0; i < numDirectReports; i++){
            Employee directReport = new Employee();
            directReport.setEmployeeId("1 - " + i);
            directReports.add(directReport);
            restTemplate.postForEntity(employeeUrl, directReport, Employee.class);
        }
        rootEmployee.setDirectReports(directReports);
        restTemplate.postForEntity(employeeUrl, rootEmployee, Employee.class);

        ReportingStructure rs = restTemplate.getForEntity(employeeReportingStructureUrl, ReportingStructure.class, rootEmployee.getEmployeeId()).getBody();

        assertEquals(numDirectReports, rs.getNumberOfReports());

    }

    @Test
    public void testTwoLevelDepthReportingStructure() {
        Employee rootEmployee = new Employee();
        rootEmployee.setEmployeeId("1");

        int numDirectReports = 5;
        int numIndierctReports = 5;
        List<Employee> directReports = new LinkedList<Employee>();
        for(int i = 0; i < numDirectReports; i++){
            List<Employee> indirectReports = new LinkedList<Employee>();
            Employee directReport = new Employee();
            directReport.setEmployeeId("1 - " + i);
            directReports.add(directReport);
            for(int j = 0; j < numIndierctReports; j++){
                Employee indirectReport = new Employee();
                directReport.setEmployeeId("1 - " + i + " - " + j);
                indirectReports.add(indirectReport);
                restTemplate.postForEntity(employeeUrl, indirectReport, Employee.class);
            }
            restTemplate.postForEntity(employeeUrl, directReport, Employee.class);
        }
        rootEmployee.setDirectReports(directReports);
        restTemplate.postForEntity(employeeUrl, rootEmployee, Employee.class);

        ReportingStructure rs = restTemplate.getForEntity(employeeReportingStructureUrl, ReportingStructure.class, rootEmployee.getEmployeeId()).getBody();

        assertEquals(numDirectReports * numIndierctReports, rs.getNumberOfReports());
    }

    private static void assertEmployeeEquivalence(Employee expected, Employee actual) {
        assertEquals(expected.getFirstName(), actual.getFirstName());
        assertEquals(expected.getLastName(), actual.getLastName());
        assertEquals(expected.getDepartment(), actual.getDepartment());
        assertEquals(expected.getPosition(), actual.getPosition());
    }
}
