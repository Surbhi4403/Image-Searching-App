package app.test.myassignment.ui_handling.fragment;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.SharedElementCallback;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.transition.TransitionInflater;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import app.test.myassignment.CommonFunctions;
import app.test.myassignment.R;
import app.test.myassignment.api_handling.api_call.ImageApiInterface;
import app.test.myassignment.api_handling.api_call.ImageSearchAPI;
import app.test.myassignment.api_handling.pojo.ImageData;
import app.test.myassignment.api_handling.pojo.Value;
import app.test.myassignment.api_handling.retrofit.APIClient;
import app.test.myassignment.api_handling.retrofit.APIInterface;
import app.test.myassignment.db_handling.DatabaseHelper;
import app.test.myassignment.ui_handling.MainActivity;
import app.test.myassignment.ui_handling.adapter.GridAdapter;
import retrofit2.Call;

/**
 * A fragment for displaying a grid of images.
 */
public class GridFragment extends Fragment implements ImageApiInterface.onApiFinishedListener {
    private View view;
    private EditText edt_search_term;
    private RecyclerView recyclerView;
    private CircleProgressBar cir_loader;
    public Activity mContext;
    public LinearLayout loader;
    public APIInterface api;
    public Call<ImageData> call;

    private int numberOfColumns = 2;
    private int offset = 0;
    private int scrollPosition = -1;
    private int countOfRecords = 20;
    private int screenWidth;
    private String searchTerm = null;
    /* Taken to avoid API call again and again if already any call's going on*/
    private boolean apiCalling = false;
    /* Taken to avoid loading of API again and again on scroll of recycler view*/
    private boolean bottomFound = false;
    // To check the image option to show from
    private int viewFrom = CommonFunctions.Online;
    private int TypeEditText = 1;
    private int TypePagination = 2;

    private List imagesList;
    private ImageApiInterface imageSearchAPI;
    private DatabaseHelper databaseHelper;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        imagesList = new ArrayList();
        mContext = getActivity();
        databaseHelper = new DatabaseHelper(mContext);

        screenWidth = CommonFunctions.screenWidth(mContext);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_grid, container, false);
        initializeData();
        if (imagesList.size() > 0) {
            setDataInGrid();
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        scrollToPosition(MainActivity.currentPosition);
    }

    /* Initializing Views */
    void initializeData() {
        if (api == null)
            api = APIClient.getClient().create(APIInterface.class);

        imageSearchAPI = new ImageSearchAPI();
        loader = view.findViewById(R.id.loader);
        cir_loader = view.findViewById(R.id.cir_loader);
        edt_search_term = view.findViewById(R.id.edt_search_term);

        recyclerView = view.findViewById(R.id.recycler_view);
        implementingListener();
    }

    /* Listeners */
    void implementingListener() {
        //Search box imeOptions search click
        edt_search_term.setOnEditorActionListener((v, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_SEARCH)) {
                checkSearchValidationNCallAPI(TypeEditText);
            }
            return false;
        });

        //Recycler scroll view
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                GridLayoutManager gridLayoutManager = (GridLayoutManager) recyclerView.getLayoutManager();

                int onScreenVisibleItemCount = gridLayoutManager.getChildCount();
                int totalItemCount = gridLayoutManager.getItemCount();
                int fistVisibleCurrentItem = gridLayoutManager.findFirstVisibleItemPosition();

                scrollPosition = fistVisibleCurrentItem;

                if ((onScreenVisibleItemCount + fistVisibleCurrentItem) >= totalItemCount && !bottomFound && viewFrom == CommonFunctions.Online) {
                    //bottom of recycler view found

                    //Taken to avoid bottom again on scroll for same api
                    bottomFound = true;
                    //Position where we need to scroll list again after another API call
                    scrollPosition = totalItemCount;
                    //API calling after checking data
                    checkSearchValidationNCallAPI(TypePagination);
                }

            }
        });

        // Search box right search icon click
        edt_search_term.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (edt_search_term.getRight() - edt_search_term.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    checkSearchValidationNCallAPI(TypeEditText);
                    return true;
                }
            }
            return false;
        });
    }

    /* Search box validations */
    void checkSearchValidationNCallAPI(int typeOfSearch) {
        String searchTerm = edt_search_term.getText().toString().trim();
        if (!TextUtils.isEmpty(searchTerm)) {
            //If searching for same item then show error
            if (typeOfSearch == TypeEditText && searchTerm.equalsIgnoreCase(this.searchTerm)) {
                showSnackbar(getString(R.string.same_search));
                bottomFound = false;
                return;
            }
            // Get images list either from server or from DB
            GetImagesList(searchTerm);
        } else bottomFound = false;
    }

    /* Search box validations */
    void GetImagesList(String searchTerm) {
        //Hiding Keyboard
        CommonFunctions.HideKeyboard(edt_search_term);

        if (CommonFunctions.checkInternetConnection(mContext)) {
            if (imageSearchAPI != null && !apiCalling) {
                if (this.searchTerm != null && !this.searchTerm.equalsIgnoreCase(searchTerm))
                    offset = 0;

                showLoader();

                imageSearchAPI.imageSearchAPI(mContext, searchTerm, offset, countOfRecords, this, api, call);
            } else bottomFound = false;
        } else {
            bottomFound = false;
            showSnackbar(getString(R.string.no_internet));

            new LoadImageFromDatabaseTask().execute(searchTerm);
        }
    }

    /**
     * Scrolls the recycler view to show the last viewed item in the grid. This is important when
     * navigating back from the grid.
     */
    private void scrollToPosition(int position) {
        recyclerView.addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                recyclerView.removeOnLayoutChangeListener(this);
                final RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                View viewAtPosition = layoutManager.findViewByPosition(position);
                // Scroll to position if the view for the current position is null (not currently part of
                // layout manager children), or it's not completely visible.
                if (viewAtPosition == null || layoutManager
                        .isViewPartiallyVisible(viewAtPosition, false, true)) {
                    recyclerView.post(() -> layoutManager.scrollToPosition(position));

                }
            }
        });
    }

    /**
     * Prepares the shared element transition to the pager fragment, as well as the other transitions
     * that affect the flow.
     */
    private void prepareTransitions() {
        setExitTransition(TransitionInflater.from(getContext())
                .inflateTransition(R.transition.grid_exit_transition));

        // A similar mapping is set at the ImagePagerFragment with a setEnterSharedElementCallback.
        setExitSharedElementCallback(
                new SharedElementCallback() {
                    @Override
                    public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                        // Locate the ViewHolder for the clicked position.
                        RecyclerView.ViewHolder selectedViewHolder = recyclerView
                                .findViewHolderForAdapterPosition(MainActivity.currentPosition);
                        if (selectedViewHolder == null || selectedViewHolder.itemView == null) {
                            return;
                        }

                        // Map the first shared element name to the child ImageView.
                        sharedElements
                                .put(names.get(0), selectedViewHolder.itemView.findViewById(R.id.card_image));
                    }
                });
    }

    /* API success and data handling*/
    @Override
    public void onApiSuccess(ImageData response, String searchTermItem) {
        hideLoader();
        if (this.searchTerm != null && !this.searchTerm.equalsIgnoreCase(searchTermItem))
            imagesList.clear();

        if (response.getValue().size() > 0) {
            for (Value value : response.getValue()) {
                if (!databaseHelper.isRecordExist(value.getImageId())) {
                    value.setSearchTerm(searchTermItem);
                    new DownloadImage().execute(value);
                }
            }
            imagesList.addAll(response.getValue());
            viewFrom = CommonFunctions.Online;
            searchTerm = searchTermItem;
            setDataInGrid();
            offset = response.getNextOffset();
        }
        bottomFound = false;
    }

    /* API failure and data handling */
    @Override
    public void onApiFailure(String message) {
        hideLoader();
        bottomFound = false;

        if (!message.equalsIgnoreCase(getString(R.string.call_cancelled)))
            showSnackbar(message);
    }

    /* Set Data inside grid view and initiate transitions */
    void setDataInGrid() {
        int gridWidth = screenWidth / numberOfColumns - 50;
        if (imagesList.size() > 0) {
            recyclerView.setAdapter(new GridAdapter(mContext, this, imagesList, gridWidth, viewFrom));
            if (scrollPosition != -1 && offset != 0) {
                scrollToPosition(scrollPosition);
            } else
                scrollPosition = 0;

            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), numberOfColumns));

            //Grid Transitions
            prepareTransitions();
            postponeEnterTransition();

            getActivity().invalidateOptionsMenu();
        } else {
            recyclerView.setAdapter(new GridAdapter(mContext, this, imagesList, gridWidth, viewFrom));
            showSnackbar(getString(R.string.no_data));
        }
    }

    /* To show middle and bottom loaders */
    public void showLoader() {
        loader.setVisibility(offset == 0 ? View.VISIBLE : View.GONE);
        cir_loader.setVisibility(offset > 0 ? View.VISIBLE : View.GONE);
        apiCalling = true;
    }

    /* To hide middle and bottom loaders */
    public void hideLoader() {
        loader.setVisibility(View.GONE);
        cir_loader.setVisibility(View.GONE);
        apiCalling = false;
    }

    public void showSnackbar(String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
    }

    /* Options Menu Management */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (imagesList.size() > 0) {
            inflater.inflate(R.menu.menu_grid_columns_update, menu);
            HandleVisibilityOfMenuOptions(menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.column_2:
                setOptionsMenuClick(2);
                return true;
            case R.id.column_3:
                setOptionsMenuClick(3);
                return true;
            case R.id.column_4:
                setOptionsMenuClick(4);
                return true;
            default:
                break;
        }
        return false;
    }

    /* Options Menu's visibility Handling */
    void HandleVisibilityOfMenuOptions(Menu menu) {
        menu.findItem(R.id.column_2).setVisible((numberOfColumns == 2) ? false : true);
        menu.findItem(R.id.column_3).setVisible((numberOfColumns == 3) ? false : true);
        menu.findItem(R.id.column_4).setVisible((numberOfColumns == 4) ? false : true);
    }

    /* Options Menu Click Handling */
    void setOptionsMenuClick(int columns) {
        numberOfColumns = columns;
        setDataInGrid();
    }

    /* DownloadImage and save image Task */
    private class DownloadImage extends AsyncTask<Value, Void, Bitmap> {
        Value value;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(Value... values) {
            Bitmap bitmap = null;
            value = values[0];
            try {
                // Download Image from URL
                InputStream input = new java.net.URL(values[0].getThumbnailUrl()).openStream();
                // Decode Bitmap
                bitmap = BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            Drawable bitmapDrawable = new BitmapDrawable(getResources(), result);
            databaseHelper.insetImage(bitmapDrawable, value);
        }
    }

    /* Load Data from DB Task */
    private class LoadImageFromDatabaseTask extends AsyncTask<String, Void, List<Value>> {

        protected void onPreExecute() {
            showLoader();
        }

        @Override
        protected List<Value> doInBackground(String... strings) {
            return databaseHelper.getImages(strings[0]);
        }

        protected void onPostExecute(List<Value> valueList) {
            imagesList = new ArrayList(valueList);

            viewFrom = CommonFunctions.DataBase;
            setDataInGrid();

            hideLoader();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (call != null)
            call.cancel();
    }
}
