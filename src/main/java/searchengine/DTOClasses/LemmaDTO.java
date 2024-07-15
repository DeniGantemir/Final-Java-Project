package searchengine.DTOClasses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LemmaDTO {

    private Integer id;
    private Integer siteEntityId;
    private String lemma;
    private Integer frequency;
}
