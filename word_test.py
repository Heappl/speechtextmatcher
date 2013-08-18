#!/usr/bin/python
# coding=utf8

import sys, re
from optparse import OptionParser
import itertools, sets 

def prepare_file(path):
    lines = file(path).read().decode("utf8").lower().split("\n")[:-1]

    tokenized = [re.split("\\s+", line) for line in lines]

    converted = [(float(split[0].replace(",", ".")), float(split[1].replace(",", ".")), " ".join(split[2:])) for split in tokenized]
    return [(start, end, label) for (start, end, label) in converted]


def createStats(checked, target, errorThresholds):
    if (len(checked) != len(target)):
        print "number of labels doesn't agree"
        sys.exit(1)
    maxDiff = 0
    maxShortDiff = 0
    aveDiff = 0
    aveStartDiff = 0
    aveEndDiff = 0
    startCounts = dict(zip(errorThresholds, [0] * len(errorThresholds)))
    endCounts = dict(zip(errorThresholds, [0] * len(errorThresholds)))
    bothCounts = dict(zip(errorThresholds, [0] * len(errorThresholds)))
    startIfShortCounts = dict(zip(errorThresholds, [0] * len(errorThresholds)))
    endIfShortCounts = dict(zip(errorThresholds, [0] * len(errorThresholds)))
    bothIfShortCounts = dict(zip(errorThresholds, [0] * len(errorThresholds)))
    for ((s1, e1, l1), (s2, e2, l2)) in zip(checked, target):
        startDiff = abs(s1 - s2)
        endDiff = abs(e1 - e2)
        maxDiff = max(maxDiff, endDiff, startDiff)
        if (s1 > s2):
            maxShortDiff = max(startDiff, maxShortDiff)
        if (e1 < e2):
            maxShortDiff = max(endDiff, maxShortDiff)
        aveDiff += endDiff + startDiff
        for threshold in errorThresholds:
            assignedToBothAlready = False
            if (startDiff > threshold):
                startCounts[threshold] += 1
                if (s1 > s2):
                    startIfShortCounts[threshold] += 1
                    bothIfShortCounts[threshold] += 1
                    assignedToBothAlready = True
            if (endDiff > threshold):
                endCounts[threshold] += 1
                if (e1 < e2):
                    endIfShortCounts[threshold] += 1
                    if (not assignedToBothAlready):
                        bothIfShortCounts[threshold] += 1
            if (startDiff > threshold) or (endDiff > threshold):
                bothCounts[threshold] += 1
    aveDiff /= 2 * len(checked)

    return (len(checked),
            maxDiff,
            maxShortDiff,
            aveDiff,
            startCounts,
            startIfShortCounts,
            endCounts,
            endIfShortCounts,
            bothCounts,
            bothIfShortCounts)

parser = OptionParser()
(_, args) = parser.parse_args()

if (len(args) < 2):
    print("at least two label files are required: [checked] [target]")
    sys.exit(1)


checked = prepare_file(args[0])
target = prepare_file(args[1])

print createStats(checked, target, [0.05, 0.1, 0.2, 0.3, 0.4, 0.5, 1])

# russian model word alignment
#(585, 1.3542329999999936, 0.5336800000000039, 0.03297232435897484, 

#startCounts:        {0.5: 9,    1: 2,   0.4: 10,        0.2: 19,        0.05: 66,       0.3: 15,        0.1: 34},
#startIfShortCounts: {0.5: 0,    1: 0,   0.4: 0,         0.2: 0,         0.05: 0,        0.3: 0,         0.1: 0}, 
#endCounts:          {0.5: 8,    1: 2,   0.4: 11,        0.2: 26,        0.05: 78,       0.3: 18,        0.1: 42}, 
#endIfShortCounts:   {0.5: 1,    1: 0,   0.4: 1,         0.2: 1,         0.05: 21,       0.3: 1,         0.1: 5}, 
#bothCounts:         {0.5: 16,   1: 4,   0.4: 20,        0.2: 44,        0.05: 132,      0.3: 32,        0.1: 73}, 
#bothIfShortCounts:  {0.5: 1,    1: 0,   0.4: 1,         0.2: 1,         0.05: 21,       0.3: 1,         0.1: 5})


