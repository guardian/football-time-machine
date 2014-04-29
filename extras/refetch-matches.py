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

todaysDate = time.strftime("%Y%m%d")

# fetch the fixtures/match day URLs

matchdayurl = '%s/api/football/competitions/matchDay/%s/%s'%(PROXY_URL,PA_API_KEY,todaysDate)
tree = ET.parse(urllib.urlopen(matchdayurl))
for elem in tree.iter('match'):
	matches.add(elem.get('matchID'))

for fixture in ['100', '500']:
	url = '%s/api/football/competition/fixtures/%s/%s'%(PROXY_URL, PA_API_KEY, fixture)
	tree = ET.parse(urllib.urlopen(url))
	for elem in tree.iter('fixture'):
		matches.add(elem.get('matchID'))

lgs = [100,101,102,103,120,121,122,123,300,301,320,321,400,500,510,620,625,635,650,701,721]

# fetch results pages
for res in lgs:
	date = time.strftime("%Y%m%d", time.localtime(time.time()-(86400*30)))
	url = '%s/api/football/competition/results/%s/%d/%s'%(PROXY_URL, PA_API_KEY, res, date)
#	print "opening %s"%url
	urllib.urlopen(url).read()

for league in lgs:
	url = '%s/api/football/competition/leagueTable/%s/%d/%s'%(PROXY_URL, PA_API_KEY, league, todaysDate)
#	print "opening %s"%url
	urllib.urlopen(url).read()


#print "matches = %r"%matches
for match in matches:
	for urltype in ['events', 'lineups']:
		matchurl = '%s/api/football/match/%s/%s/%s'%(PROXY_URL,urltype,PA_API_KEY,match)
		urllib.urlopen(matchurl).read()

