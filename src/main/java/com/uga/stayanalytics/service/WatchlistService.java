package com.uga.stayanalytics.service;

import com.uga.stayanalytics.model.WatchlistEntry;
import com.uga.stayanalytics.repository.WatchlistDao;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class WatchlistService {

    private static final Set<String> STATUSES = Set.of("PLAN_TO_WATCH", "WATCHING", "WATCHED");

    private final WatchlistDao dao;

    public WatchlistService(WatchlistDao dao) { this.dao = dao; }

    public List<WatchlistEntry> forUser(long userId) { return dao.findForUser(userId); }

    public void add(long userId, String contentId) {
        dao.addIfAbsent(userId, contentId, "PLAN_TO_WATCH");
    }

    public void updateStatus(long userId, String contentId, String status) {
        if (!STATUSES.contains(status)) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
        dao.updateStatus(userId, contentId, status);
    }

    public void remove(long userId, String contentId) { dao.remove(userId, contentId); }

    public boolean contains(long userId, String contentId) { return dao.contains(userId, contentId); }
}
