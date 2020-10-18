package com.mindex.challenge.data;

import java.util.LinkedList;
import java.util.List;

public class ReportingStructure {
    private int numberOfReports;
    private Employee rootEmployee;

    public ReportingStructure() {
    }

    public int getNumberOfReports() { return numberOfReports; }

    public void setNumberOfReports(int numberOfReports){ this.numberOfReports = numberOfReports; }

    public void setNumberOfReportsBasedOnEmployee(Employee employee){
        numberOfReports = calculateNumberOfReportsBasedOnEmployee(employee);
    }

    private int calculateNumberOfReportsBasedOnEmployee(Employee employee){
        if(employee.getDirectReports() == null){
            return 0;
        }

        int numReports = employee.getDirectReports().size();
        for(Employee directReport: employee.getDirectReports()){
            numReports += calculateNumberOfReportsBasedOnEmployee(directReport);
        }
        return numReports;
    }

    public Employee getRootEmployee() { return rootEmployee; }

    public void setRootEmployee(Employee rootEmployee) { this.rootEmployee = rootEmployee; }
}
