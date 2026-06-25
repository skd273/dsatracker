package com.dsatracker.service;

import com.dsatracker.config.A2zSheetLoader;
import com.dsatracker.model.Problem;
import com.dsatracker.model.Topic;
import com.dsatracker.repository.ProblemRepository;
import com.dsatracker.repository.TopicRepository;
import jakarta.persistence.EntityManager;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DsaSheetMigrationService {

    private final TopicRepository topicRepository;
    private final ProblemRepository problemRepository;
    private final EntityManager entityManager;
    private final A2zSheetLoader sheetLoader;

    public DsaSheetMigrationService(
            TopicRepository topicRepository,
            ProblemRepository problemRepository,
            EntityManager entityManager,
            A2zSheetLoader sheetLoader) {
        this.topicRepository = topicRepository;
        this.problemRepository = problemRepository;
        this.entityManager = entityManager;
        this.sheetLoader = sheetLoader;
    }

    public boolean needsReseed() {
        if (topicRepository.count() != sheetLoader.topicCount()) {
            return true;
        }
        if (problemRepository.count() != sheetLoader.problemCount()) {
            return true;
        }
        try {
            Long versionMatches = entityManager.createQuery(
                            "SELECT COUNT(t) FROM Topic t WHERE t.notes = :version", Long.class)
                    .setParameter("version", A2zSheetLoader.SHEET_VERSION)
                    .getSingleResult();
            return versionMatches == 0;
        } catch (RuntimeException ex) {
            return true;
        }
    }

    @Transactional
    public void clearSeededNotes() {
        entityManager.createQuery(
                        "UPDATE Problem p SET p.revisionNotes = null "
                                + "WHERE p.notes IS NOT NULL AND p.revisionNotes = p.notes")
                .executeUpdate();
        entityManager.createQuery("UPDATE Problem p SET p.notes = null WHERE p.notes IS NOT NULL")
                .executeUpdate();
    }

    @Transactional
    public void reseed() {
        SessionFactory sessionFactory = entityManager.getEntityManagerFactory().unwrap(SessionFactory.class);
        sessionFactory.getSchemaManager().dropMappedObjects(true);
        sessionFactory.getSchemaManager().exportMappedObjects(true);
        entityManager.flush();
        entityManager.clear();

        A2zSheetLoader.SheetData sheet = sheetLoader.getSheetData();
        int topicOrder = 1;

        for (A2zSheetLoader.SheetTopic topicSeed : sheet.topics()) {
            Topic topic = new Topic();
            topic.setName(topicSeed.name());
            topic.setCategory(sheet.category());
            topic.setDescription(topicSeed.description());
            topic.setNotes(A2zSheetLoader.SHEET_VERSION);
            topic.setTargetProblems(topicSeed.problems().size());
            topic.setDisplayOrder(topicOrder++);
            topic = topicRepository.save(topic);

            var sorted = topicSeed.problems().stream()
                    .sorted(Comparator.comparing(A2zSheetLoader.SheetProblem::toDifficulty))
                    .toList();

            int problemOrder = 1;
            for (A2zSheetLoader.SheetProblem seed : sorted) {
                Problem problem = new Problem();
                problem.setTitle(seed.title());
                problem.setUrl(seed.url() != null ? seed.url() : "");
                problem.setPlatform(seed.toPlatform());
                problem.setDifficulty(seed.toDifficulty());
                problem.setDisplayOrder(problemOrder++);
                problem.setTopic(topic);
                problemRepository.save(problem);
            }
        }
    }

    @Transactional
    public int syncLinksFromSheet() {
        Map<String, Map<String, A2zSheetLoader.SheetProblem>> sheetByTopic = new HashMap<>();
        for (A2zSheetLoader.SheetTopic topicSeed : sheetLoader.getSheetData().topics()) {
            Map<String, A2zSheetLoader.SheetProblem> problems = new HashMap<>();
            for (A2zSheetLoader.SheetProblem problemSeed : topicSeed.problems()) {
                problems.put(problemSeed.title(), problemSeed);
            }
            sheetByTopic.put(topicSeed.name(), problems);
        }

        int updated = 0;
        for (Topic topic : topicRepository.findAll()) {
            Map<String, A2zSheetLoader.SheetProblem> seeds = sheetByTopic.get(topic.getName());
            if (seeds == null) {
                continue;
            }
            for (Problem problem : topic.getProblems()) {
                A2zSheetLoader.SheetProblem seed = seeds.get(problem.getTitle());
                if (seed == null || seed.url() == null || seed.url().isBlank()) {
                    continue;
                }
                String nextUrl = seed.url();
                boolean changed = !nextUrl.equals(problem.getUrl())
                        || seed.toPlatform() != problem.getPlatform();
                if (changed) {
                    problem.setUrl(nextUrl);
                    problem.setPlatform(seed.toPlatform());
                    problemRepository.save(problem);
                    updated++;
                }
            }
        }
        return updated;
    }
}
