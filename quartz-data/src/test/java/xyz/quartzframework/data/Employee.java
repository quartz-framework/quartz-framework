package xyz.quartzframework.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class Employee {
    private UUID id;
    private String name;
    private int age;
    private Department department;
    private Instant createdAt;
}