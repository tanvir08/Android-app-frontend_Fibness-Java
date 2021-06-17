package com.pes.fibness;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mapbox.api.staticmap.v1.MapboxStaticMap;
import com.mapbox.api.staticmap.v1.StaticMapCriteria;
import com.mapbox.geojson.Point;

import java.io.Serializable;
import java.util.ArrayList;

public class RoutesFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private GridLayoutManager mGridLayoutManager;
    private ArrayList<Ruta> routesList;
    private RoutesFragment.MapAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_rutas, container, false);

        routesList = User.getInstance().getRutasList();

        mGridLayoutManager = new GridLayoutManager(getContext(), 2);
        mLinearLayoutManager = new LinearLayoutManager(getContext());

        // Set up the RecyclerView
        mRecyclerView = root.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        adapter = new RoutesFragment.MapAdapter(routesList);
        adapter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mapPage = new Intent(getActivity(), MapViewActivity.class);
                int pos = mRecyclerView.getChildLayoutPosition(view);
                mapPage.putExtra("new", false);
                mapPage.putExtra("originPoint", routesList.get(pos).origen);
                mapPage.putExtra("destinationPoint", routesList.get(pos).destino);
                mapPage.putExtra("routeTitle", routesList.get(pos).name);
                mapPage.putExtra("routeDescription", routesList.get(pos).description);
                mapPage.putExtra("routePosition", pos);
                startActivity(mapPage);
            }
        });
        mRecyclerView.setAdapter(adapter);

        FloatingActionButton floatingActionButton = (FloatingActionButton) root.findViewById(R.id.floatingActionButton);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newRuta();
            }
        });

        return root;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.layout_linear:
                mRecyclerView.setLayoutManager(mLinearLayoutManager);
                break;
            case R.id.layout_grid:
                mRecyclerView.setLayoutManager(mGridLayoutManager);
                break;
        }
        return true;
    }


    public void newRuta() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(R.layout.input_new_ruta);
        builder.setTitle("Route");
        final AlertDialog dialog = builder.create();
        dialog.show();
        TextView txt = (TextView) dialog.findViewById(R.id.inputboxTitleRuta);
        txt.setText("Add a name");
        final EditText nameText = (EditText) dialog.findViewById(R.id.titleRutaInput);
        final EditText descText = (EditText) dialog.findViewById(R.id.descRutaInput);
        Button bt = (Button) dialog.findViewById(R.id.btdone);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nameText.getText().toString().trim().length() == 0) {
                    nameText.setError("Please, add a name");
                }
                else if (User.getInstance().getRutasNames().contains(nameText.getText().toString())){
                    nameText.setError("This name is already used");
                }
                else {
                    String title = nameText.getText().toString();
                    String desc = descText.getText().toString();

                    dialog.dismiss();

                    Intent mapPage = new Intent(getActivity(), MapEditActivity.class);
                    mapPage.putExtra("new", true);
                    //mapPage.putExtra("originPoint", Point.fromLngLat(0, 0));
                    //mapPage.putExtra("destinationPoint", Point.fromLngLat(0, 0));
                    mapPage.putExtra("routeTitle", title);
                    mapPage.putExtra("routeDescription", desc);
                    mapPage.putExtra("routeID", -1);
                    mapPage.putExtra("routePosition", -1);
                    startActivity(mapPage);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        routesList = User.getInstance().getRutasList();
        adapter.notifyDataSetChanged();
    }


    private class MapAdapter extends RecyclerView.Adapter<RoutesFragment.MapAdapter.ViewHolder> implements View.OnClickListener{

        private ArrayList<Ruta> namedLocations;
        private View.OnClickListener listener;

        private MapAdapter(ArrayList<Ruta> locations) {
            namedLocations = locations;
        }

        @NonNull
        @Override
        public RoutesFragment.MapAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.maps_list_row, parent, false);

            view.setOnClickListener(this);

            return new RoutesFragment.MapAdapter.ViewHolder(view);
        }

        /**
         * This function is called when the user scrolls through the screen and a new item needs
         * to be shown. So we will need to bind the holder with the details of the next item.
         */
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            if (holder == null) {
                return;
            }
            holder.bindView(position);
        }

        @Override
        public int getItemCount() {
            return namedLocations.size();
        }

        public void setOnClickListener(View.OnClickListener listener){
            this.listener = listener;
        }

        @Override
        public void onClick(View v) {
            if(listener != null){
                listener.onClick(v);
            }
        }




        class ViewHolder extends RecyclerView.ViewHolder {

            ImageView mapView;
            TextView title;
            TextView desc;
            TextView dist;
            MapboxStaticMap map;
            View layout;

            private ViewHolder(View itemView) {
                super(itemView);
                layout = itemView;
                mapView = layout.findViewById(R.id.lite_listrow_map);
                title = layout.findViewById(R.id.lite_listrow_text);
                desc = layout.findViewById(R.id.textView_desc);
                dist = layout.findViewById(R.id.textView_dist);
            }

            private void setMapLocation() {
                Ruta data = (Ruta) mapView.getTag();
                com.google.android.gms.maps.model.LatLng center = LatLngBounds.builder()
                        .include(new com.google.android.gms.maps.model.LatLng(data.origen.latitude(), data.origen.longitude()))
                        .include(new com.google.android.gms.maps.model.LatLng(data.destino.latitude(), data.destino.longitude()))
                        .build()
                        .getCenter();
                map = MapboxStaticMap.builder()
                        .accessToken(getString(R.string.mapBox_ACCESS_TOKEN))
                        .styleId(StaticMapCriteria.STREET_STYLE)
                        .cameraPoint( Point.fromLngLat(center.longitude, center.latitude))
                        .cameraZoom(13)
                        .width(320) // Image width
                        .height(150) // Image height
                        .retina(true) // Retina 2x image will be returned
                        .build();
                String imageUrl = map.url().toString();

                Glide.with(RoutesFragment.this)
                        .load(imageUrl)
                        .centerCrop()
                        .into(mapView);
            }

            private void bindView(int pos) {
                Ruta item = namedLocations.get(pos);
                // Store a reference of the ViewHolder object in the layout.
                layout.setTag(this);
                // Store a reference to the item in the mapView's tag. We use it to get the
                // coordinate of a location, when setting the map location.
                mapView.setTag(item);
                setMapLocation();
                title.setText(item.name);
                desc.setText("Descripcion: " + item.description);
                dist.setText("Distancia: " + item.distance);
            }
        }
    }

}
