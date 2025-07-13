package xyz.quartzframework.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class EmployeeDTO {
    private UUID id;
    private String name;
    private String departmentName;
}