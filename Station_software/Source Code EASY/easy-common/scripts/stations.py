import matplotlib.pyplot as plt
import matplotlib.lines as mlines
from matplotlib.transforms import blended_transform_factory
from obspy import read, Stream
from obspy.geodetics import gps2dist_azimuth
from fnmatch import fnmatch
import os
import sys

black_line = mlines.Line2D([], [], color='black', label='Seismic trace data')
red_line = mlines.Line2D([], [], color='red',  label='Trigger data')

if  len(sys.argv) != 2 :
    print ('Proper usage: python %s <directory>') % (sys.argv[0])
    sys.exit()

thisdir = sys.argv[1]
st = Stream()

for r, d, f in os.walk(thisdir):
    for file in f:
        #discart any horizontal data
        if fnmatch(file, '*HZ*.mseed'):
            print(os.path.join(r, file))
            st += read(os.path.join(r, file))


# Set Earthquakes' epicenter
eq_lat = raw_input("Please enter Earthquake's Latitude:\t")
eq_lon = raw_input("Please enter Earthquake's Longitude:\t")

for tr in st:
    lat = raw_input("Please enter Station's Latitude:\t")
    lon = raw_input("Please enter Station's Longitude:\t")
    tr.stats.distance = gps2dist_azimuth(float(lat), float(lon), float(eq_lat), float(eq_lon))[0]

# Filter the data
st.filter('bandpass', freqmin=0.075, freqmax=9.9)

fig = plt.figure()
st.plot(type='section', plot_dx=20e3, recordlength=100,
        time_down=True, linewidth=.5, grid_linewidth=.25, show=False, fig=fig)
st.trigger('recstalta', sta=(1.5), lta=(60))  
st.plot(type='section', color='red', plot_dx=20e3, recordlength=100,
        time_down=True, linewidth=.5, grid_linewidth=.25, show=False, fig=fig)

# Plot customization: Add station labels to offset axis
ax = fig.axes[0]
transform = blended_transform_factory(ax.transData, ax.transAxes)
for tr in st:
    ax.text(tr.stats.distance / 1e3, 1.0, tr.stats.station, rotation=270,
            va="bottom", ha="center", transform=transform, zorder=10)

fig.canvas.set_window_title('Seismic traces')
plt.legend(handles=[red_line, black_line],loc='upper left')
plt.show()
