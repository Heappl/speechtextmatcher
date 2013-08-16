#!/usr/bin/python
# coding=utf8

import sys, re
from optparse import OptionParser
import itertools, sets 

def prepare_file(path):
    lines = file(path).read().decode("utf8").lower().split("\n")[:-1]

    tokenized = [line.split(" ") for line in lines]
    converted = [(float(split[0]), float(split[1]), " ".join(split[2:])) for split in tokenized]
    return [(start, end, label) for (start, end, label) in converted]


parser = OptionParser()
(_, args) = parser.parse_args()

if (len(args) < 2):
    print("at least two label files are required: [checked] [target]")
    sys.exit(1)

conversions1 = dict([
    ("?", "?"),
    ("a", "aa"),
    ("a_", "oo l"),
    ("b", "b"),
    ("c", "c"),
    ("ci", "ch"),
    ("cz", "t sh"),
    ("d", "d"),
    ("drz", "d zh"),
    ("dz", "d z"),
    ("dzi", "d zz ii"),
    ("e", "e"),
    ("e_", "ee l"),
    ("f", "f"),
    ("g", "g"),
    ("h", "h"),
    ("i", "i"),
    ("j", "j"),
    ("k", "k"),
    ("l", "ll"),
    ("l_", "l"),
    ("m", "m"),
    ("n", "n"),
    ("ni", "nn"),
    ("o", "oo"),
    ("o", "ay"),
    ("p", "p"),
    ("r", "r"),
    ("rz", "zh"),
    ("s", "s"),
    ("si", "sh"),
    ("sz", "sh"),
    ("t", "t"),
    ("u", "uu"),
    ("w", "v"),
    ("y", "y"),
    ("z", "z"),
    ("zi", "zh")])

conversions2 = dict([
    ("a", "a"),
    ("a_", "o ł"),
    ("b", "b"),
    ("c", "c"),
    ("ci", "ć"),
    ("cz", "t sz"),
    ("d", "d"),
    ("drz", "d ż"),
    ("dz", "d z"),
    ("dzi", "d ź"),
    ("e", "e"),
    ("e_", "e ł"),
    ("f", "f"),
    ("g", "g"),
    ("h", "h"),
    ("i", "i"),
    ("j", "j"),
    ("k", "k"),
    ("l", "l"),
    ("l_", "ł"),
    ("m", "m"),
    ("n", "n"),
    ("ni", "ń"),
    ("o", "o"),
    ("p", "p"),
    ("r", "r"),
    ("rz", "ż"),
    ("s", "s"),
    ("si", "ś"),
    ("sz", "sz"),
    ("t", "t"),
    ("u", "u"),
    ("w", "w"),
    ("y", "y"),
    ("z", "z"),
    ("zi", "ź")])

def replaceAndFilter(conversions, filteredOut, labels):
    def convert(label):
        if conversions.has_key(label):
            return unicode(conversions[label], "utf-8")
        return label
    return [(start, end, convert(label)) for (start, end, label) in labels if label not in filteredOut]

filteredOut = ['sil', 'sp', 'SIL']
checked = replaceAndFilter(conversions2, filteredOut, prepare_file(args[0]))
target = replaceAndFilter(conversions2, filteredOut, prepare_file(args[1]))

def areSimilar(l1, l2, similar):
    for l in similar:
        if (l1 in l) and (l2 in l):
            return True
    return False

def isSkip(p1, p2, skip):
    return skip.has_key(p2) and p1 in skip[p2]

def isMerged(target, checked):
    phonemes = target.split(" ")
    return phonemes == [l for (s, e, l) in checked[:len(phonemes)]]

def mergeLabels(labels):
    if (not labels):
        return (0, 0, "")
    return reduce(
        lambda (accStart, accEnd, accLabel), (start, end, label) :
            (min(accStart, start), max(accEnd, end), accLabel + label),
            labels)

def getMerged(target, checked):
    phonemes = target.split(" ")
    return (checked[len(phonemes):], mergeLabels(checked[:len(phonemes)]))

def matchPhonemes(checked, target, similar, skip):
    ret = []
    while checked and target:
        (s1, e1, l1) = checked[0]
        (s2, e2, l2) = target[0]
        if (l1 == l2) or areSimilar(l1, l2, similar):
            ret.append(((s1, e1, l1), (s2, e2, l2)))
            target = target[1:]
            checked = checked[1:]
        elif isSkip(l2, l1, skip):
            checked = checked[1:]
        elif isMerged(l2, checked):
            (checked, newLabel) = getMerged(l2, checked)
            ret.append((newLabel, target[0]))
            target = target[1:]
        elif isMerged(l1, target):
            (target, newLabel) = getMerged(l1, target)
            ret.append((checked[0], newLabel))
            checked = checked[1:]
        else:
            print "error: ", s1, l1, s2, l2
            sys.exit(1)
    return ret

def listToUtf(l):
    return [unicode(s, "utf-8") for s in l]

skip = dict([('j', listToUtf(['e', 'a', 'e j', 'u', 'o', 'e ł', 'o ł']))])

similar = [["a", "aa"],["d", "dd"],["e", "ee"],["f", "ff", "w"],["g", "gg"],["h", "hh"],["i", "ii"],["k", "kk"],["l", "ll", "ł", "m"],
           ["m", "mm"],["n", "nn", "ń"],["ay", "oo"],["p", "pp"],["r", "rr"],["t", "tt", "d", "dd"],["u", "uu"],["z", "zz", "s"],["ż", "sz"],
           ["zz", "ź", "sh"],["n", "ł"], ["k", "g"], ["i", "j"], ["u", "ł"], ["ś", "ź"], ["s", "ś"], ["p", "b"]]
similar = [listToUtf(elem) for elem in similar]



def countTimeDifference(zipped, diff):
    total = 0
    aveStartDiff = 0.0
    aveEndDiff = 0.0
    maxDiff = 0.0
    for ((start1, end1, label1), (start2, end2, label2)) in zipped:
        startDiff = abs(start2 - start1)
        endDiff = abs(end2 - end1)
        aveStartDiff += startDiff
        aveEndDiff += endDiff
        maxDiff = max(maxDiff, startDiff, endDiff)
        if (startDiff > diff) or (endDiff > diff):
            total += 1
    return (len(zipped) - total, len(zipped), float(total) / float(len(zipped)), aveStartDiff / len(zipped), aveEndDiff / len(zipped), maxDiff)

print countTimeDifference(matchPhonemes(checked, target, similar, skip), 0.2)


#0.03: (2309, 3611, 0.36056494045970644, 0.024068211761320915, 0.024422377477412025, 0.5131249999999454)
#0.05: (3002, 3611, 0.16865134311824978, 0.024068211761320915, 0.024422377477412025, 0.5131249999999454)
#0.10: (3411, 3611, 0.055386319579063974, 0.024068211761320915, 0.024422377477412025, 0.5131249999999454)
#0.20: (3572, 3611, 0.010800332317917475, 0.024068211761320915, 0.024422377477412025, 0.5131249999999454)


