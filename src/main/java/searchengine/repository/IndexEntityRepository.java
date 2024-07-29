package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;
import searchengine.modelEntity.IndexEntity;
import searchengine.modelEntity.PageEntity;

import java.util.List;


@Repository
public interface IndexEntityRepository extends JpaRepository<IndexEntity, Integer> {
    List<IndexEntity> findByPageEntity(PageEntity pageEntity);
}
