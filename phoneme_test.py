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
    ("dzi", "d zz"),
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
    ("zi", "z")])

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
checked = replaceAndFilter({}, filteredOut, prepare_file(args[0]))
target = replaceAndFilter(conversions1, filteredOut, prepare_file(args[1]))

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
        elif isSkip(l1, l2, skip):
            target = target[1:]
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

skip = dict([('j', listToUtf(['e', 'a', 'e j', 'u', 'uu', 'ee', 'aa', 'oo', 'o', 'e ł', 'o ł'])),
             ('i', listToUtf(['e', 'a', 'e j', 'u', 'uu', 'ee', 'aa', 'oo', 'o', 'ee l', 'o ł', 'ay']))])

similar = [["a", "aa"],["d", "dd"],["e", "ee"],["f", "ff", "w", "v"],["g", "gg"],["h", "hh"],["i", "ii"],["k", "kk"],["l", "ll", "ł", "m"],
           ["m", "mm"],["n", "nn", "ń"],["ay", "oo"],["p", "pp"],["r", "rr"],["t", "tt", "d", "dd"],["u", "uu"],["z", "zz", "s"],["ż", "sz", "zh", "sh"],
           ["zz", "ź", "sh", "z"], ["z", "zh"],["n", "ł", "l"], ["k", "g"], ["i", "j", "ii"], ["u", "ł", "l"], ["ś", "ź"], ["s", "ś", "sh"], ["p", "b"]]
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

print countTimeDifference(matchPhonemes(checked, target, similar, skip), 0.1)

#russian word alignment, russian phoneme alignment
#start and end
#0.05 (1446, 3570, 0.5949579831932773, 0.05707844353356384, 0.05740460832720529, 0.515500000000003)
#0.1 (2494, 3570, 0.3014005602240896, 0.05707844353356384, 0.05740460832720529, 0.515500000000003)

#start only
#0.05 (2235, 3570, 0.3739495798319328, 0.05707844353356384, 0.05740460832720529, 0.515500000000003)
#0.1 (2919, 3570, 0.18235294117647058, 0.05707844353356384, 0.05740460832720529, 0.515500000000003)

#end only
#0.05 (2281, 3570, 0.3610644257703081, 0.05707844353356384, 0.05740460832720529, 0.515500000000003)
#0.1 (2931, 3570, 0.17899159663865546, 0.05707844353356384, 0.05740460832720529, 0.515500000000003)

#russian word alignment, training

#start and end
#0.02 (1248, 3611, 0.6543893658266409, 0.02402445656885345, 0.024390807275252006, 0.5131249999999454)
#0.03 (2309, 3611, 0.36056494045970644, 0.02402445656885345, 0.024390807275252006, 0.5131249999999454)
#0.04 (2792, 3611, 0.22680697867626695, 0.02402445656885345, 0.024390807275252006, 0.5131249999999454)
#0.05 (3002, 3611, 0.16865134311824978, 0.02402445656885345, 0.024390807275252006, 0.5131249999999454)
#0.06 (3136, 3611, 0.13154250900027692, 0.02402445656885345, 0.024390807275252006, 0.5131249999999454)
#0.07 (3230, 3611, 0.10551093879811686, 0.02402445656885345, 0.024390807275252006, 0.5131249999999454)
#0.08 (3298, 3611, 0.08667959014123512, 0.02402445656885345, 0.024390807275252006, 0.5131249999999454)
#0.09 (3364, 3611, 0.068402104680144, 0.02402445656885345, 0.024390807275252006, 0.5131249999999454)
#0.10 (3411, 3611, 0.055386319579063974, 0.02402445656885345, 0.024390807275252006, 0.5131249999999454)

#start only
#0.02 (2202, 3611, 0.3901966214345057, 0.02402445656885345, 0.024390807275252006, 0.5131249999999454)
#0.03 (2885, 3611, 0.2010523400720022, 0.02402445656885345, 0.024390807275252006, 0.5131249999999454)
#0.04 (3136, 3611, 0.13154250900027692, 0.02402445656885345, 0.024390807275252006, 0.5131249999999454)
#0.05 (3258, 3611, 0.09775685405704791, 0.02402445656885345, 0.024390807275252006, 0.5131249999999454)
#0.06 (3338, 3611, 0.07560232622542232, 0.02402445656885345, 0.024390807275252006, 0.5131249999999454)
#0.07 (3398, 3611, 0.05898643035170313, 0.02402445656885345, 0.024390807275252006, 0.5131249999999454)
#0.08 (3439, 3611, 0.047632234837995016, 0.02402445656885345, 0.024390807275252006, 0.5131249999999454)
#0.09 (3472, 3611, 0.03849349210744946, 0.02402445656885345, 0.024390807275252006, 0.5131249999999454)
#0.10 (3500, 3611, 0.030739407366380506, 0.02402445656885345, 0.024390807275252006, 0.5131249999999454)

#end only
#0.02 (2033, 3611, 0.43699806147881476, 0.02402445656885345, 0.024390807275252006, 0.5131249999999454)
#0.03 (2862, 3611, 0.20742176682359456, 0.02402445656885345, 0.024390807275252006, 0.5131249999999454)
#0.04 (3186, 3611, 0.11769592910551094, 0.02402445656885345, 0.024390807275252006, 0.5131249999999454)
#0.05 (3295, 3611, 0.08751038493492107, 0.02402445656885345, 0.024390807275252006, 0.5131249999999454)
#0.06 (3359, 3611, 0.0697867626696206, 0.02402445656885345, 0.024390807275252006, 0.5131249999999454)
#0.07 (3404, 3611, 0.05732484076433121, 0.02402445656885345, 0.024390807275252006, 0.5131249999999454)
#0.08 (3434, 3611, 0.04901689282747161, 0.02402445656885345, 0.024390807275252006, 0.5131249999999454)
#0.09 (3470, 3611, 0.0390473553032401, 0.02402445656885345, 0.024390807275252006, 0.5131249999999454)
#0.10 (3492, 3611, 0.03295486014954306, 0.02402445656885345, 0.024390807275252006, 0.5131249999999454)




