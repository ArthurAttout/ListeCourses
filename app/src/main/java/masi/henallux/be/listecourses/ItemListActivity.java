package masi.henallux.be.listecourses;

import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;

import java.util.ArrayList;

import masi.henallux.be.listecourses.dao.SQLShopRepository;
import masi.henallux.be.listecourses.model.Item;
import masi.henallux.be.listecourses.model.Shop;
import masi.henallux.be.listecourses.touchhelper.ItemTouchHelperCallback;
import masi.henallux.be.listecourses.utils.Constants;

public class ItemListActivity extends AppCompatActivity {

    private RecyclerView recyclerViewItems;
    private FloatingActionButton floatingActionButtonAddItem;
    private ItemAdapter adapter;
    private Shop shop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        shop = getIntent().getParcelableExtra(Constants.INTENT_KEY_SHOP);
        setTitle(shop.getName());

        recyclerViewItems = (RecyclerView)findViewById(R.id.recyclerViewItems);
        if(getResources().getBoolean(R.bool.isTablet)){
            Display display = getWindowManager().getDefaultDisplay();
            DisplayMetrics outMetrics = new DisplayMetrics();
            display.getMetrics(outMetrics);

            float density  = getResources().getDisplayMetrics().density;
            float dpWidth  = outMetrics.widthPixels / density;

            int nbColumns = (int) (dpWidth / Constants.ITEM_DEFAULT_WIDTH);
            recyclerViewItems.setLayoutManager(new GridLayoutManager(this,nbColumns));
        }
        else
        {
            recyclerViewItems.setLayoutManager(new LinearLayoutManager(this));
        }

        adapter = new ItemAdapter(shop.getChosenItems());
        recyclerViewItems.setAdapter(adapter);

        ItemTouchHelper.Callback callback =
                new ItemTouchHelperCallback(adapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerViewItems);

        floatingActionButtonAddItem = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        floatingActionButtonAddItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapter.addEmptyItem();
            }
        });
    }

    @Override
    public void onBackPressed() {
        SQLShopRepository shopRepository = SQLShopRepository.getInstance(this);
        removeAllNullItems(shop);
        shopRepository.updateShop(shop);
        super.onBackPressed();
    }

    private void removeAllNullItems(Shop shop) {
        ArrayList<Item> toRemove = new ArrayList<>();
        for(Item item : shop.getChosenItems()){
            if(item == null || item.getName().equals(""))toRemove.add(item);

        }
        shop.getChosenItems().removeAll(toRemove);
    }

    //region Adapter
    public class ItemAdapter extends RecyclerView.Adapter<ItemViewHolder> implements ItemTouchHelperAdapter{

        private final int VIEW_TYPE_ITEM = 0x18;
        private final int VIEW_TYPE_FOOTER = 0x9f;
        private final int VIEW_TYPE_NO_ITEMS = 0x47;
        private ArrayList<Item> items;

        public ItemAdapter(ArrayList<Item> arrayList) {
            items = arrayList;
            if(items.size() != 0)
            {
                if(items.get(items.size()-1) != null)
                    items.add(null); //Add an empty item to be used as a footer (if not already done)
            }
            else
            {
                //No items chosen yet
                items.add(null);
            }
        }

        public ArrayList<Item> getItems() {
            return items;
        }

        public void addEmptyItem(){
            if(items.size() == 1){
                items.clear();
                items.add(new Item(0,"",0, Item.UnitType.none));
                items.add(null);
                notifyItemInserted(0);
            }
            else
            {
                items.add(items.size()-1,new Item(0,"",0, Item.UnitType.none));
                notifyItemInserted(items.size()-1);
            }

        }

        @Override
        public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch(viewType){
                case VIEW_TYPE_ITEM:
                    CardView v = (CardView) LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_item_layout, parent, false);
                    return new NormalItemViewHolder(v);

                case VIEW_TYPE_FOOTER:
                    ConstraintLayout v2 = (ConstraintLayout) LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_item_footer, parent, false);
                    return new FooterViewHolder(v2);

                case VIEW_TYPE_NO_ITEMS:
                    ConstraintLayout v3 = (ConstraintLayout) LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_item_no_item, parent, false);
                    return new NoItemViewHolder(v3);
            }
            throw new IllegalStateException("Unrecognized viewType");
        }

        private CustomTextChangedListener textChangedListener;
        private CustomSpinnerListener quantityChangedListener;
        private CustomUnitSpinnerListener unitChangedListener;

        @Override
        public void onBindViewHolder(final ItemViewHolder holder, int position) {
            if(holder.getItemViewType() == VIEW_TYPE_ITEM){
                Item item = items.get(position);
                NormalItemViewHolder normalHolder = (NormalItemViewHolder)holder;

                unitChangedListener = new CustomUnitSpinnerListener(item,normalHolder.quantityPicker);
                quantityChangedListener = new CustomSpinnerListener(item);
                textChangedListener = new CustomTextChangedListener(item); //Used to listen in real time to changes made to elements in the list
                                                                           //If the user changes any value, the list in the shop object will be updated
                normalHolder.editTextItemName.setText(item.getName());
                normalHolder.editTextItemName.removeTextChangedListener(textChangedListener);
                normalHolder.editTextItemName.addTextChangedListener(textChangedListener);
                normalHolder.quantityPicker.setValue(item.getQuantity());
                normalHolder.quantityPicker.setOnValueChangedListener(quantityChangedListener);
                normalHolder.spinnerUnit.setSelection(item.getUnit().ordinal());
                normalHolder.spinnerUnit.setOnItemSelectedListener(unitChangedListener);
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        @Override
        public int getItemViewType(int position) {
            if(items.size() == 1) return VIEW_TYPE_NO_ITEMS;
            if(position == items.size()-1) return VIEW_TYPE_FOOTER;
            return VIEW_TYPE_ITEM;
        }

        @Override
        public void onItemMove(int fromPosition, int toPosition) {}

        @Override
        public void onItemDismiss(int position) {}

        public void removeItem(int index){
            items.remove(index);
            notifyItemRemoved(index);
        }
    }

    private abstract class ItemViewHolder extends RecyclerView.ViewHolder{

        public ItemViewHolder(View itemView) {
            super(itemView);
        }
    }

    private class NormalItemViewHolder extends ItemViewHolder{

        public NumberPicker quantityPicker;
        public EditText editTextItemName;
        public Spinner spinnerUnit;
        public NormalItemViewHolder(View itemView) {

            super(itemView);
            quantityPicker = (NumberPicker) itemView.findViewById(R.id.quantityPicker);
            quantityPicker.setMinValue(0);
            quantityPicker.setMaxValue(15);
            editTextItemName = (EditText) itemView.findViewById(R.id.editTextItemName);
            spinnerUnit = (Spinner) itemView.findViewById(R.id.spinnerUnit);
            setupAdapter();
        }

        private void setupAdapter() {
            ArrayList<String> units = new ArrayList<>();
            for(int i = 0 ; i < Item.UnitType.values().length ; i++){
                String enumString = Item.UnitType.values()[i].toString();
                String userFriendlyString = ItemListActivity.this.getString(getResources().getIdentifier(enumString,"string",getPackageName()));
                units.add(userFriendlyString);
            }
            ArrayAdapter<String> itemsAdapter =  new ArrayAdapter<>(ItemListActivity.this, R.layout.unit_list_item, units);
            itemsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerUnit.setAdapter(itemsAdapter);
        }
    }

    private class FooterViewHolder extends ItemViewHolder{

        public FooterViewHolder(View itemView) {
            super(itemView);
        }
    }

    private class NoItemViewHolder extends ItemViewHolder{

        public NoItemViewHolder(View itemView) {
            super(itemView);
        }
    }
    //endregion

    //region Custom listeners
    private class CustomTextChangedListener implements TextWatcher{

        private Item item;
        public CustomTextChangedListener(Item item) {
            this.item = item;
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            item.setName(charSequence.toString());
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void afterTextChanged(Editable editable) {}
    }

    private class CustomSpinnerListener implements NumberPicker.OnValueChangeListener{

        private Item item;

        public CustomSpinnerListener(Item item) {
            this.item = item;
        }

        @Override
        public void onValueChange(NumberPicker numberPicker, int previousValue, int newValue) {
            item.setQuantity(newValue);
        }
    }

    private class CustomUnitSpinnerListener implements Spinner.OnItemSelectedListener{

        private Item item;
        private NumberPicker quantityPicker;

        private CustomUnitSpinnerListener(Item item, NumberPicker quantityPicker) {
            this.item = item;
            this.quantityPicker = quantityPicker;
        }

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            if(i == 0) return; // Unit of type "None" chosen
            Item.UnitType chosenType = Item.UnitType.values()[i];
            item.setUnit(chosenType);

            switch(chosenType){
                case none:
                    quantityPicker.setMinValue(1);
                    quantityPicker.setMaxValue(15);
                    quantityPicker.setFormatter(null);
                    break;
                case kilogram:
                    quantityPicker.setMinValue(1);
                    quantityPicker.setMaxValue(30);
                    quantityPicker.setFormatter(null);
                    break;
                case gram:
                    quantityPicker.setMinValue(1);
                    quantityPicker.setMaxValue(1000);
                    NumberPicker.Formatter formatter = new NumberPicker.Formatter() {
                        @Override
                        public String format(int value) {
                            int diff = value * 50;
                            return "" + diff;
                        }
                    };
                    quantityPicker.setFormatter(formatter);
                    break;
                case liter:
                    quantityPicker.setMinValue(1);
                    quantityPicker.setMaxValue(60);
                    quantityPicker.setFormatter(null);
                    break;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {}
    }
    //endregion
}
