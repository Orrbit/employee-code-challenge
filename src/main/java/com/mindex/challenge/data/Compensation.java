package com.mindex.challenge.data;

import java.util.Date;

public class Compensation {

    private String compensationId;
    private String employeeId;
    private int salary;
    private Date effectiveDate;

    public Compensation(){

    }

    public String getCompensationId() { return compensationId; }

    public void setCompensationId(String compensationId) { this.compensationId = compensationId; }

    public String getEmployeeId() { return employeeId; }

    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public int getSalary() { return salary; }

    public void setSalary(int salary) { this.salary = salary; }

    public Date getEffectiveDate() { return effectiveDate; }

    public void setEffectiveDate(Date effectiveDate) { this.effectiveDate = effectiveDate; }

}
