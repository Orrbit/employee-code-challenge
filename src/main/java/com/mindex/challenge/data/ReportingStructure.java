package com.mindex.challenge.data;

import java.util.LinkedList;
import java.util.List;

public class ReportingStructure {
    private int numberOfReports;
    private Employee rootEmployee;

    public ReportingStructure() {
    }

    public int getNumberOfReports() { return numberOfReports; }

    private void updateNumberOfReports(Employee employee){
        numberOfReports = employee.getDirectReports().size();
    }

    public Employee getRootEmployee() { return rootEmployee; }

    public void setRootEmployee(Employee rootEmployee) {
        this.rootEmployee = rootEmployee;
        updateNumberOfReports(rootEmployee);
    }
}
