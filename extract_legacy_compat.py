#!/usr/bin/env python3
"""
Temporary extraction script for MATLAB compatibility (no zero-padding)
For baseline establishment only - uses original naming convention
"""
import cv2
import os
import time

def extract(fname):
    if not os.path.exists(fname):
        os.mkdir(fname)

    vidcap = cv2.VideoCapture('./' + fname + '.mp4')
    success, image = vidcap.read()
    count = 0

    while success:
        cv2.imwrite("./" + fname + "/frame%d.jpg" % count, image)
        success, image = vidcap.read()
        print(fname, count)
        count += 1

    vidcap.release()

t = time.time()
extract('video')
print(f"Extraction time: {time.time()-t:.2f}s")
