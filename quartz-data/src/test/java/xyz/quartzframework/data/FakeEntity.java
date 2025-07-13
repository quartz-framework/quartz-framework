package xyz.quartzframework.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import xyz.quartzframework.data.entity.Identity;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
@Builder
public class FakeEntity {

    @Identity
    private UUID id;

    private String name;

    private int score;

    private boolean active;

    private Instant createdAt;

}