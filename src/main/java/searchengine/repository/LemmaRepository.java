package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;
import searchengine.modelEntity.LemmaEntity;
import searchengine.modelEntity.SiteEntity;

import java.util.List;

@Repository
public interface LemmaRepository extends JpaRepository<LemmaEntity, Integer> {
    List<LemmaEntity> findBySiteEntity(SiteEntity siteEntity);
}
