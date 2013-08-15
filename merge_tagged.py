#!/usr/bin/python


import sys, re, os, itertools, sets, random, wave, contextlib
from optparse import OptionParser

parser = OptionParser()
(_, args) = parser.parse_args()


if len(args) < 2:
    print "usage: ./marge_tagged.py contents_file directory"
    system.exit(1)

def createPhonemesFilePath(path):
    pathElements = path.strip(os.sep).split(os.sep)
    speakerId = pathElements[-1]
    return os.sep.join(pathElements) + os.sep + speakerId + ".mlf"

def readPhonemes(path):
    elements = re.sub("^#.*\n", "", file(path).read()).split(".\n")
    def extractFromTag(tag):
        tokens = tag.split(" ")
        return (float(tokens[0]) / 10000 / 1000, float(tokens[1]) / 10000 / 1000, tokens[2])
    def extractFromElement(elem):
        lines = elem.split("\n")[:-1]
        return (lines[0][-8:-5], [extractFromTag(line) for line in lines[1:]])
    return dict([extractFromElement(elem) for elem in elements if elem != ''])

def readFiles(dirPath):
    files = [path for path in os.listdir(dirPath) if path[-3:] == "wav"]
    return dict([(f[-7:-4], dirPath + os.sep + f) for f in files])
 
def readContentFile(path):
    lines = file(path).read().split("\n")
    def extractFromLine(line):
        tokens = re.split(" +", line)
        return (tokens[0][-7:-4], " ".join(tokens[1:]))
    return dict([extractFromLine(line) for line in lines if len(line) > 0])


def getDuration(path):
    with contextlib.closing(wave.open(path,'r')) as f:
        frames = f.getnframes()
        rate = f.getframerate()
        duration = frames/float(rate)
        return duration

def calculateDurations(paths):
    return dict([(path, getDuration(paths[path])) for path in paths.keys()])

def createPhonemeLabelsFromPermutation(perm, labels, durations):
    startingTime = 0.0
    ret = []
    for elem in perm:
        ret += [str(start + startingTime) + " " + str(end + startingTime) + " " + label for (start, end, label) in labels[elem]]
        startingTime += durations[elem]
    return ret

def createTextFromPermutation(perm, contents):
    return " ".join([contents[elem] for elem in perm if contents[elem]])

def mergeAudioFileFromPermutation(perm, files, outputFile):
    tempFile = outputFile + ".temp"
    os.system("cp " + files[perm[0]] + " " + tempFile)
    for elem in perm[1:]:
        os.system("sox " + tempFile + " " + files[elem] + " " + outputFile)
        os.system("cp " + outputFile + " " + tempFile)

phonemes = readPhonemes(createPhonemesFilePath(args[1]))
files = readFiles(args[1])
contents = readContentFile(args[0])
durations = calculateDurations(files)

permutation = files.keys()
random.shuffle(permutation)

phonemeLabels = createPhonemeLabelsFromPermutation(permutation, phonemes, durations)
file("phonemes.labels.txt", "w").write("\n".join(phonemeLabels))

text = createTextFromPermutation(permutation, contents)
file("merged_text.txt", "w").write(text)
mergeAudioFileFromPermutation(permutation, files, "merged.wav")


