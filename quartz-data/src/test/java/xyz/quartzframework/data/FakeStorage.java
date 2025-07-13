package xyz.quartzframework.data;

import xyz.quartzframework.data.annotation.Storage;
import xyz.quartzframework.data.query.Query;
import xyz.quartzframework.data.query.QueryParameter;
import xyz.quartzframework.data.storage.InMemoryStorage;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Storage
public interface FakeStorage extends InMemoryStorage<FakeEntity, UUID> {

    List<FakeEntity> findByName(String name);

    List<FakeEntity> findByScoreGreaterThan(int minScore);

    List<FakeEntity> findByActiveTrue();

    List<FakeEntity> findByIdIn(Collection<UUID> ids);

    @Query("find where name like ?1")
    List<FakeEntity> searchByName(String pattern);

    @Query("find top 2 where score > ?1 order by score desc")
    List<FakeEntity> topScorers(int minScore);

    List<FakeEntity> findByNameAndActiveTrue(String name);

    List<FakeEntity> findByScoreLessThanAndActiveTrue(int maxScore);

    List<FakeEntity> findByCreatedAtAfter(Instant time);

    boolean existsByName(String name);

    long countByActiveTrue();

    List<FakeEntity> findByNameNotLike(String pattern);

    List<FakeEntity> findByNameIn(Collection<String> names);

    List<FakeEntity> findByNameIsNotNull();

    List<FakeEntity> findTop2ByActiveTrueOrderByScoreDesc();

    Optional<FakeEntity> findFirstByActiveTrueOrderByCreatedAtDesc();

    @Query("find where score >= ?1 and active = true order by score desc")
    List<FakeEntity> findActivesWithMinScore(int score);

    @Query("find where name not like ?1 order by createdAt asc")
    List<FakeEntity> findByNameExclusionPattern(String pattern);

    @Query("find top 1 where score < ?1 and active = true order by createdAt desc")
    Optional<FakeEntity> findRecentLowScorer(int maxScore);

    @Query("count where name like ?1")
    long countMatchingNames(String pattern);

    @Query("exists where name = ?1 and active = true")
    boolean existsActiveByName(String name);

    @Query("find where createdAt >= ?1 and createdAt <= ?2 order by createdAt desc")
    List<FakeEntity> findBetweenDates(Instant from, Instant to);

    @Query("find where name in ?1 order by name desc")
    List<FakeEntity> findAllByNameInDesc(Collection<String> names);

    @Query("exists where score >= ?1 and active = true")
    boolean existsByMinScoreAndActive(int score);

    @Query("count where score >= ?1")
    long countByScoreGreaterThanEqual(int score);

    @Query("count where active = true and score < ?1")
    long countActiveLowScorers(int maxScore);

    @Query("exists where createdAt > ?1")
    boolean existsByCreatedAfter(Instant date);

    List<FakeEntity> findByNameIsNull();

    @Query("find where createdAt >= :from and createdAt <= :to order by createdAt desc")
    List<FakeEntity> findBetweenDatesWithParameters(
            @QueryParameter("from") Instant from,
            @QueryParameter("to") Instant to
    );

    @Query("exists where name = :name and active = true")
    boolean existsActiveByNameWithParameters(@QueryParameter("name") String name);

    @Query("count where name like :pattern")
    long countMatchingNamesWithParameters(@QueryParameter("pattern") String pattern);

    @Query("find top 1 where score < :maxScore and active = true order by createdAt desc")
    Optional<FakeEntity> findRecentLowScorerWithParameters(@QueryParameter("maxScore") int maxScore);

    @Query("find where name not like :pattern order by createdAt asc")
    List<FakeEntity> findByNameExclusionPatternWithParameters(@QueryParameter("pattern") String pattern);

    @Query("find where name in :names order by name asc")
    List<FakeEntity> findAllByNameInWithParameters(@QueryParameter("names") Collection<String> names);

    @Query("find where createdAt >= :from and createdAt <= :to")
    List<FakeEntity> brokenQuery(@QueryParameter("from") Instant from);

    @Query("find where lower(name) = lower(?1)")
    List<FakeEntity> findByNameIgnoreCase(String name);

    @Query("find where upper(name) = upper(?1)")
    List<FakeEntity> findByNameIgnoreCaseUpper(String name);

    @Query("find where lower(name) like lower(?1)")
    List<FakeEntity> searchByNameIgnoreCase(String pattern);

    @Query("find where upper(name) like upper(?1)")
    List<FakeEntity> searchByNameIgnoreCaseUpper(String pattern);

    @Query("exists where upper(name) = upper(:name)")
    boolean existsByNameIgnoreCase(@QueryParameter("name") String name);

    @Query("exists where upper(name) = :name")
    boolean existsByNameUpper(@QueryParameter("name") String name);

    @Query("find where active = true " +
            "returns new xyz.quartzframework.data.FakeEntityDTO(id)")
    List<FakeEntityDTO> findActiveAsDto();

    @Query("find top 1 where score < ?1 and active = true order by createdAt desc " +
            "returns new xyz.quartzframework.data.FakeEntityDTO(id)")
    Optional<FakeEntityDTO> findRecentLowScorerAsDto(int maxScore);

}
