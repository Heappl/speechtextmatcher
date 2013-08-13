#!/usr/bin/python

import sys, re
from optparse import OptionParser
import itertools, sets 

parser = OptionParser()
(_, args) = parser.parse_args()


if (len(args) < 2):
    print("at least two label files are required")
    sys.exit(1)


def prepare_file(path):
    lines = file(path).read().decode("utf8").lower().split("\n")[:-1]

    tokenized = [line.split(" ") for line in lines]
    converted = [(float(split[0]), float(split[1]), " ".join(split[2:])) for split in tokenized]
    return [(start, end, re.split("[ .]", label)) for (start, end, label) in converted]
    
first = prepare_file(args[0])
second = prepare_file(args[1])


def merge_to_look_similar_by_times(merging, target):
    pass

def mergeLabels(labels):
    if (not labels):
        return (0, 0, "")
    return reduce(
        lambda (accStart, accEnd, accLabel), (start, end, label) :
            (min(accStart, start), max(accEnd, end), accLabel + label),
            labels)


def merge_by_words(merging, target):
    ret = []
    for (start, end, label) in target:
        reducing = label
        toMerge = []
        while (reducing and merging):
            (currStart, currEnd, currLabel) = merging[0]
            #print ("t: ", (reducing[:len(currLabel)], currLabel), (reducing[:len(currLabel)] == currLabel))
            if (reducing[:len(currLabel)] != currLabel):
                break
            toMerge.append((currStart, currEnd, currLabel))
            reducing=reducing[len(currLabel):]
            merging = merging[1:]
        #print(label, toMerge)
        if (toMerge):
            ret.append(mergeLabels(toMerge))
        else:
            break
    return ret
    
def merge_by_times(merging, target):
    def timeFilter((start, end, l), (tstart, tend, tl)):
        return (start >= tstart - 0.3) and (end <= tend + 0.3)
    def extractFromTime(targetLabel, lookupList):
        return mergeLabels([label for label in lookupList if timeFilter(label, targetLabel)])
    return [extractFromTime(label, merging) for label in target]
    

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
     
def countWordDifference(first, second):
    def wordDiff(first, second):
        matched = 0
        for i in range(0, len(first) + 1):
            for j in range(0, len(second) + 1):
                for k in range(i + 1, len(first) + 1):
                    if (first[i:k] == second[j:(j + k - i)]):
                        matched = max(matched, k - i)
        if (len(args) > 3) and (matched == 0):
            print (first, second)
        return len(first) + len(second) - 2 * matched

    diffs = [wordDiff(label1, label2) for ((s1, e1, label1), (s2, e2, label2)) in zip(first, second)]
    ret = {}
    for diff in diffs:
        if (ret.has_key(diff)):
            ret[diff] += 1
        else:
            ret[diff] = 1
    return ret

firstMergedByWords = merge_by_words(first, second)       
firstMergedByTimes = merge_by_times(first, second)       
    
print countTimeDifference(firstMergedByWords, second, 0.3)
print countWordDifference(firstMergedByTimes, second)

#pause base alignment - simple
#(210, 715, 3.409846153846155, 3.516975611888207, 32.3939375000009)
#{0: 219, 1: 158, 2: 58, 3: 43, 4: 36, 5: 19, 6: 18, 7: 17, 8: 20, 9: 13, 10: 8, 11: 4, 12: 5, 13: 10, 14: 8, 15: 5, 16: 6, 17: 5, 18: 6, 19: 2, 20: 2, 21: 2, 22: 5, 23: 5, 24: 3, 25: 1, 26: 3, 27: 3, 28: 2, 29: 5, 30: 3, 31: 3, 32: 1, 34: 1, 35: 1, 38: 1, 41: 2, 42: 1, 44: 1, 47: 2, 51: 1, 52: 1, 53: 1, 54: 1, 62: 1, 70: 1, 71: 1, 94: 1}
#incremental
#(207, 708, 4.0902118644067835, 4.2215441384181975, 32.3939375000009)
#{0: 214, 1: 159, 2: 53, 3: 30, 4: 31, 5: 19, 6: 15, 7: 16, 8: 15, 9: 12, 10: 10, 11: 7, 12: 6, 13: 14, 14: 11, 15: 9, 16: 5, 17: 4, 18: 7, 19: 3, 20: 3, 21: 1, 22: 6, 23: 4, 24: 4, 25: 1, 26: 6, 27: 4, 28: 2, 29: 4, 30: 3, 31: 1, 32: 3, 33: 2, 34: 1, 35: 2, 37: 1, 38: 1, 39: 1, 40: 2, 41: 1, 42: 1, 44: 1, 46: 2, 47: 2, 49: 1, 53: 1, 54: 1, 67: 1, 70: 1, 71: 2, 80: 1, 94: 1}


