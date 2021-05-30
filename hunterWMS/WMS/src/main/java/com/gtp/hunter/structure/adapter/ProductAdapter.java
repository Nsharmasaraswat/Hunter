package com.gtp.hunter.structure.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.gtp.hunter.HunterMobileWMS;
import com.gtp.hunter.R;
import com.gtp.hunter.util.ProductUtil;
import com.gtp.hunter.wms.model.Product;
import com.gtp.hunter.wms.model.ProductField;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class ProductAdapter extends ArrayAdapter<Product> {
    private final CopyOnWriteArrayList<Product> products;
    private final Context context;
    private final int resourceId;

    public ProductAdapter(@NonNull Context context, int resource, CopyOnWriteArrayList<Product> prdList) {
        super(context, resource);
        this.context = context;
        this.resourceId = resource;
        this.products = prdList;
    }

    public ProductAdapter(@NonNull Context context, int resource, Product prdSel) {
        super(context, resource);
        this.context = context;
        this.resourceId = resource;
        this.products = new CopyOnWriteArrayList<>(HunterMobileWMS.getProductList());
        this.products.add(0, prdSel);
    }

    @Override
    public int getCount() {
        return products.size();
    }

    @Override
    public Product getItem(int i) {
        return products.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ProductViewHolder mViewHolder;
        Product p = products.get(position);
        ProductField pf = ProductUtil.getBoxUnit(p);
        String unBx = pf == null || pf.getValue().isEmpty() ? "" : String.format(Locale.US, " - C%02d", Integer.parseInt(pf.getValue()));

        if (convertView == null) {
            mViewHolder = new ProductViewHolder();

            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            convertView = Objects.requireNonNull(vi).inflate(resourceId, parent, false);

            mViewHolder.setProductName(convertView.findViewById(R.id.txtProductName));
            mViewHolder.setProductSku(convertView.findViewById(R.id.txtProductSku));
            mViewHolder.setProductId(convertView.findViewById(R.id.txtProductId));
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ProductViewHolder) convertView.getTag();
        }
        mViewHolder.setProductText(p.getId().toString(), p.getSku(), p.getName() + unBx);
        return convertView;
    }

    public ProductAdapter init() {
        Product empty = new Product();

        empty.setId(UUID.randomUUID());
        empty.setSku("");
        empty.setName(context.getString(R.string.select_product));
        products.add(0, empty);
        return this;
    }

    static class ProductViewHolder {
        private TextView productName;
        private TextView productSku;
        private TextView productId;


        public void setProductId(TextView productId) {
            this.productId = productId;
        }

        public void setProductName(TextView productName) {
            this.productName = productName;
        }

        public void setProductSku(TextView productSku) {
            this.productSku = productSku;
        }

        public void setProductText(String id, String sku, String name) {
            if (productSku != null & productName != null) {
                productId.setText(id);
                productSku.setText(sku);
                productName.setText(name);
            }
        }
    }
}
