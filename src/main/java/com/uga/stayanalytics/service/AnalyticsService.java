package com.uga.stayanalytics.service;

import com.uga.stayanalytics.model.GenrePlatformStat;
import com.uga.stayanalytics.model.PlatformStat;
import com.uga.stayanalytics.repository.AnalyticsDao;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnalyticsService {

    private final AnalyticsDao dao;

    public AnalyticsService(AnalyticsDao dao) { this.dao = dao; }

    public List<PlatformStat> perPlatform() { return dao.perPlatform(); }
    public List<GenrePlatformStat> perGenrePlatform() { return dao.perGenrePlatform(5); }
    public List<AnalyticsDao.TopRated> topCommunityRated() { return dao.topCommunityRated(10, 3); }
}
