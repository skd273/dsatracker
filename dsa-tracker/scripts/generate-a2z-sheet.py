#!/usr/bin/env python3
"""Generate a2z-sheet.json from the official Striver A2Z sheet structure."""
import json
import re
from pathlib import Path
from typing import Optional

ROOT = Path(__file__).resolve().parents[1]
OFFICIAL = ROOT / "src/main/resources/data/strivers-a2z-official.json"
LINK_CACHE = ROOT / "src/main/resources/data/a2zdsa-links.json"
OVERRIDES = ROOT / "src/main/resources/data/a2z-link-overrides.json"
OUT = ROOT / "src/main/resources/data/a2z-sheet.json"

TOPIC_DESC = {
    "Learn the basics": "Programming fundamentals and basic problem solving",
    "Learn Important Sorting Techniques": "Core sorting algorithms",
    "Solve Problems on Arrays [Easy -> Medium -> Hard]": "Array problems from easy to hard",
    "Binary Search [1D, 2D Arrays, Search Space]": "Binary search and search space problems",
    "Strings [Basic and Medium]": "Basic and medium string problems",
    "Learn LinkedList [Single LL, Double LL, Medium, Hard Problems]": "Linked list problems",
    "Recursion [PatternWise]": "Recursion and backtracking patterns",
    "Bit Manipulation [Concepts & Problems]": "Bit manipulation problems",
    "Stack and Queues [Learning, Pre-In-Post-fix, Monotonic Stack, Implementation]": "Stack and queue problems",
    "Sliding Window & Two Pointer Combined Problems": "Sliding window and two-pointer problems",
    "Heaps [Learning, Medium, Hard Problems]": "Heap and priority queue problems",
    "Greedy Algorithms [Easy, Medium/Hard]": "Greedy algorithm problems",
    "Binary Trees [Traversals, Medium and Hard Problems]": "Binary tree problems",
    "Binary Search Trees [Concept and Problems]": "Binary search tree problems",
    "Graphs [Concepts & Problems]": "Graph algorithm problems",
    "Dynamic Programming [Patterns and Problems]": "Dynamic programming problems",
    "Tries": "Trie data structure problems",
    "Strings": "Advanced string algorithm problems",
}

STEM_SUFFIXES = ("ing", "tion", "sion", "ment", "ness", "ies", "ied", "ed", "es", "s")


def norm(text: str) -> str:
    cleaned = re.sub(r"[^a-z0-9 ]", " ", text.lower())
    return re.sub(r"\s+", " ", cleaned).strip()


def slugify(title: str) -> str:
    slug = re.sub(r"[^a-z0-9]+", "-", title.lower())
    return re.sub(r"-+", "-", slug).strip("-")[:80] or "problem"


def clean(url: Optional[str]) -> Optional[str]:
    if not url:
        return None
    return url.split("#")[0].split("?")[0].rstrip("/")


def platform_for(url: str) -> str:
    return "LEETCODE" if "leetcode.com" in url else "GEEKSFORGEEKS"


def stem(word: str) -> str:
    for suffix in STEM_SUFFIXES:
        if len(word) > len(suffix) + 3 and word.endswith(suffix):
            return word[: -len(suffix)]
    return word


def tokens(text: str) -> list[str]:
    return [stem(word) for word in norm(text).split() if word]


def score_match(name: str, key: str) -> int:
    name_norm = norm(name)
    key_norm = norm(key)
    if name_norm == key_norm:
        return 1000 + len(key_norm)
    if key_norm in name_norm and len(key_norm) >= 8:
        return 800 + len(key_norm)
    if name_norm in key_norm and len(name_norm) >= 8:
        return 700 + len(name_norm)

    name_tokens = set(tokens(name))
    key_tokens = set(tokens(key))
    if not name_tokens or not key_tokens:
        return 0

    overlap = name_tokens & key_tokens
    if not overlap:
        return 0
    if key_tokens <= name_tokens and len(name_tokens) - len(key_tokens) <= 3:
        return 500 + len(overlap) * 10 + len(key_norm)
    if name_tokens <= key_tokens and len(key_tokens) - len(name_tokens) <= 3:
        return 400 + len(overlap) * 10 + len(key_norm)

    jaccard = len(overlap) / len(name_tokens | key_tokens)
    if jaccard >= 0.65 and len(overlap) >= 2:
        return int(300 + jaccard * 100 + len(key_norm))
    return 0


def find_gfg_link(name: str, link_map: dict[str, str]) -> Optional[str]:
    best_score = 0
    best_url = None
    for key, url in link_map.items():
        score = score_match(name, key)
        if score > best_score:
            best_score = score
            best_url = url
    return clean(best_url) if best_score > 0 else None


def resolve_link(
    problem: dict,
    link_map: dict[str, str],
    overrides: dict[str, str],
) -> tuple[Optional[str], Optional[str]]:
    title = problem["problem_name"]

    override = clean(overrides.get(title))
    if override and ("leetcode.com" in override or "geeksforgeeks.org" in override):
        return override, platform_for(override)

    leetcode = clean(problem.get("leetcode"))
    if leetcode and "leetcode.com" in leetcode:
        return leetcode, platform_for(leetcode)

    gfg = find_gfg_link(title, link_map)
    if gfg and ("leetcode.com" in gfg or "geeksforgeeks.org" in gfg):
        return gfg, platform_for(gfg)

    return None, None


def main() -> None:
    official = json.loads(OFFICIAL.read_text())
    link_map = json.loads(LINK_CACHE.read_text())
    overrides = json.loads(OVERRIDES.read_text()) if OVERRIDES.exists() else {}

    topics_out = []
    total = 0
    with_links = 0

    for section in official["sections"]:
        problems_out = []
        for subcategory in section["subcategories"]:
            for problem in subcategory["problems"]:
                url, platform = resolve_link(problem, link_map, overrides)
                if not url:
                    continue

                with_links += 1
                diff = (problem.get("difficulty") or "Medium").upper()
                if diff not in ("EASY", "MEDIUM", "HARD"):
                    diff = "MEDIUM"

                problems_out.append({
                    "title": problem["problem_name"],
                    "slug": slugify(problem["problem_name"]),
                    "difficulty": diff,
                    "keyPatterns": "",
                    "url": url,
                    "platform": platform,
                })

        if not problems_out:
            continue

        total += len(problems_out)
        topics_out.append({
            "name": section["category_name"],
            "description": TOPIC_DESC.get(section["category_name"], section["category_name"]),
            "keyPatterns": "",
            "problems": problems_out,
        })

    sheet = {
        "version": "A2Z_SHEET_V5",
        "category": "A2Z DSA Sheet",
        "topics": topics_out,
    }
    OUT.write_text(json.dumps(sheet, indent=2))
    print(f"Wrote {total} linked problems in {len(topics_out)} topics -> {OUT}")


if __name__ == "__main__":
    main()
