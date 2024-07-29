package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.modelEntity.*;
import searchengine.repository.IndexEntityRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ForkJoinPool;

import static org.hibernate.tool.schema.SchemaToolingLogging.LOGGER;

@Service
@RequiredArgsConstructor
public class SiteIndexingService {

    private final SitesList sitesList;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexEntityRepository indexEntityRepository;

    private final ForkJoinPool forkJoinPool = new ForkJoinPool();
    private volatile boolean IsStopIndexing = false;
    private final PageLemmaIndexClass pageLemmaIndexClass;

    public void startIndexing() {
        if (IsStopIndexing) {
            return;
        }
        LOGGER.info("Запускается индексация сайтов");
        if (siteRepository != null ||
                pageRepository != null ||
                lemmaRepository != null ||
                indexEntityRepository != null) {
            indexEntityRepository.deleteAll();
            lemmaRepository.deleteAll();
            pageRepository.deleteAll();
            siteRepository.deleteAll();
        }
        for (Site site : sitesList.getSites()) {
            forkJoinPool.submit(new RecursiveSiteIndexingService(site,
                    siteRepository,
                    pageLemmaIndexClass));
        }
    }

    public void stopIndexing() {
        LOGGER.info("Запущен процесс остановки индексация сайтов");
        try {
            IsStopIndexing = true;
            if (forkJoinPool!= null) {
                forkJoinPool.shutdownNow();
            }
            List<SiteEntity> siteEntities = siteRepository.findAll();
            for (SiteEntity siteEntity : siteEntities) {
                if (siteEntity.getStatus() == IndexStatus.INDEXING) {

                    siteEntity.setStatus(IndexStatus.FAILED);
                    siteEntity.setLastError("Индексация остановлена пользователем");
                    siteRepository.saveAndFlush(siteEntity);
                }
            }
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            LOGGER.error("Ошибка остановки индексации", e);
        }
        LOGGER.info("Индексация успешно остановлена");
    }


    public void indexPageMethod(String url) throws IOException {
        SiteEntity siteEntity = siteRepository.findByUrl(url).orElseThrow();

        List<PageEntity> existingPageEntities = pageRepository.findByPathOrSiteEntity(url, siteEntity);

        for (PageEntity existingPageEntity : existingPageEntities) {
            List<IndexEntity> indexEntities = indexEntityRepository.findByPageEntity(existingPageEntity);
            indexEntities.forEach(indexEntityRepository::delete);
            pageRepository.delete(existingPageEntity);
        }
        pageRepository.flush();

        List<LemmaEntity> lemmaEntities = lemmaRepository.findBySiteEntity(siteEntity);
        lemmaEntities.forEach(lemmaRepository::delete);

        pageLemmaIndexClass.getPageLemmaIndexSiteMethod(siteEntity, url);
        LOGGER.info("Индексация сайта " + url + "завершена");
    }
}
