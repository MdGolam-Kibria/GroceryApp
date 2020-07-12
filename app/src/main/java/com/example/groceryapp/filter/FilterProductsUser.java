package com.example.groceryapp.filter;

import android.widget.Filter;

import com.example.groceryapp.adapter.AdapterProductSeller;
import com.example.groceryapp.adapter.AdapterProductUser;
import com.example.groceryapp.model.ModelProduct;

import java.util.ArrayList;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class FilterProductsUser extends Filter {
    private AdapterProductUser adapter;
    private ArrayList<ModelProduct> filterList;


    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults result = new FilterResults();
        //validate data for search query
        if (constraint != null && constraint.length() > 0) {
            //now change to upper case to lower case
            constraint = constraint.toString().toUpperCase();
            // now store in filter list
            ArrayList<ModelProduct> filteredModels = new ArrayList<>();
            for (int i = 0; i < filterList.size(); i++) {
                //check search by title and category
                if (filterList.get(i).getProductTitle().toUpperCase().contains(constraint) ||
                        filterList.get(i).getProductCategory().toUpperCase().contains(constraint)) {
                    //add filter data to list
                    filteredModels.add(filterList.get(i));
                }
            }
            result.count = filteredModels.size();
            result.values = filteredModels;
        }else {
            //search filter empty ,return original product list
            result.count = filterList.size();
            result.values = filterList;
        }
        return result;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        adapter.productsList = (ArrayList<ModelProduct>) results.values;
        //refresh adapter
        adapter.notifyDataSetChanged();
    }
}
