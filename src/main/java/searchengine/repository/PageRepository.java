package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;
import searchengine.modelEntity.PageEntity;
import searchengine.modelEntity.SiteEntity;

import java.util.List;

@Repository
public interface PageRepository extends JpaRepository<PageEntity, Integer> {
    List<PageEntity> findByPathOrSiteEntity(String path, SiteEntity siteEntity);
}
