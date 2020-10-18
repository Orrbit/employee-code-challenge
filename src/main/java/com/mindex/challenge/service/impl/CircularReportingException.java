package com.mindex.challenge.service.impl;

import com.mindex.challenge.data.Employee;

public class CircularReportingException extends RuntimeException {
    public CircularReportingException(String errorMessage, Employee employee) {
        super(errorMessage + " " + employee.getEmployeeId());
    }
}