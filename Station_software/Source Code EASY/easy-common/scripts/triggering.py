from obspy.taup import TauPyModel
from obspy.geodetics import kilometers2degrees
import sys
import os
from obspy.core import read
from obspy.signal.trigger import plot_trigger
from obspy.signal.trigger import classic_sta_lta



if  len(sys.argv) != 2 :
    print ('Proper usage: python %s <recording>') % (sys.argv[0])
    sys.exit()
trace = read(sys.argv[1])[0]
df = trace.stats.sampling_rate
cft = classic_sta_lta(trace.data, int(1 * df), int(60 * df))
plot_trigger(trace, cft, 4, 2)
