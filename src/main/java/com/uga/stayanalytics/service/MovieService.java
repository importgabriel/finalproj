package com.uga.stayanalytics.service;

import com.uga.stayanalytics.model.Content;
import com.uga.stayanalytics.model.Genre;
import com.uga.stayanalytics.model.Platform;
import com.uga.stayanalytics.model.SimilarTitle;
import com.uga.stayanalytics.repository.ContentDao;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MovieService {

    private final ContentDao contentDao;

    public MovieService(ContentDao contentDao) { this.contentDao = contentDao; }

    public List<Content> trending(int limit) { return contentDao.findTrending(limit); }

    public Optional<Content> findById(String contentId) { return contentDao.findById(contentId); }

    public List<Content> search(String title, Integer genreId, Integer platformId,
                                Integer year, String type) {
        return contentDao.search(title, genreId, platformId, year, type, 100);
    }

    public List<Platform> allPlatforms() { return contentDao.allPlatforms(); }
    public List<Genre> allGenres() { return contentDao.allGenres(); }

    public List<SimilarTitle> similar(String contentId) {
        return contentDao.similarByTags(contentId, 8);
    }

    public List<String> tagsFor(String contentId) {
        return contentDao.tagsFor(contentId);
    }
}
