document.addEventListener('DOMContentLoaded', () => {
    initTheme();
    initAccordions();
    initSectionPagination();
    initPotd();
    initResetProgress();
    initFilters();
    initProblemToggles();
    initRevisionNotes();
});

function initTheme() {
    const btn = document.getElementById('themeToggleBtn');
    if (!btn) {
        return;
    }

    btn.addEventListener('click', () => {
        const current = document.documentElement.getAttribute('data-theme') || 'dark';
        const next = current === 'dark' ? 'light' : 'dark';
        document.documentElement.setAttribute('data-theme', next);
        localStorage.setItem('theme', next);
    });
}

function initResetProgress() {
    const btn = document.getElementById('resetProgressBtn');
    if (!btn) {
        return;
    }

    btn.addEventListener('click', async () => {
        const confirmed = window.confirm(
            'Reset all progress? This clears every solved checkbox and revision note.'
        );
        if (!confirmed) {
            return;
        }

        btn.disabled = true;
        try {
            const response = await fetch('/api/progress/reset', { method: 'POST' });
            if (!response.ok) {
                const err = await response.json();
                throw new Error(err.error || 'Reset failed');
            }
            window.location.reload();
        } catch (err) {
            alert(err.message);
            btn.disabled = false;
        }
    });
}

function initPotd() {
    document.querySelectorAll('.potd-jump-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            const topicId = btn.dataset.topicId;
            const problemId = btn.dataset.problemId;
            const accordion = document.querySelector(`.topic-accordion[data-topic-id="${topicId}"]`);
            if (!accordion) {
                return;
            }

            accordion.classList.remove('hidden');

            const trigger = accordion.querySelector('.topic-accordion-trigger');
            const panel = accordion.querySelector('.topic-accordion-panel');
            trigger.setAttribute('aria-expanded', 'true');
            accordion.classList.add('open');
            panel.hidden = false;

            accordion.scrollIntoView({ behavior: 'smooth', block: 'start' });

            if (problemId) {
                jumpToProblemInSection(accordion, problemId);
            }
        });
    });

    document.querySelectorAll('.potd-toggle').forEach(checkbox => {
        checkbox.addEventListener('change', async (e) => {
            const id = e.target.dataset.id;
            const potdBody = e.target.closest('.potd-body');

            try {
                const response = await fetch(`/api/problems/${id}/toggle`, { method: 'PUT' });
                if (!response.ok) {
                    const err = await response.json();
                    throw new Error(err.error || 'Toggle failed');
                }

                const problem = await response.json();
                potdBody.classList.toggle('solved', problem.solved);

                const accordion = document.querySelector(`.problem-card[data-problem-id="${id}"]`)?.closest('.topic-accordion');
                const listCheckbox = document.querySelector(`.problem-card[data-problem-id="${id}"] .problem-toggle`);
                if (listCheckbox) {
                    listCheckbox.checked = problem.solved;
                    listCheckbox.closest('.problem-card')?.classList.toggle('solved', problem.solved);
                }
                updateTopicProgress(accordion);
                await updateOverallProgress();
                await refreshActivity();
            } catch (err) {
                e.target.checked = !e.target.checked;
                alert(err.message);
            }
        });
    });
}

function initAccordions() {
    document.querySelectorAll('.topic-accordion-trigger').forEach(trigger => {
        trigger.addEventListener('click', () => {
            const accordion = trigger.closest('.topic-accordion');
            const panel = accordion.querySelector('.topic-accordion-panel');
            const expanded = trigger.getAttribute('aria-expanded') === 'true';

            trigger.setAttribute('aria-expanded', String(!expanded));
            accordion.classList.toggle('open', !expanded);
            panel.hidden = expanded;

            if (!expanded) {
                accordion.dataset.currentPage = '1';
                updateSectionPage(accordion);
            }
        });
    });
}

const SECTION_PAGE_SIZE = 5;

function initSectionPagination() {
    document.querySelectorAll('.topic-accordion').forEach(accordion => {
        accordion.dataset.currentPage = '1';

        const prevBtn = accordion.querySelector('.topic-page-prev');
        const nextBtn = accordion.querySelector('.topic-page-next');
        if (prevBtn && nextBtn) {
            prevBtn.addEventListener('click', () => changeSectionPage(accordion, -1));
            nextBtn.addEventListener('click', () => changeSectionPage(accordion, 1));
        }
        updateSectionPage(accordion);
    });
}

function getSectionCards(accordion) {
    return Array.from(accordion.querySelectorAll('.problem-card:not(.hidden)'));
}

function changeSectionPage(accordion, delta) {
    const current = parseInt(accordion.dataset.currentPage || '1', 10);
    accordion.dataset.currentPage = String(current + delta);
    updateSectionPage(accordion);

    const scrollArea = accordion.querySelector('.topic-panel-scroll');
    if (scrollArea) {
        scrollArea.scrollTop = 0;
    }
}

function updateSectionPage(accordion) {
    const cards = getSectionCards(accordion);
    const pagination = accordion.querySelector('.topic-pagination');
    const prevBtn = accordion.querySelector('.topic-page-prev');
    const nextBtn = accordion.querySelector('.topic-page-next');
    const pageInfo = accordion.querySelector('.topic-page-info');
    const totalPages = Math.max(1, Math.ceil(cards.length / SECTION_PAGE_SIZE));

    let page = parseInt(accordion.dataset.currentPage || '1', 10);
    page = Math.min(Math.max(page, 1), totalPages);
    accordion.dataset.currentPage = String(page);

    cards.forEach((card, index) => {
        const cardPage = Math.floor(index / SECTION_PAGE_SIZE) + 1;
        card.classList.toggle('page-hidden', cardPage !== page);
    });

    accordion.querySelectorAll('.difficulty-group').forEach(group => {
        const visibleOnPage = group.querySelectorAll('.problem-card:not(.hidden):not(.page-hidden)').length > 0;
        group.classList.toggle('group-page-empty', !visibleOnPage);
    });

    if (pagination) {
        pagination.hidden = cards.length <= SECTION_PAGE_SIZE;
    }
    if (prevBtn) {
        prevBtn.disabled = page <= 1;
    }
    if (nextBtn) {
        nextBtn.disabled = page >= totalPages;
    }
    if (pageInfo) {
        pageInfo.textContent = `Page ${page} of ${totalPages}`;
    }
}

function jumpToProblemInSection(accordion, problemId) {
    const card = accordion.querySelector(`.problem-card[data-problem-id="${problemId}"]`);
    if (!card) {
        return;
    }

    const cards = getSectionCards(accordion);
    const index = cards.indexOf(card);
    if (index >= 0) {
        accordion.dataset.currentPage = String(Math.floor(index / SECTION_PAGE_SIZE) + 1);
        updateSectionPage(accordion);
    }

    setTimeout(() => {
        const scrollArea = accordion.querySelector('.topic-panel-scroll');
        if (scrollArea && card.offsetParent) {
            const top = card.offsetTop - scrollArea.offsetTop;
            scrollArea.scrollTo({ top: Math.max(0, top - 12), behavior: 'smooth' });
        }
        card.style.outline = '2px solid var(--accent)';
        setTimeout(() => { card.style.outline = ''; }, 2000);
    }, 150);
}

function initFilters() {
    const searchInput = document.getElementById('searchInput');
    if (!searchInput) {
        return;
    }

    function applyFilters() {
        const query = searchInput.value.toLowerCase().trim();

        document.querySelectorAll('.topic-accordion').forEach(accordion => {
            const name = (accordion.dataset.name || '').toLowerCase();
            const problems = (accordion.dataset.problems || '').toLowerCase();
            const revisionNotes = (accordion.dataset.revisionNotes || '').toLowerCase();

            const sectionMatch = !query
                || name.includes(query)
                || problems.includes(query)
                || revisionNotes.includes(query);

            let visibleProblems = 0;
            accordion.querySelectorAll('.problem-card').forEach(card => {
                const title = (card.dataset.title || '').toLowerCase();
                const cardRevision = (card.dataset.revision || '').toLowerCase();

                const cardMatch = !query
                    || title.includes(query)
                    || cardRevision.includes(query)
                    || sectionMatch;

                card.classList.toggle('hidden', !cardMatch);
                if (cardMatch) {
                    visibleProblems++;
                }
            });

            accordion.querySelectorAll('.difficulty-group').forEach(group => {
                const visibleCards = group.querySelectorAll('.problem-card:not(.hidden)');
                group.classList.toggle('hidden', visibleCards.length === 0);
            });

            const visible = visibleProblems > 0;
            accordion.classList.toggle('hidden', !visible);

            if (query && visible) {
                const trigger = accordion.querySelector('.topic-accordion-trigger');
                const panel = accordion.querySelector('.topic-accordion-panel');
                trigger.setAttribute('aria-expanded', 'true');
                accordion.classList.add('open');
                panel.hidden = false;
            }

            accordion.dataset.currentPage = '1';
            updateSectionPage(accordion);
        });

        document.querySelectorAll('.category-block').forEach(block => {
            const visibleSections = block.querySelectorAll('.topic-accordion:not(.hidden)');
            block.style.display = visibleSections.length > 0 ? '' : 'none';
        });
    }

    searchInput.addEventListener('input', applyFilters);
}

function initProblemToggles() {
    document.querySelectorAll('.problem-toggle').forEach(checkbox => {
        checkbox.addEventListener('change', async (e) => {
            const id = e.target.dataset.id;
            const item = e.target.closest('.problem-card');
            const accordion = e.target.closest('.topic-accordion');

            try {
                const response = await fetch(`/api/problems/${id}/toggle`, { method: 'PUT' });
                if (!response.ok) {
                    const err = await response.json();
                    throw new Error(err.error || 'Toggle failed');
                }

                const problem = await response.json();
                item.classList.toggle('solved', problem.solved);

                const potdToggle = document.querySelector(`.potd-body[data-problem-id="${id}"] .potd-toggle`);
                const potdBody = document.querySelector(`.potd-body[data-problem-id="${id}"]`);
                if (potdToggle) {
                    potdToggle.checked = problem.solved;
                }
                if (potdBody) {
                    potdBody.classList.toggle('solved', problem.solved);
                }

                updateTopicProgress(accordion);
                await updateOverallProgress();
                await refreshActivity();
            } catch (err) {
                e.target.checked = !e.target.checked;
                alert(err.message);
            }
        });
    });
}

function updateTopicProgress(accordion) {
    if (!accordion) {
        return;
    }

    const total = accordion.querySelectorAll('.problem-toggle').length;
    const solved = accordion.querySelectorAll('.problem-toggle:checked').length;
    const percent = total > 0 ? Math.round((solved * 100) / total) : 0;

    const fill = accordion.querySelector('.topic-fill');
    if (fill) {
        fill.style.width = `${percent}%`;
    }

    const solvedEl = accordion.querySelector('.topic-solved-count');
    if (solvedEl) {
        solvedEl.textContent = String(solved);
    }
}

function updateOverallProgress() {
    return fetch('/api/topics/stats')
        .then(response => (response.ok ? response.json() : null))
        .then(stats => {
            if (!stats) {
                return;
            }

            const percentEl = document.getElementById('overallPercent');
            const fillEl = document.getElementById('overallFill');
            const solvedEl = document.getElementById('totalSolved');

            if (percentEl) {
                percentEl.textContent = `${stats.overallProgressPercent}%`;
            }
            if (fillEl) {
                fillEl.style.width = `${stats.overallProgressPercent}%`;
            }
            if (solvedEl) {
                solvedEl.textContent = String(stats.totalProblemsSolved);
            }
        })
        .catch(() => {
            // ignore refresh errors
        });
}

function levelFor(count, maxCount) {
    if (count <= 0 || maxCount <= 0) {
        return 0;
    }
    if (count === 1) {
        return 1;
    }
    if (count <= maxCount / 3) {
        return 2;
    }
    if (count <= (maxCount * 2) / 3) {
        return 3;
    }
    return 4;
}

async function refreshActivity() {
    try {
        const response = await fetch('/api/activity');
        if (!response.ok) {
            return;
        }

        const activity = await response.json();
        const currentStreak = document.getElementById('currentStreak');
        const longestStreak = document.getElementById('longestStreak');
        const grid = document.getElementById('heatmapGrid');

        if (currentStreak) {
            currentStreak.textContent = String(activity.currentStreak);
        }
        if (longestStreak) {
            longestStreak.textContent = String(activity.longestStreak);
        }
        if (!grid) {
            return;
        }

        grid.dataset.max = String(activity.maxCount);
        grid.innerHTML = '';

        activity.weeks.forEach(week => {
            const weekEl = document.createElement('div');
            weekEl.className = 'heatmap-week';

            week.forEach(day => {
                const cell = document.createElement('div');
                cell.className = 'heatmap-cell';
                cell.dataset.date = day.date || '';
                cell.dataset.count = String(day.count);

                if (day.count < 0) {
                    cell.classList.add('pad');
                } else {
                    cell.classList.add(`level-${levelFor(day.count, activity.maxCount)}`);
                    cell.title = `${day.date}: ${day.count} solved`;
                }

                weekEl.appendChild(cell);
            });

            grid.appendChild(weekEl);
        });
    } catch {
        // ignore refresh errors
    }
}

function initRevisionNotes() {
    const debounceTimers = new Map();

    document.querySelectorAll('.revision-textarea').forEach(textarea => {
        textarea.addEventListener('input', () => {
            const id = textarea.dataset.id;
            const status = document.querySelector(`.revision-status[data-id="${id}"]`);
            if (status) {
                status.textContent = 'Saving...';
                status.className = 'revision-status';
            }

            clearTimeout(debounceTimers.get(id));
            debounceTimers.set(id, setTimeout(() => saveRevisionNotes(id, textarea, status), 600));
        });
    });
}

async function saveRevisionNotes(id, textarea, status) {
    try {
        const response = await fetch(`/api/problems/${id}/revision-notes`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ revisionNotes: textarea.value }),
        });

        if (!response.ok) {
            const err = await response.json();
            throw new Error(err.error || 'Save failed');
        }

        const card = textarea.closest('.problem-card');
        const accordion = textarea.closest('.topic-accordion');
        if (card) {
            card.dataset.revision = textarea.value;
        }
        if (accordion) {
            const notes = Array.from(accordion.querySelectorAll('.problem-card'))
                .map(c => c.dataset.revision || '')
                .join(' ');
            accordion.dataset.revisionNotes = notes;
        }

        if (status) {
            status.textContent = 'Saved';
            status.className = 'revision-status saved';
            setTimeout(() => {
                if (status.textContent === 'Saved') {
                    status.textContent = '';
                }
            }, 2000);
        }
    } catch (err) {
        if (status) {
            status.textContent = err.message;
            status.className = 'revision-status error';
        }
    }
}
