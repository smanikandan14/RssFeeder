TechCrunchRssFeader
===================
An Android App to read rss feed from Techcrunch and update the list whenever new entries are added to the feed. 

UpdateInterval: 10 minutes 

Feed Url: http://feeds.feedburner.com/TechCrunch/social 

This app demonstrates 
- How UI is never blocked while fetching the data.
- If user closes the app before the feed is completely loaded (for the first time) and put it in the background. The app will continue to fetch the data in the background so that if the user reopens the app, the listview is updated without having to re-fetch the data.

- The use of AsyncTaskLoader which is used to offload the work of querying feeds from db onto a worker thread 
helps to keep the UI responsive.

- AsyncTaskLoader listens for new feeds available update using broadcastreceiver and loads the new data. 

- AlarmManager with alarm type ELAPSED_REALTIME is used to set a alarm for update interval of 10 minutes. AlarmManager is set with pending intent created to call IntentService.

- IntentService is used to connect to network and get the feeeds in background thread.

- Whenever new feed is obtained local broad cast message is sent for the loaders to update the new feeds.

##Design
![alt text](https://github.com/smanikandan14/TechCrunchRssFeader/blob/master/art/design.png "")

##Screenshot
![alt text](https://github.com/smanikandan14/TechCrunchRssFeader/blob/master/art/RssFeeder.png "")
