package searchengine.services;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RecursiveTask;

import static org.hibernate.tool.schema.SchemaToolingLogging.LOGGER;

public class ForkSiteParser extends RecursiveTask<Set<String>> {
    private final String link;
    private static final Set<String> linkSet = ConcurrentHashMap.newKeySet();

    public ForkSiteParser(String link) {
        this.link = link;
    }
    @Override
    protected Set<String> compute() {
        Set<String> links = new TreeSet<>();
        HashSet<ForkSiteParser> taskList = new HashSet<>();
        try {
            Thread.sleep(1000);
            Document doc = Jsoup.connect(link).
                    userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6").
                    referrer("http://www.google.com").get();
            Elements element = doc.select("a");
            for (Element e : element) {
                String linkFromAttr = e.absUrl("href").replaceAll("/$", "");
                if (!linkFromAttr.contains(link) || !linkSet.add(linkFromAttr) ||
                        linkFromAttr.contains("#") || linkFromAttr.contains(".png") ||
                        linkFromAttr.contains(".pdf")) {
                    continue;
                }
                links.add(linkFromAttr);
                ForkSiteParser task = new ForkSiteParser(linkFromAttr);
                task.fork();
                taskList.add(task);
            }
        } catch (HttpStatusException h) {
            LOGGER.error("Ошибка 404 получения URL: ", link, h);
        } catch (Exception e) {
            LOGGER.error("Ошибка парсинга ссылки: ", link, e);
        }

        for (ForkSiteParser task1 : taskList) {
            links.addAll(task1.join());
        }
        return links;
    }
}
