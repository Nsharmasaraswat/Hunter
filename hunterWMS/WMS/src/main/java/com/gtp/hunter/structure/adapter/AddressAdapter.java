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
import com.gtp.hunter.wms.model.Address;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class AddressAdapter extends ArrayAdapter<Address> {
    private final CopyOnWriteArrayList<Address> addresses;
    private final Context context;
    private final int resourceId;

    public AddressAdapter(@NonNull Context context, int resource, CopyOnWriteArrayList<Address> list) {
        super(context, resource);
        this.context = context;
        this.resourceId = resource;
        this.addresses = list;
    }

    public AddressAdapter(@NonNull Context context, int resource, Address addrSel, String type) {
        super(context, resource);
        this.context = context;
        this.resourceId = resource;
        this.addresses = new CopyOnWriteArrayList<>(HunterMobileWMS.getAddressList(type));
        this.addresses.add(0, addrSel);
    }

    public AddressAdapter init() {
        Address emptyAddress = new Address();

        emptyAddress.setId(UUID.randomUUID());
        emptyAddress.setName(context.getString(R.string.select_address));
        addresses.add(0, emptyAddress);
        return this;
    }

    @Override
    public int getCount() {
        return addresses.size();
    }

    @Override
    public Address getItem(int i) {
        return addresses.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        AddressViewHolder mViewHolder;
        Address p = addresses.get(position);

        if (convertView == null) {
            mViewHolder = new AddressViewHolder();

            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            convertView = Objects.requireNonNull(vi).inflate(resourceId, parent, false);

            mViewHolder.setAddressId(convertView.findViewById(R.id.txtAddressId));
            mViewHolder.setAddressName(convertView.findViewById(R.id.txtAddressName));
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (AddressViewHolder) convertView.getTag();
        }
        mViewHolder.setAddressText(p.getId().toString(), p.getName());
        return convertView;
    }

    public int getItemPosition(Address a) {
        return addresses.indexOf(a);
    }

    static class AddressViewHolder {
        private TextView addressName;
        private TextView addressId;


        public void setAddressId(TextView addressId) {
            this.addressId = addressId;
        }

        public void setAddressName(TextView addressName) {
            this.addressName = addressName;
        }

        public void setAddressText(String id, String name) {
            if (addressId != null & addressName != null) {
                addressId.setText(id);
                addressName.setText(name);
            }
        }
    }

    public void remove(Address address) {
        this.addresses.remove(address);
    }

    public void setAddresses(Collection<Address> addrList) {
        this.addresses.clear();
        this.addresses.addAll(addrList);
    }

    public void sortList(boolean reverse) {
        Address[] ar = new Address[addresses.size()];
        Comparator<Address> compareByMetaname = (Address o1, Address o2) -> {
            if (o1 == null && o2 == null) return 0;
            if (o2 == null) return reverse ? 1 : -1;
            if (o1 == null) return reverse ? -1 : 1;
            if (o1.getMetaname() == null && o2.getMetaname() == null) return 0;
            if (o2.getMetaname() == null) return reverse ? 1 : -1;
            if (o1.getMetaname() == null) return reverse ? -1 : 1;
            return o1.getMetaname().compareTo(o2.getMetaname()) * (reverse ? -1 : 1);
        };

        ar = addresses.toArray(ar);
        Arrays.sort(ar, compareByMetaname);
        addresses.clear();
        addresses.addAll(Arrays.asList(ar));
    }
}
