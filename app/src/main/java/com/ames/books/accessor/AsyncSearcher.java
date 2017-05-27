package com.ames.books.accessor;

import android.os.AsyncTask;
import android.util.Log;

import com.ames.books.BuildConfig;
import com.ames.books.data.SearchBlock;
import com.ames.books.data.SearchResultListener;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.services.books.Books;
import com.google.api.services.books.BooksRequestInitializer;

import java.io.IOException;

/**
 * The search module that takes the search query. It takes the search query and properly notifies the BookListActivity about the query results.
 */
public class AsyncSearcher {
  // The key is not fixed to the app signature. It is the official API key for this APP. Still maybe better to provide.
  public static String API_KEY = "AIzaSyCjWRXuTr0xFXu1j9Qf3HOWSL-vIemEJE4";

  private static final String TAG = "books.search.list";
  public static final int ITEMS_PER_REQUEST = 10; // Specs says 40 max
  final SearchResultListener listener;

  public AsyncSearcher(SearchResultListener bookList) {
    this.listener = bookList;
  }

  public void doSearch(final String query, final int offset) {
    new AsyncTask<String, Void, SearchBlock>() {

      @Override
      protected SearchBlock doInBackground(String... params) {
        return search(query, offset);
      }

      @Override
      protected void onPostExecute(SearchBlock books) {
        listener.onQueryResult(books, query);
      }
    }.execute(query);
  }

  /**
   * Main search method, runs outside UI thread.
   */
  private SearchBlock search(String query, int offset) {
    Books books = new Books.Builder(AndroidHttp.newCompatibleTransport(), AndroidJsonFactory.getDefaultInstance(), null)
       .setApplicationName(BuildConfig.APPLICATION_ID)
       .setGoogleClientRequestInitializer(new BooksRequestInitializer(API_KEY))
       .build();

    try {
      // Executes the query
      Books.Volumes.List list = books.volumes().list(query);
      list.setMaxResults(Long.valueOf(ITEMS_PER_REQUEST));
      list.setStartIndex(Long.valueOf(offset));
      list.setFields("totalItems,items(volumeInfo(title,authors,pageCount,imageLinks/smallThumbnail),selfLink,id)");
      return new SearchBlock(list.execute(), offset);
    } catch (IOException e) {
      Log.e(TAG, "IO ex", e);
      return null;
    }
  }

  public int getItemsPerRequest() {
    return ITEMS_PER_REQUEST;
  }
}