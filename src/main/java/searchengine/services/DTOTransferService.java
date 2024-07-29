package searchengine.services;

import searchengine.dto.dtoClasses.IndexDTO;
import searchengine.dto.dtoClasses.LemmaDTO;
import searchengine.dto.dtoClasses.PageDTO;
import searchengine.dto.dtoClasses.SiteDTO;
import searchengine.modelEntity.IndexEntity;
import searchengine.modelEntity.LemmaEntity;
import searchengine.modelEntity.PageEntity;
import searchengine.modelEntity.SiteEntity;

public class DTOTransferService {
    public SiteEntity mapToSiteEntity(SiteDTO siteDTO) {
        SiteEntity siteEntity = new SiteEntity();
        siteEntity.setId(siteDTO.getId());
        siteEntity.setUrl(siteDTO.getUrl());
        siteEntity.setName(siteDTO.getName());
        siteEntity.setStatus(siteDTO.getStatus());
        siteEntity.setStatusTime(siteDTO.getStatusTime());
        return siteEntity;
    }
    public PageEntity mapToPageEntity(PageDTO pageDTO, SiteEntity siteEntity) {
        PageEntity pageEntity = new PageEntity();
        pageEntity.setId(pageDTO.getId());
        pageEntity.setPath(pageDTO.getPath());
        pageEntity.setCode(pageDTO.getCode());
        pageEntity.setContent(pageDTO.getContent());
        pageEntity.setSiteEntity(siteEntity);
        return pageEntity;
    }
    public LemmaEntity mapToLemmaEntity(LemmaDTO lemmaDTO, SiteEntity siteEntity) {
        LemmaEntity lemmaEntity = new LemmaEntity();
        lemmaEntity.setId(lemmaDTO.getId());
        lemmaEntity.setSiteEntity(siteEntity);
        lemmaEntity.setLemma(lemmaDTO.getLemma());
        lemmaEntity.setFrequency(lemmaDTO.getFrequency());
        return lemmaEntity;
    }
    public IndexEntity mapToIndexEntity(IndexDTO indexDTO, LemmaEntity lemmaEntity, PageEntity pageEntity) {
        IndexEntity indexEntity = new IndexEntity();
        indexEntity.setLemmaEntity(lemmaEntity);
        indexEntity.setPageEntity(pageEntity);
        indexEntity.setId(indexDTO.getId());
        indexEntity.setRank(indexDTO.getRank());
        return indexEntity;
    }

    public PageDTO mapToPageDto(PageEntity pageEntity) {
        PageDTO pageDTO = new PageDTO();
        pageDTO.setId(pageEntity.getId());
        pageDTO.setPath(pageEntity.getPath());
        pageDTO.setCode(pageEntity.getCode());
        pageDTO.setContent(pageEntity.getContent());
        pageDTO.setSiteEntityId(pageEntity.getSiteEntity().getId());
        return pageDTO;
    }
    public SiteDTO mapToSiteDto(SiteEntity siteEntity) {
        SiteDTO siteDTO = new SiteDTO();
        siteDTO.setId(siteEntity.getId());
        siteDTO.setUrl(siteEntity.getUrl());
        siteDTO.setName(siteEntity.getName());
        siteDTO.setStatus(siteEntity.getStatus());
        siteDTO.setStatusTime(siteEntity.getStatusTime());
        return siteDTO;
    }
    public LemmaDTO mapToLemmaDto(LemmaEntity lemmaEntity) {
        LemmaDTO lemmaDTO = new LemmaDTO();
        lemmaDTO.setId(lemmaEntity.getId());
        lemmaDTO.setSiteEntityId(lemmaEntity.getSiteEntity().getId());
        lemmaDTO.setLemma(lemmaEntity.getLemma());
        lemmaDTO.setFrequency(lemmaEntity.getFrequency());
        return lemmaDTO;
    }
    public IndexDTO mapToIndexDto(IndexEntity indexEntity) {
        IndexDTO indexDTO = new IndexDTO();
        indexDTO.setId(indexEntity.getId());
        indexDTO.setPageEntityId(indexEntity.getPageEntity().getId());
        indexDTO.setLemmaEntityId(indexEntity.getLemmaEntity().getId());
        indexDTO.setRank(indexEntity.getRank());
        return indexDTO;
    }
}
