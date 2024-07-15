package searchengine.DTOClasses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import searchengine.model.IndexStatus;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SiteDTO {

    private Integer id;
    private String url;
    private String name;
    private IndexStatus status;
    private LocalDateTime statusTime;
    private String lastError;
}
