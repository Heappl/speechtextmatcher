#!/usr/bin/python

import sys, re
from optparse import OptionParser
import itertools, sets 

def prepare_file(path):
    lines = file(path).read().decode("utf8").lower().split("\n")[:-1]

    tokenized = [line.split(" ") for line in lines]
    converted = [(float(split[0]), float(split[1]), " ".join(split[2:])) for split in tokenized]
    return [(start, end, label) for (start, end, label) in converted]

def countTimeDifference(first, second, diff):
    if (len(first) != len(second)):
        print "error: first and second length don't agree"
        sys.exit(1)

    total = 0
    aveStartDiff = 0.0
    aveEndDiff = 0.0
    maxDiff = 0.0
    for ((start1, end1, label1), (start2, end2, label2)) in zip(first, second):
        if ("".join(label1) != "".join(label2)):
            print "error: zipped labels don't agree ", label1, label2
            sys.exit(1)
        startDiff = abs(start2 - start1)
        endDiff = abs(end2 - end1)
        aveStartDiff += startDiff
        aveEndDiff += endDiff
        maxDiff = max(maxDiff, startDiff, endDiff)
        if (startDiff > diff) or (endDiff > diff):
            total += 1
    return (len(second) - total, len(second), aveStartDiff / len(first), aveEndDiff / len(first), maxDiff)

parser = OptionParser()
(_, args) = parser.parse_args()

if (len(args) < 2):
    print("at least two label files are required: [checked] [target]")
    sys.exit(1)

checked = prepare_file(args[0])
target = [(start, end, label) for (start, end, label) in prepare_file(args[1]) if (label != 'sp') and (label != 'sil')]

print countTimeDifference(checked, target, 0.05)

