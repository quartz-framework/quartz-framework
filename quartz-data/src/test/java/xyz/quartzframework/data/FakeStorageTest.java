package xyz.quartzframework.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.quartzframework.data.query.InMemoryQueryExecutor;
import xyz.quartzframework.data.query.ParameterBindingException;
import xyz.quartzframework.data.query.SimpleQueryParser;
import xyz.quartzframework.data.util.ProxyFactoryUtil;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FakeStorageTest {

    private FakeStorage storage;

    UUID uuid1 = UUID.randomUUID();
    UUID uuid2 = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        Instant now = Instant.parse("2025-01-01T00:00:00Z");
        List<FakeEntity> data = List.of(
                new FakeEntity(uuid1, "Alice", 90, true, now.minusSeconds(1000)),
                new FakeEntity(uuid2, "Bob", 50, false, now.minusSeconds(500)),
                new FakeEntity(UUID.randomUUID(), "Charlie", 70, true, now)
        );

        InMemoryQueryExecutor<FakeEntity> executor = new InMemoryQueryExecutor<>(data, FakeEntity.class);
        storage = ProxyFactoryUtil.createProxy(new SimpleQueryParser(), FakeStorage.class, executor, FakeEntity.class, UUID.class);
    }

    @Test
    void testFindByName() {
        List<FakeEntity> result = storage.findByName("Alice");
        assertEquals(1, result.size());
        assertEquals("Alice", result.get(0).getName());
    }

    @Test
    void testFindByScoreGreaterThan() {
        List<FakeEntity> result = storage.findByScoreGreaterThan(60);
        assertEquals(2, result.size());
    }

    @Test
    void testFindByActiveTrue() {
        List<FakeEntity> result = storage.findByActiveTrue();
        assertEquals(2, result.size());
    }

    @Test
    void testFindByIdIn() {
        List<FakeEntity> result = storage.findByIdIn(List.of(uuid1, uuid2));
        assertEquals(2, result.size());
    }

    @Test
    void testSearchByNameWithLike() {
        List<FakeEntity> result = storage.searchByName("%li%");
        assertEquals(2, result.size());
    }

    @Test
    void testTopScorers() {
        List<FakeEntity> result = storage.topScorers(60);
        assertEquals(2, result.size());
        assertTrue(result.get(0).getScore() >= result.get(1).getScore());
    }

    @Test
    void testFindByNameAndActiveTrue() {
        List<FakeEntity> result = storage.findByNameAndActiveTrue("Alice");
        assertEquals(1, result.size());
        assertTrue(result.get(0).isActive());
    }

    @Test
    void testFindByScoreLessThanAndActiveTrue() {
        List<FakeEntity> result = storage.findByScoreLessThanAndActiveTrue(100);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(FakeEntity::isActive));
    }

    @Test
    void testFindByCreatedAtAfter() {
        Instant cutoff = Instant.parse("2025-01-01T00:00:00Z").minusSeconds(800);
        List<FakeEntity> result = storage.findByCreatedAtAfter(cutoff);
        assertEquals(2, result.size());
    }

    @Test
    void testExistsByName() {
        assertTrue(storage.existsByName("Alice"));
        assertFalse(storage.existsByName("Zelda"));
    }

    @Test
    void testCountByActiveTrue() {
        long count = storage.countByActiveTrue();
        assertEquals(2, count);
    }

    @Test
    void testFindByNameNotLike() {
        List<FakeEntity> result = storage.findByNameNotLike("%li%");
        assertEquals(1, result.size());
        assertEquals("Bob", result.get(0).getName());
    }

    @Test
    void testFindByNameIn() {
        List<FakeEntity> result = storage.findByNameIn(List.of("Alice", "Charlie"));
        assertEquals(2, result.size());
    }

    @Test
    void testFindByNameIsNotNull() {
        List<FakeEntity> result = storage.findByNameIsNotNull();
        assertEquals(3, result.size());
    }

    @Test
    void testFindTop2ByActiveTrueOrderByScoreDesc() {
        List<FakeEntity> result = storage.findTop2ByActiveTrueOrderByScoreDesc();
        assertEquals(2, result.size());
        assertTrue(result.get(0).getScore() >= result.get(1).getScore());
    }

    @Test
    void testFindFirstByActiveTrueOrderByCreatedAtDesc() {
        var result = storage.findFirstByActiveTrueOrderByCreatedAtDesc();
        assertTrue(result.isPresent());
        assertEquals("Charlie", result.get().getName());
    }

    @Test
    void testFindActivesWithMinScore() {
        List<FakeEntity> result = storage.findActivesWithMinScore(70);
        assertEquals(2, result.size());
        assertTrue(result.get(0).getScore() >= result.get(1).getScore());
    }

    @Test
    void testFindByNameExclusionPattern() {
        List<FakeEntity> result = storage.findByNameExclusionPattern("%li%");
        assertEquals(1, result.size());
        assertEquals("Bob", result.get(0).getName());
    }

    @Test
    void testFindRecentLowScorer() {
        Optional<FakeEntity> result = storage.findRecentLowScorer(80);
        assertTrue(result.isPresent());
        assertEquals("Charlie", result.get().getName());
    }

    @Test
    void testCountMatchingNames() {
        long count = storage.countMatchingNames("%li%");
        assertEquals(2, count);
    }

    @Test
    void testExistsActiveByName() {
        assertTrue(storage.existsActiveByName("Alice"));
        assertFalse(storage.existsActiveByName("Bob"));
    }

    @Test
    void testFindBetweenDates() {
        Instant start = Instant.parse("2024-12-31T23:40:00Z");
        Instant end = Instant.parse("2025-01-01T00:00:00Z");
        List<FakeEntity> result = storage.findBetweenDates(start, end);
        assertEquals(3, result.size());
    }

    @Test
    void testFindAllByNameInDesc() {
        List<FakeEntity> result = storage.findAllByNameInDesc(List.of("Charlie", "Alice"));
        assertEquals(2, result.size());
        assertEquals("Charlie", result.get(0).getName());
        assertEquals("Alice", result.get(1).getName());
    }

    @Test
    void testFindByScoreGreaterThanEqual() {
        List<FakeEntity> result = storage.findByScoreGreaterThan(69);
        assertEquals(2, result.size());
    }

    @Test
    void testFindByNameIsNull() {
        FakeEntity unnamed = new FakeEntity(UUID.randomUUID(), null, 10, true, Instant.parse("2025-01-01T00:01:00Z"));
        InMemoryQueryExecutor<FakeEntity> executor = new InMemoryQueryExecutor<>(List.of(unnamed), FakeEntity.class);
        FakeStorage tempStorage = ProxyFactoryUtil.createProxy(new SimpleQueryParser(), FakeStorage.class, executor, FakeEntity.class, UUID.class);
        List<FakeEntity> result = tempStorage.findByNameIsNull();
        assertEquals(1, result.size());
    }

    @Test
    void testExistsByMinScoreAndActive() {
        assertTrue(storage.existsByMinScoreAndActive(70));
        assertFalse(storage.existsByMinScoreAndActive(100));
    }

    @Test
    void testCountByScoreGreaterThanEqual() {
        long count = storage.countByScoreGreaterThanEqual(70);
        assertEquals(2, count);
    }

    @Test
    void testCountActiveLowScorers() {
        long count = storage.countActiveLowScorers(80);
        assertEquals(1, count);
    }

    @Test
    void testExistsByCreatedAfter() {
        Instant after = Instant.parse("2024-12-31T23:59:00Z");
        assertTrue(storage.existsByCreatedAfter(after));
        assertFalse(storage.existsByCreatedAfter(Instant.parse("2025-01-02T00:00:00Z")));
    }

    @Test
    void testFindByScoreLessThan() {
        List<FakeEntity> result = storage.findByScoreLessThanAndActiveTrue(80);
        assertEquals(1, result.size());
        assertEquals("Charlie", result.get(0).getName());
    }

    @Test
    void testFindTop1ByScore() {
        List<FakeEntity> result = storage.topScorers(10);
        assertEquals(2, result.size());
        assertTrue(result.get(0).getScore() > result.get(1).getScore());
    }

    @Test
    void testFindInactiveEntities() {
        List<FakeEntity> result = storage.findByScoreGreaterThan(0).stream()
                .filter(e -> !e.isActive())
                .toList();
        assertEquals(1, result.size());
        assertEquals("Bob", result.get(0).getName());
    }

    @Test
    void testFindByCreatedAtAfterNow() {
        Instant future = Instant.parse("2025-01-02T00:00:00Z");
        List<FakeEntity> result = storage.findByCreatedAtAfter(future);
        assertEquals(0, result.size());
    }

    @Test
    void testExistsByNameLowercase() {
        assertFalse(storage.existsByName("alice")); // case-sensitive
    }

    @Test
    void testFindByNameWithEmptyList() {
        List<FakeEntity> result = storage.findByNameIn(List.of());
        assertEquals(0, result.size());
    }

    @Test
    void testCountWhenNoneMatch() {
        long count = storage.countMatchingNames("%zzz%");
        assertEquals(0, count);
    }

    @Test
    void testExistsFalseWithLowScoreAndInactive() {
        assertFalse(storage.existsByMinScoreAndActive(100));
    }

    @Test
    void testFindBetweenDatesExclusive() {
        Instant start = Instant.parse("2025-01-01T00:00:01Z");
        Instant end = Instant.parse("2025-01-01T00:00:02Z");
        List<FakeEntity> result = storage.findBetweenDates(start, end);
        assertEquals(0, result.size());
    }

    @Test
    void testFindByNameWithExactMatchIn() {
        List<FakeEntity> result = storage.findByNameIn(List.of("Bob"));
        assertEquals(1, result.size());
        assertEquals("Bob", result.get(0).getName());
    }

    @Test
    void testFindByScoreEqualBoundary() {
        List<FakeEntity> result = storage.findByScoreGreaterThan(90);
        assertEquals(0, result.size());
    }

    @Test
    void testFindByNameCaseSensitive() {
        List<FakeEntity> result = storage.findByName("ALICE");
        assertEquals(0, result.size());
    }

    @Test
    void testCountByScoreAboveAverage() {
        long count = storage.countByScoreGreaterThanEqual(70);
        assertEquals(2, count);
    }

    @Test
    void testFindByNameWithSpecialCharacters() {
        FakeEntity special = new FakeEntity(UUID.randomUUID(), "Dr. Alice!", 10, true, Instant.now());
        FakeStorage temp = ProxyFactoryUtil.createProxy(new SimpleQueryParser(), FakeStorage.class, new InMemoryQueryExecutor<>(List.of(special), FakeEntity.class), FakeEntity.class, UUID.class);
        List<FakeEntity> result = temp.findByName("Dr. Alice!");
        assertEquals(1, result.size());
    }

    @Test
    void testExistsByNameWhitespaceSensitivity() {
        FakeEntity spaced = new FakeEntity(UUID.randomUUID(), " Alice ", 10, true, Instant.now());
        FakeStorage temp = ProxyFactoryUtil.createProxy(new SimpleQueryParser(), FakeStorage.class, new InMemoryQueryExecutor<>(List.of(spaced), FakeEntity.class), FakeEntity.class, UUID.class);
        assertFalse(temp.existsByName("Alice"));
    }

    @Test
    void testFindAllSortedByNameDescending() {
        List<FakeEntity> result = storage.findAllByNameInDesc(List.of("Alice", "Charlie"));
        assertEquals(List.of("Charlie", "Alice"), result.stream().map(FakeEntity::getName).toList());
    }

    @Test
    void testFindInactiveByScoreAbove() {
        List<FakeEntity> result = storage.findByScoreGreaterThan(10).stream()
                .filter(e -> !e.isActive())
                .toList();
        assertEquals(1, result.size());
        assertEquals("Bob", result.get(0).getName());
    }

    @Test
    void testFindBetweenExactDates() {
        Instant exact = Instant.parse("2025-01-01T00:00:00Z");
        List<FakeEntity> result = storage.findBetweenDates(exact, exact);
        assertEquals(1, result.size());
        assertEquals("Charlie", result.get(0).getName());
    }

    @Test
    void testCountActiveAboveLowThreshold() {
        long count = storage.findByScoreGreaterThan(10).stream()
                .filter(FakeEntity::isActive)
                .count();
        assertEquals(2, count);
    }

    @Test
    void testFindFirstByCreatedAtAndName() {
        Optional<FakeEntity> result = storage.findByNameAndActiveTrue("Charlie").stream().min((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        assertTrue(result.isPresent());
        assertEquals("Charlie", result.get().getName());
    }

    @Test
    void testCountByScoreEqualBoundary() {
        long count = storage.countByScoreGreaterThanEqual(90);
        assertEquals(1, count);
    }

    @Test
    void testFindByNameCaseSensitivity() {
        List<FakeEntity> result = storage.findByName("alice");
        assertEquals(0, result.size());
    }

    @Test
    void testFindAllByNameInDescEmpty() {
        List<FakeEntity> result = storage.findAllByNameInDesc(List.of());
        assertTrue(result.isEmpty());
    }

    @Test
    void testSearchByNameExactMatch() {
        List<FakeEntity> result = storage.searchByName("Alice");
        assertEquals(1, result.size());
        assertEquals("Alice", result.get(0).getName());
    }

    @Test
    void testFindTop2ByActiveTrueOrderByScoreDesc_SecondEntity() {
        List<FakeEntity> result = storage.findTop2ByActiveTrueOrderByScoreDesc();
        assertEquals("Charlie", result.get(1).getName());
    }

    @Test
    void testFindByNameNotLikeI() {
        List<FakeEntity> result = storage.findByNameNotLike("%i%");
        assertEquals(1, result.size());
        assertEquals("Bob", result.get(0).getName());
    }

    @Test
    void testExistsByNameWhitespace() {
        assertFalse(storage.existsByName(" "));
    }

    @Test
    void testFindByNameNotLikeAllNamesContain() {
        List<FakeEntity> result = storage.findByNameNotLike("%l%");
        assertEquals(1, result.size());
    }

    @Test
    void testFindByCreatedAtAfterExactBoundary() {
        Instant boundary = Instant.parse("2025-01-01T00:00:00Z");
        List<FakeEntity> result = storage.findByCreatedAtAfter(boundary);
        assertEquals(0, result.size());
    }

    @Test
    void testFindByNameLikeZ() {
        List<FakeEntity> result = storage.searchByName("%z%");
        assertEquals(0, result.size());
    }

    @Test
    void testCountByActiveTrueWithHighScoreThreshold() {
        long count = storage.findByActiveTrue().stream().filter(e -> e.getScore() > 80).count();
        assertEquals(1, count);
    }

    @Test
    void testFindFirstByActiveTrueOrderByCreatedAtDescIsMostRecent() {
        Optional<FakeEntity> result = storage.findFirstByActiveTrueOrderByCreatedAtDesc();
        assertTrue(result.isPresent());
        assertEquals("Charlie", result.get().getName());
    }

    @Test
    void testFindByNameIsNullAndInactive() {
        FakeEntity e = new FakeEntity(UUID.randomUUID(), null, 0, false, Instant.now());
        FakeStorage temp = ProxyFactoryUtil.createProxy(new SimpleQueryParser(), FakeStorage.class, new InMemoryQueryExecutor<>(List.of(e), FakeEntity.class), FakeEntity.class, UUID.class);
        List<FakeEntity> result = temp.findByNameIsNull();
        assertEquals(1, result.size());
        assertFalse(result.get(0).isActive());
    }

    @Test
    void testExistsActiveByNameCaseInsensitive() {
        assertFalse(storage.existsActiveByName("alice"));
    }

    @Test
    void testFindTopScorersWithNoResults() {
        List<FakeEntity> result = storage.topScorers(1000);
        assertTrue(result.isEmpty());
    }

    @Test
    void testSearchByNameLikeWildcardEdgeCase() {
        List<FakeEntity> result = storage.searchByName("Z%");
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindByCreatedAtWithFutureRange() {
        Instant futureStart = Instant.parse("2026-01-01T00:00:00Z");
        Instant futureEnd = Instant.parse("2026-12-31T00:00:00Z");
        List<FakeEntity> result = storage.findBetweenDates(futureStart, futureEnd);
        assertEquals(0, result.size());
    }

    @Test
    void testFindByScoreGreaterThanEqualMax() {
        List<FakeEntity> result = storage.findByScoreGreaterThan(90);
        assertEquals(0, result.size());
    }

    @Test
    void testFindByScoreLessThanMin() {
        List<FakeEntity> result = storage.findByScoreLessThanAndActiveTrue(10);
        assertTrue(result.isEmpty());
    }

    @Test
    void testExistsByCreatedAfterFarFuture() {
        Instant future = Instant.parse("2030-01-01T00:00:00Z");
        assertFalse(storage.existsByCreatedAfter(future));
    }

    @Test
    void testCountByActiveTrueExact() {
        long count = storage.findByActiveTrue().size();
        assertEquals(2, count);
    }

    @Test
    void testFindBetweenDatesInclusiveRange() {
        Instant start = Instant.parse("2024-12-31T23:40:00Z");
        Instant end = Instant.parse("2025-01-01T00:00:00Z");
        List<FakeEntity> result = storage.findBetweenDates(start, end);
        assertEquals(3, result.size());
    }

    @Test
    void testFindAllByNameInWithDuplicates() {
        List<FakeEntity> result = storage.findByNameIn(List.of("Alice", "Alice"));
        assertEquals(1, result.size());
    }

    @Test
    void testFindByNameWithTrailingSpaces() {
        FakeEntity custom = new FakeEntity(UUID.randomUUID(), "Alice ", 42, true, Instant.now());
        FakeStorage temp = ProxyFactoryUtil.createProxy(new SimpleQueryParser(), FakeStorage.class, new InMemoryQueryExecutor<>(List.of(custom), FakeEntity.class), FakeEntity.class, UUID.class);
        List<FakeEntity> result = temp.findByName("Alice ");
        assertEquals(1, result.size());
    }

    @Test
    void testFindInactiveByScoreBelowThreshold() {
        List<FakeEntity> result = storage.findByScoreGreaterThan(0).stream().filter(e -> !e.isActive() && e.getScore() < 60).toList();
        assertEquals(1, result.size());
    }

    @Test
    void testFindByScoreEqualToExactMatch() {
        List<FakeEntity> result = storage.findByScoreGreaterThan(69);
        assertTrue(result.stream().anyMatch(e -> e.getScore() == 70));
    }

    @Test
    void testExistsByNameNullString() {
        assertFalse(storage.existsByName(null));
    }

    @Test
    void testSearchByNameUnderscoreWildcard() {
        FakeEntity user = new FakeEntity(UUID.randomUUID(), "A_ice", 15, true, Instant.now());
        FakeStorage temp = ProxyFactoryUtil.createProxy(new SimpleQueryParser(), FakeStorage.class, new InMemoryQueryExecutor<>(List.of(user), FakeEntity.class), FakeEntity.class, UUID.class);
        List<FakeEntity> result = temp.searchByName("A_ice");
        assertEquals(1, result.size());
    }

    @Test
    void testFindByScoreBetweenTwoValues() {
        List<FakeEntity> result = storage.findByScoreGreaterThan(60).stream()
                .filter(e -> e.getScore() < 80)
                .toList();
        assertEquals(1, result.size());
        assertEquals("Charlie", result.get(0).getName());
    }

    @Test
    void testFindAllSortedByCreatedAtDesc() {
        List<FakeEntity> result = storage.findByActiveTrue().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .toList();
        assertEquals("Charlie", result.get(0).getName());
    }

    @Test
    void testLikeEndsWithE() {
        List<FakeEntity> result = storage.searchByName("%e");
        assertEquals(2, result.size());
    }

    @Test
    void testLikeMiddleCharacter() {
        List<FakeEntity> result = storage.searchByName("_l_ce");
        assertEquals(1, result.size());
        assertEquals("Alice", result.get(0).getName());
    }

    @Test
    void testLikeExactMatchNoWildcards() {
        List<FakeEntity> result = storage.searchByName("Charlie");
        assertEquals(1, result.size());
        assertEquals("Charlie", result.get(0).getName());
    }

    @Test
    void testLikeSpecialCharacterEscaping() {
        FakeEntity special = new FakeEntity(UUID.randomUUID(), "Dr. Bob$", 10, true, Instant.now());
        FakeStorage temp = ProxyFactoryUtil.createProxy(new SimpleQueryParser(), FakeStorage.class, new InMemoryQueryExecutor<>(List.of(special), FakeEntity.class), FakeEntity.class, UUID.class);
        List<FakeEntity> result = temp.searchByName("Dr. Bob$");
        assertEquals(1, result.size());
        assertEquals("Dr. Bob$", result.get(0).getName());
    }

    @Test
    void testLikeWithUnderscore() {
        FakeEntity underscored = new FakeEntity(UUID.randomUUID(), "Jo_n", 10, true, Instant.now());
        FakeStorage temp = ProxyFactoryUtil.createProxy(new SimpleQueryParser(), FakeStorage.class, new InMemoryQueryExecutor<>(List.of(underscored), FakeEntity.class), FakeEntity.class, UUID.class);
        List<FakeEntity> result = temp.searchByName("Jo_n");
        assertEquals(1, result.size());
        assertEquals("Jo_n", result.get(0).getName());
    }

    @Test
    void testNotLikeEndingWithE() {
        List<FakeEntity> result = storage.findByNameNotLike("%e");
        assertEquals(1, result.size());
        assertTrue(result.stream().noneMatch(e -> e.getName().endsWith("e")));
    }

    @Test
    void testNotLikeMiddleWildcard() {
        List<FakeEntity> result = storage.findByNameNotLike("_l_ce");
        assertEquals(2, result.size());
        assertTrue(result.stream().noneMatch(e -> e.getName().equals("Alice")));
    }

    @Test
    void testNotLikeExactMatch() {
        List<FakeEntity> result = storage.findByNameNotLike("Bob");
        assertEquals(2, result.size());
        assertTrue(result.stream().noneMatch(e -> e.getName().equals("Bob")));
    }

    @Test
    void testNotLikeSingleWildcard() {
        List<FakeEntity> result = storage.findByNameNotLike("C_ar_ie");
        assertEquals(2, result.size());
    }

    @Test
    void testNotLikeWithPercentage() {
        List<FakeEntity> result = storage.findByNameNotLike("%z%");
        assertEquals(3, result.size());
    }

    @Test
    void testFindBetweenDates_Named() {
        Instant start = Instant.parse("2024-12-31T23:40:00Z");
        Instant end = Instant.parse("2025-01-01T00:00:00Z");
        List<FakeEntity> result = storage.findBetweenDatesWithParameters(start, end);
        assertEquals(3, result.size());
    }

    @Test
    void testExistsActiveByName_Named() {
        assertTrue(storage.existsActiveByNameWithParameters("Alice"));
        assertFalse(storage.existsActiveByNameWithParameters("Bob"));
    }

    @Test
    void testCountMatchingNames_Named() {
        long count = storage.countMatchingNamesWithParameters("%li%");
        assertEquals(2, count);
    }

    @Test
    void testFindRecentLowScorer_Named() {
        Optional<FakeEntity> result = storage.findRecentLowScorerWithParameters(80);
        assertTrue(result.isPresent());
        assertEquals("Charlie", result.get().getName());
    }

    @Test
    void testFindByNameExclusionPattern_Named() {
        List<FakeEntity> result = storage.findByNameExclusionPatternWithParameters("%li%");
        assertEquals(1, result.size());
        assertEquals("Bob", result.get(0).getName());
    }

    @Test
    void testFindAllByNameInWithParameters() {
        List<FakeEntity> result = storage.findAllByNameInWithParameters(List.of("Alice", "Charlie"));
        assertEquals(2, result.size());
        assertEquals("Alice", result.get(0).getName());
        assertEquals("Charlie", result.get(1).getName());
    }

    @Test
    void testFindAllByNameInWithParameters_EmptyList() {
        List<FakeEntity> result = storage.findAllByNameInWithParameters(List.of());
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindAllByNameInWithParameters_SingleMatch() {
        List<FakeEntity> result = storage.findAllByNameInWithParameters(List.of("Bob"));
        assertEquals(1, result.size());
        assertEquals("Bob", result.get(0).getName());
    }

    @Test
    void testBrokenQueryBindingError() {
        Instant now = Instant.now();
        assertThrows(ParameterBindingException.class, () -> storage.brokenQuery(now.minusSeconds(1000)));
    }

    @Test
    void testFindByNameIgnoreCase_Lower() {
        List<FakeEntity> result = storage.findByNameIgnoreCase("alice");
        assertEquals(1, result.size());
        assertEquals("Alice", result.get(0).getName());
    }

    @Test
    void testFindByNameIgnoreCase_Upper() {
        List<FakeEntity> result = storage.findByNameIgnoreCaseUpper("ALICE");
        assertEquals(1, result.size());
        assertEquals("Alice", result.get(0).getName());
    }

    @Test
    void testSearchByNameIgnoreCase() {
        List<FakeEntity> result = storage.searchByNameIgnoreCase("%LI%");
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(e -> e.getName().equals("Alice")));
    }

    @Test
    void testSearchByNameIgnoreCaseUpper() {
        List<FakeEntity> result = storage.searchByNameIgnoreCaseUpper("%LI%");
        assertEquals(2, result.size());
    }

    @Test
    void testExistsByNameIgnoreCase() {
        assertTrue(storage.existsByNameIgnoreCase("ALICE"));
        assertFalse(storage.existsByNameIgnoreCase("Zelda"));
    }

    @Test
    void testExistsByNameUpper() {
        FakeEntity underscored = new FakeEntity(UUID.randomUUID(), "JOHN", 10, true, Instant.now());
        FakeStorage temp = ProxyFactoryUtil.createProxy(new SimpleQueryParser(), FakeStorage.class, new InMemoryQueryExecutor<>(List.of(underscored), FakeEntity.class), FakeEntity.class, UUID.class);
        assertTrue(temp.existsByNameUpper("JOHN"));
    }

    @Test
    void testFindActiveAsDto() {
        List<FakeEntityDTO> result = storage.findActiveAsDto();
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(dto -> dto.getId() != null));
        assertTrue(result.stream().anyMatch(dto -> dto.getId().equals(uuid1)));
    }

    @Test
    void testFindRecentLowScorerAsDto() {
        Optional<FakeEntityDTO> result = storage.findRecentLowScorerAsDto(80);
        assertTrue(result.isPresent());
        assertNotNull(result.get().getId());
    }
}