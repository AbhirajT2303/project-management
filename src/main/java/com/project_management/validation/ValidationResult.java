package com.project_management.validation;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ValidationResult {
    private List<Violation> violations = new ArrayList<>();

    public void addViolation(String field, String message){
        violations.add(new Violation(field,message));
    }

    public boolean hasErrors(){
        return !violations.isEmpty();
    }
}
