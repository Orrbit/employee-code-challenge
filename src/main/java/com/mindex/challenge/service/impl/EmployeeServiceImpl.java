package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private static final Logger LOG = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public Employee create(Employee employee) {
        LOG.debug("Creating employee [{}]", employee);

        employee.setEmployeeId(UUID.randomUUID().toString());
        employeeRepository.insert(employee);

        return employee;
    }

    @Override
    public Employee read(String id) {
        LOG.debug("Reading employee with id [{}]", id);

        Employee employee = employeeRepository.findByEmployeeId(id);

        if (employee == null) {
            throw new RuntimeException("Invalid employeeId: " + id);
        }

        return employee;
    }

    @Override
    public Employee update(Employee employee) {
        LOG.debug("Updating employee [{}]", employee);

        return employeeRepository.save(employee);
    }

    public ReportingStructure getNumberOfReports(Employee employee){
        LOG.debug("Getting the number of reports for employee: [{}]", employee);

        Employee fullInfoEmployee = populateAllReportInformation(employee, new LinkedList<Employee>());

        ReportingStructure reportingStructure = new ReportingStructure();
        reportingStructure.setRootEmployee(fullInfoEmployee);
        reportingStructure.setNumberOfReportsBasedOnEmployee(fullInfoEmployee);

        return reportingStructure;
    }

    /**
     * This function will populate the report information of an employee.
     * It is required because the data on stores the direct reports id.
     * There are many cases where we want to know all the information
     * about the employee.
     *
     *
     * @param employee
     * @return the input employee with all information about its reports.
     */
    private Employee populateAllReportInformation(Employee employee, List<Employee> alreadyVisitedEmployees)
    throws CircularReportingException{
        alreadyVisitedEmployees.add(employee);
        if(employee.getDirectReports() == null){
            return employee;
        }
        List<Employee> fullInfoDirectReports = new LinkedList<Employee>();
        for (Employee directReport: employee.getDirectReports()) {
            if(hasBeenVisited(alreadyVisitedEmployees, directReport)){
                throw new CircularReportingException("Detected a circular reporting with employee", directReport);
            }
            Employee fullInfoDirectReportEmployee = read(directReport.getEmployeeId());
            fullInfoDirectReportEmployee = populateAllReportInformation(fullInfoDirectReportEmployee, alreadyVisitedEmployees);
            fullInfoDirectReports.add(fullInfoDirectReportEmployee);
        }
        employee.setDirectReports(fullInfoDirectReports);
        return employee;
    }

    private boolean hasBeenVisited(List<Employee> alreadyVisitedEmployees, Employee employee){
        for(Employee alreadyVisitedEmployee: alreadyVisitedEmployees){
            if(alreadyVisitedEmployee.getEmployeeId().equals(employee.getEmployeeId())){
                return true;
            }
        }
        return false;
    }
}
