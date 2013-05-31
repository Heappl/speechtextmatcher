#!/usr/bin/python

lines = [l.split(" ") for l in file("parts_labels.txt").read().split("\n")[:-1]]
outf = file("parts_labels.copy.txt", "w")
for (start, end, label) in lines:
    start = str(float(start) * 2.75625)
    end = str(float(end) * 2.75625)
    outf.write(start + " " + end + " " + label + "\n")

