# Image Searching App

An Android view for displaying images using bing APIs, Offline image saving in SQLite DB, Pagination of records and Shared Element Transitions between grid and details view images. Scroll feature on details view as well to view back and next images.

## Search and View Images with Pagination
![video](Gifs/SearchItemsAndPagination.gif)

## Change Number of Columns
![video](Gifs/ChangeColumns.gif)

## Shared Element Transitions and Viewpager with Image scroll option
![video](Gifs/SharedElmentTransition.gif)

## Offline Images
![video](Gifs/OfflineData.gif)

## How it Works
* Create Bing Account and generate Image Search API Key. 
* Use below API:
https://api.cognitive.microsoft.com/bing/v7.0/images/search?mkt=en-in&safeSearch=Strict&q=searchTerm&offset=0&count=20
and pass header as Ocp-Apim-Subscription-Key:Your_API_Key
* Change API key in the file: CommonFunctions>BingKey and get results using your key.
* Enter search Term in the box and press of either side search button or keyboard's search button (Done button will be converted to search button as applied in xml).
* Once you will see search results, you will see options menu to switch between 3/4 columns from 2. Once you will change columns, options menu will change according to the selected value, i.e. If selected 3, options menu will show 2 and 4 in the list and vice versa.
* It will load 20 items for first time and once you'll scroll, it'll get more items and load them in the same list.
* Whatever you have seen in the app in online mode in searching (Images), you will be able to see all of them in offline mode as that data's saved in SQLite database of mobile app.
* Once you will click on any image, you will see shared element transition between grid and full screen image.
* The full screen image has view pager so you can swipe left and right from same screen to check images.

