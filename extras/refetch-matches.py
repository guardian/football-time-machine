#!/usr/bin/python

"""
Force the PA football proxy to refetch a snapshot of today's matches.
"""
import os
import urllib
import xml.etree.ElementTree as ET

PROXY_URL="http://54.72.218.177:9000/record"
PA_API_KEY=os.environ.get('PA_API_KEY')

# fetch the fixtures

for fixture in ['100', '500']:
	url = '%s/api/football/competition/fixtures/%s/%s'%(PROXY_URL, PA_API_KEY, fixture)
	tree = ET.parse(urllib.urlopen(url))
	matches = [c.get('matchID') for c in tree.iter('fixture')]
	for match in matches:
		for urltype in ['events', 'lineups']:
			matchurl = '%s/api/football/match/%s/%s/%s'%(PROXY_URL,urltype,PA_API_KEY,match)
			urllib.urlopen(matchurl).read()

