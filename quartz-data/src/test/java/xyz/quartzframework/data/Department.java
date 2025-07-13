package xyz.quartzframework.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import xyz.quartzframework.data.entity.Attribute;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class Department {
    private UUID id;

    @Attribute("deptName")
    private String name;
}