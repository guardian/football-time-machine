# football-time-machine

Stores pa feed on s3 to replay them later.

This app reads data from an s3 bucket `pa-football-time-machine` in the mobile account. 

When testing locally or in CODE, make sure you hit the `setDate` method first as this outputs data to s3 which the
other two endpoints run from. 

When testing the `getPaData` make sure the path is set to a file that exists in the `pa-football-time-machine` bucket

### Running locally
Make sure you get mobile credentials from Janus

Go to `src/main/scala/com/gu/footballtimemachine/ApiLambda.scala`

Uncomment the endpoint that you want to run

Run main

### Testing in CODE

The time machine is normally off in the CODE environment by default and the rule is only to be enabled from AWS for testing and then disabled once testing is done (The APIs below still respond when it is disabled). Get in touch with a recent contributor to the repository if you are unsure about how to enable the rule.

#### Football time machine api
There are 3 endpoints which are handled by API Gateway. 

1) setDate -> https://hdjq4n85yi.execute-api.eu-west-1.amazonaws.com/Prod/setDate?startDate=2017-06-11T21:00:00Z&speed=3
2) getPaData -> https://hdjq4n85yi.execute-api.eu-west-1.amazonaws.com/Prod/competitions/matchDay/apiKey/20240215
3) getTime -> https://hdjq4n85yi.execute-api.eu-west-1.amazonaws.com/Prod/getTime

#### Archive

This runs on a cron schedule. Search for the `football-time-machine-archive-CODE` lambda in the console and tail
the logs