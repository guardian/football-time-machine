Football Time Machine
=====================

The Football Time Machine allows you to simulate time travel by
replaying old football API responses.

## Usage

You can use the Football Time Machine by changing the URL you use to
access the PA API.  Instead of (e.g.) `http://pads6.pa-sport.com` you
should point to the Football Time Machine, including the date and time
you wish to travel to. For example if you set
`http://<host>/20140225/1550` you will receive data as if it is
15:50 on the 25th of Feb 2014.

You'll need to choose a time and date for which there exists saved
data. You can browse to the root of the time machine to see a list of
available dates and times, or check the data dir in the project.

*Note*, historical data may not work for you if the API calls you are
making weren't recorded at the time.

If you want to record data for future time travel adventures then set
you should set the PA API URL to `http://<host>/record`. This
will transparently save PA API responses into your data directory while
returning the full response to your application.

For more details/instructions, browse to the root of the time machine
after starting it using sbt.
