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
#endCounts:          {0.5: 8,    1: 2,   0.4: 11,        0.2: 26,        0.05: 78,       0.3: 18,        0.1: 42}, 
#bothCounts:         {0.5: 16,   1: 4,   0.4: 20,        0.2: 44,        0.05: 132,      0.3: 32,        0.1: 73}, 

# doktor piotr by length

#(585, 0.42244158977234747, 0.3714552725894009, 0.043984370916071075, 
#startCounts:        {0.5: 0,   1: 0,   0.4: 1,         0.2: 7,         0.05: 117,      0.3: 3,         0.1: 37}, 
#endCounts:          {0.5: 0,   1: 0,   0.4: 0,         0.2: 14,        0.05: 203,      0.3: 4,         0.1: 70}, 
#bothCounts:         {0.5: 0,   1 : 0,  0.4: 1,         0.2: 20,        0.05: 277,      0.3: 6,         0.1: 99}, 


#boze narodzenie audio model

#(1779, 2.4512300000000096, 0.5431210000000135, 0.015826948566615866,
#startCounts:        {0.5: 4,       1: 1,       0.4: 9,         0.2: 28,        0.05: 102,      0.3: 20,        0.1: 44},
#endCounts:          {0.5: 5,       1: 1,       0.4: 12,        0.2: 22,        0.05: 79,       0.3: 16,        0.1: 38}
#bothCounts:         {0.5: 9,       1: 2,       0.4: 19,        0.2: 48,        0.05: 168,      0.3: 34,        0.1: 79}


#boze narodzenie by length

#(1779, 0.6056822888515399, 0.6056822888515399, 0.046190836579278256

#startCounts:        {0.5: 0,       1: 0,       0.4: 2,         0.2: 8,         0.05: 255,      0.3: 5,         0.1: 60},
#endCounts:          {0.5: 5,       1: 0,       0.4: 9,         0.2: 71,        0.05: 798,      0.3: 28,        0.1: 216},
#bothCounts:         {0.5: 5,       1: 0,       0.4: 11,        0.2: 79,        0.05: 937,      0.3: 33,        0.1: 269},

