package xyz.quartzframework.data;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.quartzframework.data.query.InMemoryQueryExecutor;
import xyz.quartzframework.data.query.SimpleQueryParser;
import xyz.quartzframework.data.util.ProxyFactoryUtil;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeStorageTest {

    private EmployeeStorage storage;

    UUID id1 = UUID.randomUUID();
    UUID id2 = UUID.randomUUID();
    UUID id3 = UUID.randomUUID();
    UUID id4 = UUID.randomUUID();

    UUID deptEngineeringId = UUID.randomUUID();
    UUID deptHrId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        Department engineering = new Department(deptEngineeringId, "Engineering");
        Department people = new Department(deptHrId, "People");

        List<Employee> employees = List.of(
                new Employee(id1, "Alice", 30, engineering, Instant.parse("2025-01-01T00:00:00Z")),
                new Employee(id2, "Bob", 25, engineering, Instant.parse("2025-01-01T00:00:01Z")),
                new Employee(id3, "Charlie", 40, people, Instant.parse("2025-01-01T00:00:02Z")),

                new Employee(id4, "David", 35, people, Instant.parse("2024-12-31T23:40:00Z"))
        );

        InMemoryQueryExecutor<Employee> executor = new InMemoryQueryExecutor<>(employees, Employee.class);
        storage = ProxyFactoryUtil.createProxy(
                new SimpleQueryParser(),
                EmployeeStorage.class,
                executor,
                Employee.class,
                UUID.class
        );
    }

    @Test
    void testFindByDepartmentName_Engineering() {
        List<EmployeeDTO> result = storage.findByDepartmentName("Engineering");
        assertEquals(2, result.size());

        assertTrue(result.stream().anyMatch(dto -> dto.getId().equals(id1)));
        assertTrue(result.stream().anyMatch(dto -> dto.getId().equals(id2)));
        assertTrue(result.stream().allMatch(dto -> dto.getDepartmentName().equals("Engineering")));
    }

    @Test
    void testFindByDepartmentName_HR() {
        List<EmployeeDTO> result = storage.findByDepartmentName("People");
        assertEquals(2, result.size());

        assertTrue(result.stream().anyMatch(dto -> dto.getId().equals(id3)));
        assertTrue(result.stream().anyMatch(dto -> dto.getId().equals(id4)));
    }

    @Test
    void testFindByDepartmentName_EmptyResult() {
        List<EmployeeDTO> result = storage.findByDepartmentName("Marketing");
        assertTrue(result.isEmpty());
    }

    @Test
    void testIgnoreCaseNameMatch() {
        List<Employee> result = storage.findByNameIgnoreCaseOrDepartment_NameNot("ALICE", "Engineering");
        assertEquals(3, result.size());
        assertTrue(result.stream().anyMatch(e -> e.getId().equals(id1)));
        assertTrue(result.stream().anyMatch(e -> e.getId().equals(id4)));
    }

    @Test
    void testExistsByNameLikeAndMaxAge() {
        assertTrue(storage.existsByNameLikeAndMaxAge("A%", 40));
        assertFalse(storage.existsByNameLikeAndMaxAge("Z%", 50));
    }

    @Test
    void testCountByDepartmentAndMinAge() {
        long count = storage.countByDepartmentAndMinAge("People", 30);
        assertEquals(2, count);
    }

    @Test
    void testTop3ByCreatedAt() {
        Instant reference = Instant.parse("2024-12-31T23:43:00Z");
        List<Employee> result = storage.findTop3ByStartDateAndDepartmentNamePattern(reference, "%e%");
        assertEquals(3, result.size());
    }

    @Test
    void testFindAllWithDepartmentDistinct() {
        List<Employee> result = storage.findAllWithDepartment();
        assertEquals(4, result.size());
    }

    @Test
    void testMethodDistinctByDepartment() {
        List<Employee> result = storage.findDistinctByDepartment_NameIsNotNullOrderByDepartment_NameAsc();
        assertEquals(4, result.size());
    }

    @Test
    void testFindByAgeRange() {
        List<Employee> result = storage.findByAgeRange(25, 35);
        assertEquals(3, result.size());
    }

    @Test
    void testExistsByDepartmentAndNameIgnoreCase() {
        assertTrue(storage.existsByDepartmentAndNameIgnoreCase("engineering", "alice"));
        assertFalse(storage.existsByDepartmentAndNameIgnoreCase("People", "bob"));
    }

    @Test
    void testFindAllBeforeDate() {
        Instant cutoff = Instant.parse("2025-01-01T00:00:00Z");
        List<Employee> result = storage.findAllBeforeDate(cutoff);
        assertEquals(1, result.size());
    }

    @Test
    void testFindDistinctOlderEmployees() {
        List<Employee> result = storage.findDistinctOlderEmployees(30);
        assertTrue(result.stream().allMatch(e -> e.getAge() >= 30));
    }

    @Test
    void testFindYoungerOutsideDepartment() {
        List<Employee> result = storage.findYoungerOutsideDepartment("Engineering", 36);
        assertEquals(1, result.size());
        assertEquals("David", result.get(0).getName());
    }

    @Test
    void testCountOlderWithDepartmentLike() {
        long count = storage.countOlderWithDepartmentLike("%e%", 30);
        assertEquals(3, count);
    }

    @Test
    void testFindFirstJoinedInDepartment() {
        List<Employee> result = storage.findFirstJoinedInDepartment("People");
        assertEquals(1, result.size());
        assertEquals("David", result.get(0).getName());
    }

    @Test
    void testFindByDepartmentExcludingNamePattern() {
        List<Employee> result = storage.findByDepartmentExcludingNamePattern("Engineering", "A%");
        assertEquals(1, result.size());
        assertEquals("Bob", result.get(0).getName());
    }

    @Test
    void testTop3ByCreatedAtAfterDerived() {
        Instant ref = Instant.parse("2024-12-31T23:30:00Z");
        List<Employee> result = storage.findTop3ByCreatedAtAfterAndDepartment_NameLikeOrderByCreatedAtDesc(ref, "%e%");
        assertEquals(3, result.size());
    }

    @Test
    void testExcludeByNameAndDepartment() {
        List<Employee> result = storage.excludeByNameAndDepartment("A%", "Engineering");
        assertEquals(2, result.size());
    }

    @Test
    void testTop3QQLAndDerivedEquivalence() {
        Instant ref = Instant.parse("2024-12-31T23:30:00Z");
        List<Employee> qql = storage.findTop3ByStartDateAndDepartmentNamePattern(ref, "%e%");
        List<Employee> derived = storage.findTop3ByCreatedAtAfterAndDepartment_NameLikeOrderByCreatedAtDesc(ref, "%e%");
        assertEquals(qql.size(), derived.size());
    }

    @Test
    void testCountByDepartmentNameAndAgeGreaterThan() {
        long count = storage.countByDepartment_NameAndAgeGreaterThan("People", 30);
        assertEquals(2, count);
    }

    @Test
    void testExistsByNameLikeAndAgeLessThan() {
        assertTrue(storage.existsByNameLikeAndAgeLessThan("A%", 35));
        assertFalse(storage.existsByNameLikeAndAgeLessThan("Z%", 50));
    }

    @Test
    void testFindByDepartmentCaseInsensitivePattern() {
        List<Employee> result = storage.findByDepartmentCaseInsensitivePattern("peo%");
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(e -> e.getDepartment().getName().equals("People")));
    }

    @Test
    void testFindByDepartmentUsingAttributeAlias() {
        List<Employee> result = storage.findByDepartmentDeptName("Engineering");
        assertEquals(2, result.size());

        assertTrue(result.stream().anyMatch(e -> e.getName().equals("Alice")));
        assertTrue(result.stream().anyMatch(e -> e.getName().equals("Bob")));
    }

    @Test
    void testFindByDepartmentAliasWithMethodQuery() {
        List<Employee> result = storage.findByDepartment_deptName("People");
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(e -> e.getName().equals("Charlie")));
        assertTrue(result.stream().anyMatch(e -> e.getName().equals("David")));
    }

    @Test
    void testFindByDepartmentFieldWithMethodQuery() {
        List<Employee> result = storage.findByDepartment_Name("People");
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(e -> e.getName().equals("Charlie")));
        assertTrue(result.stream().anyMatch(e -> e.getName().equals("David")));
    }

    @Test
    void testFindByNameOrDeptAndMinAge() {
        val storage = mockEmployeeStorage();
        List<Employee> result = storage.findByNameOrDeptAndMinAge("Zara", "IT", 30);

        assertEquals(2, result.size());

        assertTrue(result.stream().anyMatch(e -> e.getName().equals("Zara")));
        assertTrue(result.stream().anyMatch(e -> e.getDepartment() != null && e.getDepartment().getName().equals("IT") && e.getAge() >= 30));

        List<Employee> resultParens = storage.findByNameOrDeptAndMinAgeWithParens("Zara", "IT", 30);

        assertEquals(2, resultParens.size());

        assertTrue(resultParens.stream().anyMatch(e -> e.getName().equals("Zara")));
        assertTrue(resultParens.stream().anyMatch(e -> e.getDepartment() != null && e.getDepartment().getName().equals("IT") && e.getAge() >= 30));

    }

    @Test
    void testFindMatchingNameOrValidDept() {
        val storage = mockEmployeeStorage();
        List<Employee> result = storage.findMatchingNameOrValidDept("A%", 25, "People");
        assertTrue(result.stream().anyMatch(e -> e.getName().startsWith("Ana") && e.getAge() >= 25));
        assertTrue(result.stream().allMatch(e -> e.getName().startsWith("Ana") ||
                (e.getDepartment() != null &&
                        !e.getDepartment().getName().equalsIgnoreCase("People"))));
    }

    @Test
    void testFindAdvancedFiltering() {
        val storage = mockEmployeeStorage();
        List<Employee> result = storage.findAdvancedFiltering("ana", 30, "hr", 35);
        assertTrue(result.stream().anyMatch(e -> e.getName().equalsIgnoreCase("ana") && e.getAge() >= 30));
        assertTrue(result.stream().anyMatch(e ->
                (e.getDepartment() != null && !e.getDepartment().getName().equalsIgnoreCase("hr") && e.getAge() < 35)));
    }

    @Test
    void testFindByValidDepartments() {
        val storage = mockEmployeeStorage();
        List<Employee> result = storage.findByValidDepartments(List.of("IT", "HR", "Engineering"), List.of("Ana", "Zara"));
        assertTrue(result.stream().allMatch(e -> e.getDepartment() != null));
        assertTrue(result.stream().noneMatch(e -> e.getName().equals("Ana") || e.getName().equals("Zara")));
    }

    @Test
    void testTripleOrMix() {
        val storage = mockEmployeeStorage();
        List<Employee> result = storage.tripleOrMix("Lara", "IT", 30, 40, "HR");
        assertTrue(result.stream().anyMatch(e -> e.getName().equalsIgnoreCase("Lara")));
        assertTrue(result.stream().anyMatch(e ->
                e.getDepartment() != null && e.getDepartment().getName().equals("IT") && e.getAge() > 30));
        assertTrue(result.stream().anyMatch(e ->
                e.getAge() < 40 && (e.getDepartment() == null || !e.getDepartment().getName().equals("HR"))));
    }

    private EmployeeStorage mockEmployeeStorage() {
        Department it = new Department(UUID.randomUUID(), "IT");
        Department hr = new Department(UUID.randomUUID(), "HR");

        List<Employee> employees = List.of(
                new Employee(UUID.randomUUID(), "Zara", 20, null, Instant.now()),
                new Employee(UUID.randomUUID(), "Ana", 40, it, Instant.now()),
                new Employee(UUID.randomUUID(), "Ana", 22, it, Instant.now()),
                new Employee(UUID.randomUUID(), "Ana", 35, hr, Instant.now()),
                new Employee(UUID.randomUUID(), "Ana", 30, null, Instant.now()),
                new Employee(UUID.randomUUID(), "Lara", 28, null, Instant.now())
        );

        return ProxyFactoryUtil.createProxy(
                new SimpleQueryParser(),
                EmployeeStorage.class,
                new InMemoryQueryExecutor<>(employees, Employee.class),
                Employee.class, UUID.class);
    }
}