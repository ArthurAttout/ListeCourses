package masi.henallux.be.listecourses;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import masi.henallux.be.listecourses.AddOrUpdateShopDialog.AddShopCallback;
import masi.henallux.be.listecourses.dao.SQLShopRepository;
import masi.henallux.be.listecourses.model.Item;
import masi.henallux.be.listecourses.model.Shop;
import masi.henallux.be.listecourses.utils.Constants;

public class ShopListActivity extends AppCompatActivity implements AddShopCallback {

    private RecyclerView shopRecyclerView;
    private ConstraintLayout rootLayout;
    private ProgressBar progressBar;
    private ShopAdapter adapter;
    private DialogFragment dialogFragment;
    private boolean shouldShowMapDrawable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(getString(R.string.shop_list_title));

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        rootLayout = (ConstraintLayout) findViewById(R.id.activity_main);
        shopRecyclerView = (RecyclerView)findViewById(R.id.recyclerViewShops);
        shopRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        shouldShowMapDrawable = retrievePreferenceMaps();

        adapter = new ShopAdapter();
        shopRecyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateRecyclerView();
    }

    private void updateRecyclerView() {
        new RetrieveAllShopsAsync() {
            @Override
            protected void onPostExecute(ArrayList<Shop> shops) {
                adapter.setShops(shops);
                progressBar.setVisibility(View.GONE);
            }
        }.execute(shouldShowMapDrawable);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        MenuItem streetView = menu.findItem(R.id.checkboxActivateStreetView);
        streetView.setChecked(shouldShowMapDrawable);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.checkboxActivateStreetView:
                shouldShowMapDrawable = !shouldShowMapDrawable;
                item.setChecked(shouldShowMapDrawable);
                savePreference();
                updateRecyclerView();
                return true;
            case R.id.actionAddShop:
                showAddShopDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showAddShopDialog() {
        dialogFragment = new AddOrUpdateShopDialog();
        dialogFragment.show(getFragmentManager(), "AddOrUpdateShopDialog");
    }

    private void showUpdateShopDialog(Shop shopToUpdate) {
        dialogFragment = new AddOrUpdateShopDialog();
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.BUNDLE_SHOP_TO_UPDATE,shopToUpdate);
        dialogFragment.setArguments(bundle);
        dialogFragment.show(getFragmentManager(), "AddOrUpdateShopDialog");
    }

    private void savePreference() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPref.edit();
        edit.putBoolean(Constants.SHOULD_SHOW_MAPS_PREFERENCE, shouldShowMapDrawable);
        edit.commit();
    }

    private boolean retrievePreferenceMaps(){
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        return sharedPref.getBoolean(Constants.SHOULD_SHOW_MAPS_PREFERENCE,false);
    }

    @Override
    public void addShopCallback(Shop newShop) {
        dialogFragment.dismiss();
        newShop = SQLShopRepository.getInstance(this).insertShop(newShop);
        adapter.addShop(newShop);
    }

    @Override
    public void updateShopCallback(Shop updatedShop) {
        dialogFragment.dismiss();
        SQLShopRepository.getInstance(this).updateShop(updatedShop);
        adapter.setShop(updatedShop);
    }

    private abstract class RetrieveAllShopsAsync extends AsyncTask<Boolean,Void,ArrayList<Shop>>{

        @Override
        protected ArrayList<Shop> doInBackground(Boolean... shouldShowStreetViewDrawable) {
            SQLShopRepository repository = SQLShopRepository.getInstance(ShopListActivity.this);
            //TODO dependency injection ..

            if(shouldShowStreetViewDrawable[0]){
                return new ArrayList<Shop>(repository.tryGetAllShopsWithPictures());
            }
            return new ArrayList<Shop>(repository.getAllShops());
        }

        @Override
        protected abstract void onPostExecute(ArrayList<Shop> shops);
    }

    //region RecyclerView Adapter

    public class ShopAdapter extends RecyclerView.Adapter<ShopViewHolder> {

        public ArrayList<Shop> shops = new ArrayList<>();
        private ShopViewHolder viewHolder;

        public ShopAdapter(ArrayList<Shop> arrayList) {
            shops = arrayList;
        }

        public ShopAdapter() {
        }

        @Override
        public ShopViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            CardView v = (CardView) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_shop_layout, parent, false);
            viewHolder = new ShopViewHolder(v);
            return viewHolder;
        }

        public void setShops(ArrayList<Shop> shops) {
            this.shops = shops;
            notifyDataSetChanged();
        }

        public void addShop(Shop shop) {
            shops.add(shop);
            notifyItemInserted(shops.size() - 1);
        }

        @Override
        public void onBindViewHolder(final ShopViewHolder holder, final int position) {
            final Shop shop = shops.get(position);
            holder.textViewShopName.setText(shop.getName());
            holder.textViewCity.setText(shop.getAddress().getCity());

            if (shop.getShopMapUriDrawable() != null && shouldShowMapDrawable) {
                holder.imageViewMap.setScaleType(ImageView.ScaleType.FIT_XY);
                Picasso.with(ShopListActivity.this)
                        .load(shop.getShopMapUriDrawable())
                        .memoryPolicy(MemoryPolicy.NO_CACHE)
                        .into(holder.imageViewMap);
            } else {
                if (shop.getShopLogoUriDrawable() != null) {
                    holder.imageViewMap.setScaleType(ImageView.ScaleType.CENTER);
                    Picasso.with(ShopListActivity.this)
                            .load(shop.getShopLogoUriDrawable())
                            .fit()
                            .centerInside()
                            .into(holder.imageViewMap);
                } else {
                    Picasso.with(ShopListActivity.this)
                            .load(R.drawable.default_shop)
                            .into(holder.imageViewMap);
                }
            }

            holder.buttonDeleteAllItems.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final ArrayList<Item> savedItems = (ArrayList<Item>) shop.getChosenItems().clone();
                    SQLShopRepository.getInstance(ShopListActivity.this).deleteAllItemsOfShop(shop);
                    Snackbar snackbar = Snackbar
                            .make(rootLayout, R.string.confirmation_all_items_deleted, Snackbar.LENGTH_LONG);
                    snackbar.setAction(R.string.undo, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            shop.addItemRange(savedItems);
                            SQLShopRepository.getInstance(ShopListActivity.this).updateShop(shop);
                            holder.buttonDeleteAllItems.setEnabled(true);
                        }
                    });
                    snackbar.show();
                    holder.buttonDeleteAllItems.setEnabled(false);
                }
            });

            //Can delete all items of a shop only if the shop has items selected
            holder.buttonDeleteAllItems.setEnabled(shop.getChosenItems().size() != 0);


            holder.buttonShowListItems.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    gotoListItems(shops.get(position));
                }
            });

            holder.imageViewMap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    gotoListItems(shops.get(position));
                }
            });

            holder.imageViewMap.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    showUpdateShopDialog(shops.get(position));
                    return true;
                }
            });
        }


        private void gotoListItems(Shop shop) {
            Intent intent = new Intent(getBaseContext(), ItemListActivity.class);
            intent.putExtra(Constants.INTENT_KEY_SHOP, shop);
            startActivity(intent);
        }

        @Override
        public int getItemCount() {
            return shops.size();
        }

        public void setShop(Shop updatedShop) {
            Shop correspondingShop = null;
            for(Shop shop : shops){
                if(shop.getId() == updatedShop.getId()){
                    correspondingShop = shop;
                }
            }
            correspondingShop.setAddress(updatedShop.getAddress());
            correspondingShop.setName(updatedShop.getName());
            notifyItemChanged(shops.indexOf(correspondingShop));
            //Other attributes cannot be modified through UI, so no need to set them
        }
    }

    private class ShopViewHolder extends RecyclerView.ViewHolder{

        public ImageButton imageViewMap;
        public TextView textViewShopName;
        public TextView textViewCity;
        public Button buttonShowListItems;
        public Button buttonDeleteAllItems;

        public ShopViewHolder(View itemView) {

            super(itemView);
            imageViewMap = (ImageButton) itemView.findViewById(R.id.imageViewMapShop);
            textViewShopName = (TextView) itemView.findViewById(R.id.textViewShopName);
            textViewCity = (TextView) itemView.findViewById(R.id.textViewCity);
            buttonShowListItems = (Button) itemView.findViewById(R.id.buttonListItems);
            buttonDeleteAllItems = (Button) itemView.findViewById(R.id.buttonEmptyList);
        }
    }

    //endregion
}
