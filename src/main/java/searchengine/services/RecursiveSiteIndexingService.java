package searchengine.services;

import org.jsoup.HttpStatusException;
import searchengine.DTOClasses.SiteDTO;
import searchengine.config.Site;
import searchengine.model.*;
import searchengine.repository.IndexEntityRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.io.IOException;
import java.time.LocalDateTime;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import static org.hibernate.tool.schema.SchemaToolingLogging.LOGGER;

public class RecursiveSiteIndexingService extends RecursiveTask<Void> {
    private final Site site;
    private final SiteRepository siteRepository;
    private final DTOTransferService dtoTransferService = new DTOTransferService();
    private final PageLemmaIndexClass pageLemmaIndexClass;


    public RecursiveSiteIndexingService(Site site,
                                        SiteRepository siteRepository,
                                        PageLemmaIndexClass pageLemmaIndexClass) {
        this.site = site;
        this.pageLemmaIndexClass = pageLemmaIndexClass;
        this.siteRepository = siteRepository;
    }

    @Override
    protected Void compute() {
        try {
            // Создаем новую запись в таблице site со статусом INDEXING
            SiteDTO siteDTO = new SiteDTO();
            siteDTO.setUrl(site.getUrl());
            siteDTO.setName(site.getName());
            siteDTO.setStatus(IndexStatus.INDEXING);
            siteDTO.setStatusTime(LocalDateTime.now());

            SiteEntity siteEntity = dtoTransferService.mapToSiteEntity(siteDTO);
            siteRepository.save(siteEntity);

            // Обходим все страницы, начиная с главной
            pageLemmaIndexClass.getPageLemmaIndexSiteMethod(siteEntity, site.getUrl());

            LOGGER.info("Индексирование сайта " + site.getName() + " завершена");
            SiteEntity existingSiteEntity = siteRepository.findAll().stream()
                    .filter(se -> se.getUrl().equals(site.getUrl()))
                    .findFirst().orElse(null);

            // Меняем статус на INDEXED
            if (existingSiteEntity!= null) {
                existingSiteEntity.setStatus(IndexStatus.INDEXED);
                siteRepository.save(existingSiteEntity);
            }
        } catch (HttpStatusException h) {
            LOGGER.info("Ошибка: " + h);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
