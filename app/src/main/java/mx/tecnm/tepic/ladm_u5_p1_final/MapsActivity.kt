package mx.tecnm.tepic.ladm_u5_p1_final


import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import mx.tecnm.tepic.ladm_u5_p1_final.databinding.ActivityMapsBinding
import android.widget.ArrayAdapter
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.android.synthetic.main.activity_maps.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap : GoogleMap
    private lateinit var binding : ActivityMapsBinding
    private lateinit var awt : AutoCompleteTextView
    private var list: ArrayList<String>? = null
    private var baseRemota = FirebaseFirestore.getInstance()
    var nom = ""
    val posicion = ArrayList<Data>()
    private lateinit var locacion:LocationManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),1)
        }
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        awt = findViewById(R.id.buscar)
        list = ArrayList()
        baseRemota.collection("lugares")
            .addSnapshotListener{querySnapshot,error ->
                if(error!=null){
                    Toast.makeText(this,error.message!!,Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }
                posicion.clear()
                for(document in querySnapshot!!){
                    val data = Data()
                    val cadena = "${document.getString("nombre")}"
                    list!!.add(cadena)
                    data.nombre = document.getString("nombre").toString()
                    data.posicion1 = document.getGeoPoint("posicion1")!!
                    data.posicion2 = document.getGeoPoint("posicion2")!!
                    posicion.add(data)
                }
                awt.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, list!!))

            }

        awt.dropDownVerticalOffset = 30
        awt.setOnClickListener {
            awt.showDropDown()
        }

        awt.setOnItemClickListener { _, _, _, _ ->
            val intent = Intent(this, MainActivity ::class.java)
            nom = awt.text.toString()
            intent.putExtra("idNombre",nom)
            startActivity(intent)
        }
        locacion = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val oyente = Oyente(this)
        locacion.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,01f,oyente)

    }

    override fun onMapReady(googleMap: GoogleMap) {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),1)
        }
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val plazabic = LatLng(21.507286193119725, -104.89298885456299)
        //val bounds = LatLngBounds(LatLng(21.506996317775542, -104.89348146939408),LatLng(21.50733819461058, -104.89254806060846))

        mMap.addMarker(MarkerOptions().position(plazabic).title("Marcador in Plaza Bicentenario"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(plazabic,19f))
        mMap.uiSettings.isZoomControlsEnabled =  true
        mMap.isMyLocationEnabled = true
        miUbicacion()
    }

    fun miUbicacion(){
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        LocationServices.getFusedLocationProviderClient(this)
            .lastLocation.addOnSuccessListener {
                var geoPosicion = GeoPoint(it.latitude, it.longitude)
                for(item in posicion){
                    if(item.estoyEn(geoPosicion)){
                        AlertDialog.Builder(this)
                            .setMessage("Ver información de ${item.nombre}")
                            .setTitle("ATENCIÓN")
                            .setPositiveButton("OK"){p, q->
                                val intent = Intent(this, MainActivity ::class.java)
                                nom = awt.text.toString()
                                intent.putExtra("idNombre",nom)
                                startActivity(intent)
                            }.setNegativeButton("No"){p, q->}
                            .show()
                        break
                    }
                }
            }.addOnFailureListener {
                Toast.makeText(this,"ERROR AL OBTENER UBICACIÓN", Toast.LENGTH_LONG).show()
            }
    }
}

class Oyente(puntero:MapsActivity) :LocationListener{
    private val p = puntero
    override fun onLocationChanged(location: Location) {
        val geoPosicion = GeoPoint(location.latitude,location.longitude)
        for(item in p.posicion){
            if(item.estoyEn(geoPosicion)){
                if(item.estoyEn(geoPosicion)){
                    AlertDialog.Builder(p)
                        .setMessage("Usted se encuentra en ${item.nombre}")
                        .setTitle("ATENCIÓN")
                        .setPositiveButton("OK"){r, q->
                            Toast.makeText(p,item.nombre,Toast.LENGTH_LONG).show()
                        }
                        .show()
                }
            }
        }
    }

}

