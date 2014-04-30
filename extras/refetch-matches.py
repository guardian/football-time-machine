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
VERBOSE=False

matches = set()

todaysDate = time.strftime("%Y%m%d")

# fetch the competitions

compsurl = '%s/api/football/competitions/competitions/%s'%(PROXY_URL,PA_API_KEY)
tree = ET.parse(urllib.urlopen(compsurl))
comps = set([elem.get("competitionID") for elem in tree.iter("season")])

def fetchUrl(url):
	global VERBOSE
	if VERBOSE:
		print "Fetching %s"%url
	return urllib.urlopen(url).read()

def fetchAndParseUrl(url):
	global VERBOSE
	if VERBOSE:
		print "Fetching %s"%url
	return ET.parse(urllib.urlopen(url))

# fetch the fixtures/match day URLs

matchdayurl = '%s/api/football/competitions/matchDay/%s/%s'%(PROXY_URL,PA_API_KEY,todaysDate)
tree = fetchAndParseUrl(matchdayurl)
for elem in tree.iter('match'):
	matches.add(elem.get('matchID'))

for fixture in ['100', '500']:
	url = '%s/api/football/competition/fixtures/%s/%s'%(PROXY_URL, PA_API_KEY, fixture)
	tree = fetchAndParseUrl(url)
	for elem in tree.iter('fixture'):
		matches.add(elem.get('matchID'))

# fetch results pages
for res in comps:
	date = time.strftime("%Y%m%d", time.localtime(time.time()-(86400*30)))
	url = '%s/api/football/competition/results/%s/%s/%s'%(PROXY_URL, PA_API_KEY, res, date)
	fetchUrl(url)

for league in comps:
	url = '%s/api/football/competition/leagueTable/%s/%s/%s'%(PROXY_URL, PA_API_KEY, league, todaysDate)
	fetchUrl(url)


for match in matches:
	for urltype in ['events', 'lineups']:
		matchurl = '%s/api/football/match/%s/%s/%s'%(PROXY_URL,urltype,PA_API_KEY,match)
		fetchUrl(matchurl)

