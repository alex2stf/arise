import alsaaudio
import sys
am = alsaaudio.Mixer()
am.setvolume(sys.argv[1])
print(am.getvolume()[0])