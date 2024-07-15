package searchengine.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "`SiteEntity`")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SiteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "`id`")
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "`status`", columnDefinition = "ENUM('INDEXING', 'INDEXED', 'FAILED')")
    private IndexStatus status;

    @Column(name = "`status_time`", nullable = false, updatable = false)
    private LocalDateTime statusTime;

    @Column(name = "`last_error`", columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "`url`", nullable = false, columnDefinition = "VARCHAR(255)")
    private String url;

    @Column(name = "`name`", nullable = false, columnDefinition = "VARCHAR(255)")
    private String name;
}
