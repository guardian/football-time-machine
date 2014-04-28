#!/usr/bin/python

"""
Force the PA football proxy to refetch a snapshot of today's matches.
"""
import os
import time
import urllib
import xml.etree.ElementTree as ET

PROXY_URL="http://54.72.218.177:9000/record"
PA_API_KEY=os.environ.get('PA_API_KEY')

matches = set()

# fetch the fixtures/match day URLs

matchdayurl = '%s/api/football/competitions/matchDay/%s/%s'%(PROXY_URL,PA_API_KEY,time.strftime("%Y%m%d"))
tree = ET.parse(urllib.urlopen(matchdayurl))
for elem in tree.iter('match'):
	matches.add(elem.get('matchID'))

for fixture in ['100', '500']:
	url = '%s/api/football/competition/fixtures/%s/%s'%(PROXY_URL, PA_API_KEY, fixture)
	tree = ET.parse(urllib.urlopen(url))
	for elem in tree.iter('fixture'):
		matches.add(elem.get('matchID'))

print "matches = %r"%matches
for match in matches:
	for urltype in ['events', 'lineups']:
		matchurl = '%s/api/football/match/%s/%s/%s'%(PROXY_URL,urltype,PA_API_KEY,match)
		urllib.urlopen(matchurl).read()

