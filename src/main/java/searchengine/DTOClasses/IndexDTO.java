package searchengine.DTOClasses;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class IndexDTO {

    private Integer id;
    private Integer pageEntityId;
    private Integer lemmaEntityId;
    private Float rank;
}
