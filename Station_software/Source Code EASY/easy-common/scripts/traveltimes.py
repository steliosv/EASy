from obspy.taup import TauPyModel
from obspy.geodetics import kilometers2degrees
import sys
import os

model = TauPyModel(model="iasp91")


if  len(sys.argv) != 3 :
    print ('Proper usage: python %s <deapth> <dist in km>') % (sys.argv[0])
    sys.exit()

source_depth_in_km=float(sys.argv[1])
distance_in_degree=(kilometers2degrees(float(sys.argv[2])))

arrivals = model.get_travel_times(source_depth_in_km, distance_in_degree, phase_list=["p", "s"])
print(arrivals)  
print '\tts - tp: ',round(arrivals[1].time - arrivals[0].time, 3), 'seconds'
arrivals = model.get_ray_paths(source_depth_in_km,distance_in_degree, phase_list=["ttbasic"])
ax = arrivals.plot_rays()
