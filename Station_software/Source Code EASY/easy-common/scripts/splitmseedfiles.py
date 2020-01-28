from obspy import read
import os
import sys
import ntpath
ntpath.basename("a/b/c")

if  len(sys.argv) != 2 :
    print ('Proper usage: python %s <Path to file>') % (sys.argv[0])
    sys.exit()

st = read(sys.argv[1])
head, tail = ntpath.split(sys.argv[1])
for tr in st:
	tr.write(head+tr.id + "_" + str(int(tr.stats.sampling_rate)) + "sps"".mseed", format="MSEED")


