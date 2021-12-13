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
    var nom = "Plaza Bicentenario"
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
        locacion = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val oyente = Oyente(this)
        locacion.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,01f,oyente)
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
        acerca.setOnClickListener {
            AlertDialog.Builder(this)
                .setMessage(" Ramón Antonio Estrada Torres \n Abril Libertad Pérez Rios \n Paulina Alejandra Nova Ramírez \n Erick Octavio Nolaco Machuca")
                .setTitle("INTEGRANTES")
                .setPositiveButton("OK"){r, q->
                }
                .show()
        }
        tienda.setOnClickListener {
            AlertDialog.Builder(this)
                .setMessage(" FAMSA TEPIC \n\n\n Farmacia Sufacen")
                .setTitle("TIENDAS CERNCAS")
                .setPositiveButton("OK"){r, q->
                }
                .show()
        }
        comida.setOnClickListener {
            AlertDialog.Builder(this)
                .setMessage(" Café Diligencias Bicentenario \n\n\n Subway \n\n\n Hotel Real de Don Juan")
                .setTitle("LUGARES DE COMIDA")
                .setPositiveButton("OK"){r, q->
                }
                .show()
        }





    }

    override fun onMapReady(googleMap: GoogleMap) {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),1)
        }
        mMap = googleMap
        // Add a marker in Sydney and move the camera
        val plazabic = LatLng(21.507286193119725, -104.89298885456299)

        mMap.addMarker(MarkerOptions().position(plazabic).title("Marcador in Plaza Bicentenario"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(plazabic,19f))
        mMap.uiSettings.isZoomControlsEnabled =  true
        mMap.isMyLocationEnabled = true
    }

}

class Oyente(puntero:MapsActivity) :LocationListener{
    private val p = puntero
    override fun onLocationChanged(location: Location) {
        val geoPosicion = GeoPoint(location.latitude,location.longitude)
        for(item in p.posicion){
            if(item.estoyEn(geoPosicion)){
                AlertDialog.Builder(p)
                    .setMessage("Usted se encuentra en ${item.nombre}. ¿Desea conocer más acerca de este lugar?")
                    .setPositiveButton("OK"){r, q->
                        val intent = Intent(p, MainActivity ::class.java)
                        intent.putExtra("idNombre",item.nombre)
                        p.startActivity(intent)
                    }
                    .setNegativeButton("NO"){d,i->

                    }
                    .show()
            }
        }
    }
}

