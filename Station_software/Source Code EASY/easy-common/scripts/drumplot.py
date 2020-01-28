import matplotlib.pyplot as plt
from obspy import read
import sys
import os

if  len(sys.argv) != 2 :
    print ('Proper usage: python %s <Filepath>') % (sys.argv[0])
    sys.exit()
st = read(sys.argv[1])
st.filter("lowpass", freq=0.1, corners=2)
st.plot(type="dayplot", interval=60, right_vertical_labels=False,
        vertical_scaling_range=5e3, one_tick_per_line=True,
        color=['k', 'r', 'b', 'g'], show_y_UTC_label=False)
