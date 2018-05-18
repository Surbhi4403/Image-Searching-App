package app.test.myassignment.ui_handling;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import app.test.myassignment.R;
import app.test.myassignment.ui_handling.fragment.GridFragment;

/**
 * Grid to pager app's main activity.
 */
public class MainActivity extends AppCompatActivity {

  /**
   * Holds the current image position to be shared between the grid and the pager fragments. This
   * position updated when a grid item is clicked, or when paging the pager.
   *
   */
  public static int currentPosition;
  private static final String KEY_CURRENT_POSITION = "app.test.myassignment.key.currentPosition";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    if (savedInstanceState != null) {
      currentPosition = savedInstanceState.getInt(KEY_CURRENT_POSITION, 0);
      // Return here to prevent adding additional GridFragments when changing orientation.
      return;
    }
    FragmentManager fragmentManager = getSupportFragmentManager();
    fragmentManager
        .beginTransaction()
        .add(R.id.fragment_container, new GridFragment(), GridFragment.class.getSimpleName())
        .commit();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putInt(KEY_CURRENT_POSITION, currentPosition);
  }
}
