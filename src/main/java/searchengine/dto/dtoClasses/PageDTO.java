package searchengine.dto.dtoClasses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PageDTO {

    private Integer id;
    private String path;
    private Integer code;
    private String content;
    private Integer siteEntityId;
}
