package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.CompensationService;
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

import java.util.Date;
import java.util.List;

import static com.mindex.challenge.service.impl.EmployeeServiceImplTest.assertEmployeeEquivalence;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CompensationServiceImplTest {

    private String compensationUrl;
    private String compensationAndEmployeeIdUrl;

    @Autowired
    private CompensationService compensationService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void setup() {
        compensationUrl = "http://localhost:" + port + "/compensation";
        compensationAndEmployeeIdUrl = "http://localhost:" + port + "/compensation/employee/{employeeId}";
    }

    @Test
    public void testCreateRead() {
        Employee testEmployee = employeeRepository.findByEmployeeId("16a596ae-edd3-4847-99fe-c4518e82c86f");

        Compensation compensation = new Compensation();
        compensation.setEffectiveDate(new Date());
        compensation.setSalary(20000);
        compensation.setEmployee(testEmployee);

        // Create checks
        Compensation createdCompensation = restTemplate.postForEntity(compensationUrl, compensation, Compensation.class).getBody();

        assertNotNull(createdCompensation.getCompensationId());
        assertCompensationEquivalence(compensation, createdCompensation);

        // Read checks
        Compensation[] readCompensations = restTemplate.getForEntity(compensationAndEmployeeIdUrl, Compensation[].class, testEmployee.getEmployeeId()).getBody();
        Compensation firstReadCompensation = readCompensations[0];

        assertEquals(createdCompensation.getCompensationId(), firstReadCompensation.getCompensationId());
        assertCompensationEquivalence(createdCompensation, firstReadCompensation);
    }

    @Test
    public void testCreateManyReadAllSameEmployee() {
        Employee testEmployee = employeeRepository.findByEmployeeId("16a596ae-edd3-4847-99fe-c4518e82c86f");

        Compensation firstCompensation = new Compensation();
        firstCompensation.setEffectiveDate(new Date());
        firstCompensation.setSalary(20000);
        firstCompensation.setEmployee(testEmployee);

        Compensation secondCompensation = new Compensation();
        secondCompensation.setEffectiveDate(new Date());
        secondCompensation.setSalary(50000);
        secondCompensation.setEmployee(testEmployee);

        //Create both compensations
        restTemplate.postForEntity(compensationUrl, firstCompensation, Compensation.class);
        restTemplate.postForEntity(compensationUrl, secondCompensation, Compensation.class);


        // Read checks
        Compensation[] readCompensations = restTemplate.getForEntity(compensationAndEmployeeIdUrl, Compensation[].class, testEmployee.getEmployeeId()).getBody();

        assertEquals(2, readCompensations.length);
    }

    @Test
    public void testCreateManyReadAllDifferentEmployee() {
        Employee firstEmployee = employeeRepository.findByEmployeeId("16a596ae-edd3-4847-99fe-c4518e82c86f");
        Employee secondEmployee = employeeRepository.findByEmployeeId("b7839309-3348-463b-a7e3-5de1c168beb3");

        Compensation firstCompensation = new Compensation();
        firstCompensation.setEffectiveDate(new Date());
        firstCompensation.setSalary(20000);
        firstCompensation.setEmployee(firstEmployee);

        Compensation secondCompensation = new Compensation();
        secondCompensation.setEffectiveDate(new Date());
        secondCompensation.setSalary(50000);
        secondCompensation.setEmployee(secondEmployee);

        //Create both compensations
        restTemplate.postForEntity(compensationUrl, firstCompensation, Compensation.class);
        restTemplate.postForEntity(compensationUrl, secondCompensation, Compensation.class);


        // Read checks
        Compensation[] readCompensations = restTemplate.getForEntity(compensationAndEmployeeIdUrl, Compensation[].class, firstEmployee.getEmployeeId()).getBody();

        assertEquals(1, readCompensations.length);

        readCompensations = restTemplate.getForEntity(compensationAndEmployeeIdUrl, Compensation[].class, secondEmployee.getEmployeeId()).getBody();

        assertEquals(1, readCompensations.length);
    }

    private static void assertCompensationEquivalence(Compensation expected, Compensation actual) {
        assertEquals(expected.getEffectiveDate(), actual.getEffectiveDate());
        assertEquals(expected.getSalary(), actual.getSalary());
        assertEmployeeEquivalence(expected.getEmployee(), actual.getEmployee());
    }

}
