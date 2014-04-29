#!/usr/bin/python

"""
Compact the data files in a directory, replacing identical files with symlinks.
This should be run as a daily cron job after a day is finished. 
"""

import os
import os.path
import filecmp


def areDuplicates(f1, f2):
	st1 = os.stat(f1)
	st2 = os.stat(f2)
	# if they're the same inode, they're not duplicates
	if st1.st_ino == st2.st_ino:
		return False
	return filecmp.cmp(f1, f2, shallow=False)




def _compact(path):
	for (dirpath, dirnames, filenames) in os.walk(path):
		if filenames:
			# iterate through the filenames in this directory
			os.chdir(dirpath)
			prevfile = filenames.pop(0)
			while filenames:
				nextfile = filenames.pop(0)
				if areDuplicates(prevfile, nextfile):
					print "%s: linking %s to %s"%(dirpath, prevfile, nextfile)
					#os.unlink(nextfile)
					#os.link(prevfile, nextfile)
				else:
					prevfile = nextfile


def compact(path):
	filemap = {}  # filepath -> [ fullpath, ... ], sorted by time
	for time in os.listdir(path):
		tpath = os.path.join(path,time)
		for (dirpath, dirnames, filenames) in os.walk(tpath):
			if filenames:
				for fname in filenames:
					fnpath = os.path.join(dirpath, fname)
					spath = fnpath[len(tpath)+1:]
					#print "%s -> %s (%s)"%(spath, fnpath, time)
					filemap.setdefault(spath,[]).append(fnpath)

	for (fpath, insts) in filemap.iteritems():
		#print "Compacting "+fpath
		prevfile = insts.pop(0)
		while insts:
			nextfile = insts.pop(0)
			if areDuplicates(prevfile, nextfile):
				#print "- linking %s to %s"%(prevfile, nextfile)
				os.unlink(nextfile)
				os.link(prevfile, nextfile)
			else:
				#print ". %s and %s differ"%(prevfile, nextfile)
				prevfile = nextfile






if __name__ == "__main__":
	import sys
	for path in sys.argv[1:]:
		compact(path)