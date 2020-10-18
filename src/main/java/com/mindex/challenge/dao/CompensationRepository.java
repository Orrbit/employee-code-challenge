package com.mindex.challenge.dao;

import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.Employee;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CompensationRepository extends MongoRepository<Compensation, String> {
    List<Compensation> findAllByEmployeeId(String employeeId);
}
