package xyz.quartzframework.data;

import xyz.quartzframework.data.annotation.Storage;
import xyz.quartzframework.data.query.Query;
import xyz.quartzframework.data.query.QueryParameter;
import xyz.quartzframework.data.storage.InMemoryStorage;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Storage
public interface EmployeeStorage extends InMemoryStorage<Employee, UUID> {

    @Query("find where department.name = :deptName order by name asc " +
            "returns new xyz.quartzframework.data.EmployeeDTO(id, name, department.name)")
    List<EmployeeDTO> findByDepartmentName(@QueryParameter("deptName") String deptName);

    @Query("find top 3 where createdAt >= :startDate and department.name like :pattern order by createdAt desc")
    List<Employee> findTop3ByStartDateAndDepartmentNamePattern(@QueryParameter("startDate") Instant startDate,
                                                               @QueryParameter("pattern") String pattern);

    @Query("count where department.name = :deptName and age > :minAge")
    long countByDepartmentAndMinAge(@QueryParameter("deptName") String deptName,
                                    @QueryParameter("minAge") int minAge);

    @Query("exists where name like :name and age < :maxAge")
    boolean existsByNameLikeAndMaxAge(@QueryParameter("name") String name,
                                      @QueryParameter("maxAge") int maxAge);

    @Query("find distinct where department.name is not null order by department.name asc")
    List<Employee> findAllWithDepartment();

    @Query("find where lower(name) = lower(:name) or department.name != :dept")
    List<Employee> findByNameIgnoreCaseOrDepartment_NameNot(@QueryParameter("name") String name,
                                                            @QueryParameter("dept") String excludedDept);

    List<Employee> findDistinctByDepartment_NameIsNotNullOrderByDepartment_NameAsc();

    List<Employee> findTop3ByCreatedAtAfterAndDepartment_NameLikeOrderByCreatedAtDesc(Instant startDate, String pattern);

    long countByDepartment_NameAndAgeGreaterThan(String deptName, int minAge);

    boolean existsByNameLikeAndAgeLessThan(String name, int maxAge);

    @Query("find where age >= :min and age <= :max order by name asc")
    List<Employee> findByAgeRange(@QueryParameter("min") int min, @QueryParameter("max") int max);

    @Query("find where department.name not like :deptName and age <= :maxAge order by createdAt desc")
    List<Employee> findYoungerOutsideDepartment(@QueryParameter("deptName") String dept, @QueryParameter("maxAge") int maxAge);

    @Query("exists where lower(department.name) = lower(:dept) and lower(name) = lower(:name)")
    boolean existsByDepartmentAndNameIgnoreCase(@QueryParameter("dept") String dept, @QueryParameter("name") String name);

    @Query("count where department.name like :pattern and age >= :min")
    long countOlderWithDepartmentLike(@QueryParameter("pattern") String pattern, @QueryParameter("min") int min);

    @Query("find top 1 where department.name = :dept order by createdAt asc")
    List<Employee> findFirstJoinedInDepartment(@QueryParameter("dept") String dept);

    @Query("find where department.name = :dept and name not like :exclude order by name desc")
    List<Employee> findByDepartmentExcludingNamePattern(@QueryParameter("dept") String dept, @QueryParameter("exclude") String excludePattern);

    @Query("find where createdAt < :before order by createdAt asc")
    List<Employee> findAllBeforeDate(@QueryParameter("before") Instant before);

    @Query("find where lower(department.name) like lower(:pattern)")
    List<Employee> findByDepartmentCaseInsensitivePattern(@QueryParameter("pattern") String pattern);

    @Query("find distinct where age >= :min order by department.name desc")
    List<Employee> findDistinctOlderEmployees(@QueryParameter("min") int min);

    @Query("find where name not like :pattern and department.name != :dept")
    List<Employee> excludeByNameAndDepartment(@QueryParameter("pattern") String pattern, @QueryParameter("dept") String dept);

    @Query("find where department.deptName = :deptName")
    List<Employee> findByDepartmentDeptName(@QueryParameter("deptName") String deptName);

    List<Employee> findByDepartment_deptName(String deptName);

    List<Employee> findByDepartment_Name(String deptName);

    @Query("find where name = :name or department.name = :dept and age >= :minAge")
    List<Employee> findByNameOrDeptAndMinAge(@QueryParameter("name") String name,
                                             @QueryParameter("dept") String dept,
                                             @QueryParameter("minAge") int minAge);

    @Query("find where (name = :name) or (department.name = :dept and age >= :minAge)")
    List<Employee> findByNameOrDeptAndMinAgeWithParens(@QueryParameter("name") String name,
                                                       @QueryParameter("dept") String dept,
                                                       @QueryParameter("minAge") int minAge);

    @Query("""
    find\s
    where (name like :pattern and age >= :minAge)\s
    or (department.name is not null and department.name not like :excludedDept)
    order by name asc
""")
    List<Employee> findMatchingNameOrValidDept(@QueryParameter("pattern") String pattern,
                                               @QueryParameter("minAge") int minAge,
                                               @QueryParameter("excludedDept") String excludedDept);

    @Query("""
    find\s
    where (lower(name) like lower(:namePattern) and age >= :minAge)
    or (lower(department.name) not like lower(:excluded) and age < :maxAge)
    order by createdAt desc
""")
    List<Employee> findAdvancedFiltering(@QueryParameter("namePattern") String namePattern,
                                         @QueryParameter("minAge") int minAge,
                                         @QueryParameter("excluded") String excluded,
                                         @QueryParameter("maxAge") int maxAge);

    @Query("""
    find\s
    where department.name in :departments\s
    and name not in :excludedNames\s
    and department.id is not null
    order by age desc
""")
    List<Employee> findByValidDepartments(@QueryParameter("departments") List<String> departments,
                                          @QueryParameter("excludedNames") List<String> excludedNames);

    @Query("""
    find\s
    where (lower(name) = lower(:n1))\s
    or (department.name = :d2 and age > :a2)
    or (age < :a3 and department.name != :d3)
    order by createdAt asc
""")
    List<Employee> tripleOrMix(@QueryParameter("n1") String n1,
                               @QueryParameter("d2") String d2,
                               @QueryParameter("a2") int a2,
                               @QueryParameter("a3") int a3,
                               @QueryParameter("d3") String d3);
}