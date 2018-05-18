package app.test.myassignment.ui_handling.adapter;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;

import app.test.myassignment.ui_handling.fragment.ImageFragment;
import app.test.myassignment.api_handling.pojo.Value;

public class ImagePagerAdapter extends FragmentStatePagerAdapter {
  private List<Value> arrayImages;
  private int viewFrom;

  public ImagePagerAdapter(Fragment fragment,List<Value> arrayImages,int viewFrom) {
    // Note: Initialize with the child fragment manager.
    super(fragment.getChildFragmentManager());
    this.arrayImages=arrayImages;
    this.viewFrom=viewFrom;
  }

  @Override
  public int getCount() {
    return arrayImages.size();
  }

  @Override
  public Fragment getItem(int position) {
    return ImageFragment.newInstance(arrayImages.get(position),viewFrom);
  }
}
