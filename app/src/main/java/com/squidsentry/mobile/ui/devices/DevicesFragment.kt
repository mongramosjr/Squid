package com.squidsentry.mobile.ui.devices

import android.os.Bundle
import android.os.Parcel
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.squidsentry.mobile.R
import com.squidsentry.mobile.databinding.FragmentDevicesBinding
import org.osmdroid.api.IMapController
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay


class DevicesFragment() : Fragment(), OnMapReadyCallback, MapListener{

    private var _binding: FragmentDevicesBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var gMap: GoogleMap

    lateinit var osMap: MapView
    lateinit var osController: IMapController;
    lateinit var osMyLocationOverlay: MyLocationNewOverlay;

    constructor(parcel: Parcel) : this() {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)




    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val devicesViewModel =
            ViewModelProvider(this).get(DevicesViewModel::class.java)

        _binding = FragmentDevicesBinding.inflate(inflater, container, false)

        val textView: TextView = binding.textDevices
        devicesViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        val mapFragment = childFragmentManager.findFragmentById(R.id.map_devices) as SupportMapFragment
        mapFragment.getMapAsync(this)

        osMap = binding.osmmap
        osMap.setTileSource(TileSourceFactory.MAPNIK)
        osMap.mapCenter
        osMap.setMultiTouchControls(true)
        //osMap.getLocalVisibleRect(Rect())

        //Then we add default zoom buttons, and ability to zoom with 2 fingers (multi-touch)
        //osMap.setBuiltInZoomControls(true);
        osMap.setMultiTouchControls(true);

        //We can move the map on a default view point. For this, we need access to the map controller
        val startPoint = GeoPoint(48.8583, 2.2944)
        osController = osMap.getController()
        osController.setZoom(9.5)
        osController.setCenter(startPoint)

        // Map Overlay
        osMyLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), osMap)
        osMyLocationOverlay.enableMyLocation()
        osMyLocationOverlay.enableFollowLocation()
        osMap.overlays.add(osMyLocationOverlay)
        osController.animateTo(startPoint)


        osController.setZoom(6.0)

        Log.e("TAG", "onCreate:in ${osController.zoomIn()}")
        Log.e("TAG", "onCreate: out  ${osController.zoomOut()}")


        osMap.addMapListener(this)


        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        gMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        gMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        gMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    override fun onResume() {
        super.onResume()
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        osMap.onResume() //needed for compass, my location overlays, v6.0.0 and up
    }

    override fun onPause() {
        super.onPause()
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        osMap.onPause() //needed for compass, my location overlays, v6.0.0 and up
    }


    override fun onScroll(event: ScrollEvent?): Boolean {
        // event?.source?.getMapCenter()
        Log.e("TAG", "onCreate:la ${event?.source?.getMapCenter()?.latitude}")
        Log.e("TAG", "onCreate:lo ${event?.source?.getMapCenter()?.longitude}")
        //  Log.e("TAG", "onScroll   x: ${event?.x}  y: ${event?.y}", )
        return true
    }

    override fun onZoom(event: ZoomEvent?): Boolean {
        //  event?.zoomLevel?.let { controller.setZoom(it) }
        Log.e("TAG", "onZoom zoom level: ${event?.zoomLevel}   source:  ${event?.source}")
        return false;
    }

    /*
    override fun onGpsStatusChanged(event: Int) {


        TODO("Not yet implemented")
    }
*/
}